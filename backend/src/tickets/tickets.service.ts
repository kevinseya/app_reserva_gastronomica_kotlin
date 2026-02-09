import { Injectable, BadRequestException, NotFoundException, ConflictException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { PrismaService } from '../prisma/prisma.service';
import { CreateTicketDto } from './dto/create-ticket.dto';
import { TicketStatus } from '@prisma/client';
import Stripe from 'stripe';
import { randomUUID } from 'crypto';

@Injectable()
export class TicketsService {
  private stripe: Stripe;

  constructor(
    private prisma: PrismaService,
    private configService: ConfigService,
  ) {
    const stripeKey = this.configService.get<string>('STRIPE_SECRET_KEY');
    if (!stripeKey) {
      throw new Error('STRIPE_SECRET_KEY no está configurado en las variables de entorno');
    }
    this.stripe = new Stripe(stripeKey, {
      apiVersion: '2026-01-28.clover',
    });
  }

  async createPaymentIntent(userId: string, createTicketDto: CreateTicketDto) {
    // Verificar que el evento existe
    const event = await this.prisma.event.findUnique({
      where: { id: createTicketDto.eventId },
    });

    if (!event) {
      throw new NotFoundException('Evento no encontrado');
    }

    // Intentar detectar si se están comprando table_seats (mesas)
    const tableSeats = await this.prisma.tableSeat.findMany({
      where: { id: { in: createTicketDto.seatIds } },
      include: { table: true },
    });

    let amountCents: number;

    if (tableSeats.length === createTicketDto.seatIds.length) {
      // Comprando asientos de mesa
      const unavailable = tableSeats.filter(ts => ts.reservationId !== null);
      if (unavailable.length > 0) {
        throw new ConflictException('Uno o más asientos de mesa ya están reservados');
      }

      const tableMismatch = tableSeats.filter(ts => ts.table.eventId !== createTicketDto.eventId);
      if (tableMismatch.length > 0) {
        throw new BadRequestException('Uno o más asientos de mesa no pertenecen a este evento');
      }

      // Sumar precios: precio base del evento (una sola vez) + precio por asiento (tableSeat.price en centavos)
      // event.ticketPrice is a decimal per ticket; charge it per seat selected
      const eventPriceCents = Math.round(event.ticketPrice * 100) * createTicketDto.seatIds.length;
      // tableSeats.price is now stored as decimal (e.g. 4.50), convert to cents
      const seatsTotalCents = tableSeats.reduce((acc, ts) => acc + Math.round((ts.price || 0) * 100), 0);
      amountCents = eventPriceCents + seatsTotalCents;
    } else {
      // Flujo tradicional: seats grid
      const seats = await this.prisma.seat.findMany({
        where: { id: { in: createTicketDto.seatIds } },
      });

      if (seats.length !== createTicketDto.seatIds.length) {
        throw new NotFoundException('Uno o más asientos no encontrados');
      }

      const occupiedSeats = seats.filter(seat => seat.isOccupied);
      if (occupiedSeats.length > 0) {
        throw new ConflictException('Uno o más asientos ya están ocupados');
      }

      const wrongEventSeats = seats.filter(seat => seat.eventId !== createTicketDto.eventId);
      if (wrongEventSeats.length > 0) {
        throw new BadRequestException('Uno o más asientos no pertenecen a este evento');
      }

      amountCents = Math.round(event.ticketPrice * 100) * createTicketDto.seatIds.length;
    }

    // Calcular costo de comida si existe
    let foodTotalCents = 0;
    if (createTicketDto.foodOrders && createTicketDto.foodOrders.length > 0) {
      for (const order of createTicketDto.foodOrders) {
        for (const item of order.foodItems) {
          const food = await this.prisma.foodItem.findUnique({ where: { id: item.foodId } });
          if (food) {
            foodTotalCents += Math.round((food.price || 0) * 100) * item.quantity;
          }
        }
      }
    }
    amountCents += foodTotalCents;

    // Crear el PaymentIntent en Stripe
    const paymentIntent = await this.stripe.paymentIntents.create({
      amount: amountCents, // valor en centavos
      currency: 'usd',
      automatic_payment_methods: {
        enabled: true,
      },
      metadata: {
        userId,
        eventId: createTicketDto.eventId,
        seatIds: createTicketDto.seatIds.join(','),
        seatCount: createTicketDto.seatIds.length.toString(),
        // Guardamos la orden de comida como JSON string en metadata para recuperarla al confirmar
        foodOrders: createTicketDto.foodOrders ? JSON.stringify(createTicketDto.foodOrders) : '',
      },
    });

    return {
      clientSecret: paymentIntent.client_secret,
      amount: amountCents,
      paymentIntentId: paymentIntent.id,
    };
  }

  async confirmPayment(userId: string, paymentIntentId: string) {
    // Verificar el pago en Stripe
    const paymentIntent = await this.stripe.paymentIntents.retrieve(paymentIntentId);

    if (paymentIntent.status !== 'succeeded') {
      throw new BadRequestException('El pago no ha sido completado');
    }

    const { eventId, seatIds, foodOrders } = paymentIntent.metadata || {};

    if (!eventId || !seatIds) {
      throw new BadRequestException('Metadata de pago incompleta');
    }

    const seatIdArray = seatIds.split(',');
    const parsedFoodOrders = foodOrders ? JSON.parse(foodOrders) : [];

    // Intentamos detectar si los seatIds pertenecen a table_seats (disposición por mesas)
    const tableSeats = await this.prisma.tableSeat.findMany({
      where: { id: { in: seatIdArray } },
      include: { table: true },
    });

    if (tableSeats.length === seatIdArray.length) {
      // Flujo de compra por mesa / asientos de mesa
      const tickets = await this.prisma.$transaction(async (prisma) => {
        // Comprobar disponibilidad (reservationId debe ser null)
        const available = await prisma.tableSeat.findMany({
          where: { id: { in: seatIdArray }, reservationId: null },
          include: { table: true },
        });

        if (available.length !== seatIdArray.length) {
          throw new ConflictException('Uno o más asientos de mesa ya están reservados');
        }

        // Crear una reserva que agrupe la compra por mesa
        const firstTable = available[0].table;
        const reservation = await prisma.reservation.create({
          data: {
            userId,
            eventId,
            eventTableId: firstTable.id,
            datetime: new Date(),
            partySize: available.length,
            status: 'CONFIRMED',
            requestedSeatIds: seatIdArray.join(','),
          },
        });

        // Marcar table_seats con reservationId
        await prisma.tableSeat.updateMany({
          where: { id: { in: seatIdArray } },
          data: { reservationId: reservation.id },
        });

        // Marcar la mesa como ocupada
        await prisma.eventTable.update({
          where: { id: firstTable.id },
          data: { status: 'OCCUPIED' },
        });

        // Crear tickets asociados a las table_seats (campo tableSeatId en Ticket)
        const createdTickets: any[] = [];
        for (const ts of available) {
          // Generar payload para QR que incluya mesa y número de asiento
          const payload = {
            id: randomUUID(),
            type: 'table_seat',
            eventId,
            userId,
            tableId: ts.table?.id ?? null,
            tableName: ts.table?.name ?? null,
            seatIndex: ts.index,
            tableSeatId: ts.id,
            createdAt: new Date().toISOString(),
            paymentIntentId
          };
          const qrCode = Buffer.from(JSON.stringify(payload)).toString('base64');

          const ticket = await prisma.ticket.create({
            data: {
              userId,
              eventId,
              tableSeatId: ts.id,
              qrCode,
              stripePaymentId: paymentIntentId,
              status: TicketStatus.PAID,
            },
            include: {
              event: true,
              tableSeat: {
                include: { table: true }
              },
            },
          });

          // Asociar comida a este ticket específico si hay orden para este asiento
          const seatOrder = parsedFoodOrders.find((o: any) => o.seatId === ts.id);
          if (seatOrder && seatOrder.foodItems) {
            for (const item of seatOrder.foodItems) {
              await prisma.ticketFood.create({
                data: {
                  ticketId: ticket.id,
                  foodItemId: item.foodId,
                  quantity: item.quantity,
                  status: 'PENDING'
                }
              });
            }
          }
          createdTickets.push(ticket);
        }

        return createdTickets;
      }, { timeout: 15000 });

      return tickets;
    }

    // Flujo tradicional (Seats Grid)
    const seats = await this.prisma.seat.findMany({
      where: { id: { in: seatIdArray } },
    });

    if (seats.length !== seatIdArray.length) {
      throw new NotFoundException('Uno o más asientos no encontrados');
    }

    const occupiedSeats = seats.filter(seat => seat.isOccupied);
    if (occupiedSeats.length > 0) {
      throw new ConflictException('Uno o más asientos ya fueron ocupados');
    }

    // Crear tickets (asientos tradicionales)
    const tickets = await this.createTicketsWithoutStripe(
      userId,
      eventId,
      seatIdArray,
      paymentIntentId,
      parsedFoodOrders
    );

    return tickets;
  }

  async getUserTickets(userId: string) {
    return this.prisma.ticket.findMany({
      where: { userId },
      include: {
        event: true,
        seat: true,
        tableSeat: { include: { table: true } },
        foodItems: { include: { foodItem: { include: { category: true } } } } // Incluir comida y categoría
      },
      orderBy: { purchaseDate: 'desc' },
    });
  }

  async getTicketById(id: string, userId: string) {
    const ticket = await this.prisma.ticket.findUnique({
      where: { id },
      include: {
        event: true,
        seat: true,
        tableSeat: { include: { table: true } },
        foodItems: { include: { foodItem: { include: { category: true } } } },
        user: {
          select: {
            id: true,
            email: true,
            firstName: true,
            lastName: true,
          },
        },
      },
    });

    if (!ticket) {
      throw new NotFoundException('Ticket no encontrado');
    }

    if (ticket.userId !== userId) {
      throw new BadRequestException('No tienes permiso para ver este ticket');
    }

    return ticket;
  }

  private async createTicketsWithoutStripe(
    userId: string,
    eventId: string,
    seatIds: string[],
    paymentId: string,
    foodOrders: any[] = []
  ) {
    // Intentamos hacer la menor cantidad de operaciones dentro
    // de la transacción para evitar timeouts. Primero marcamos
    // todos los asientos como ocupados con un solo updateMany
    // condicional (sólo marcará los que aún no están ocupados),
    // y comprobamos que la cuenta coincide con la cantidad de
    // asientos solicitados. Luego creamos los tickets.

    const tickets = await this.prisma.$transaction(
      async (prisma) => {
        // Marcar todos los asientos en una sola operación atómica
        const updateResult = await prisma.seat.updateMany({
          where: { id: { in: seatIds }, isOccupied: false },
          data: { isOccupied: true },
        });

        if (updateResult.count !== seatIds.length) {
          // Alguno de los asientos ya fue ocupado por otra transacción
          throw new ConflictException('Uno o más asientos ya fueron ocupados');
        }

        const createdTickets: any[] = [];
        // Obtener detalles de asientos para incluir fila/columna en el QR
        const seatsInfo = await prisma.seat.findMany({ where: { id: { in: seatIds } } });
        for (const seatId of seatIds) {
          const seatInfo = seatsInfo.find(s => s.id === seatId);
          const payload = {
            id: randomUUID(),
            type: 'seat',
            eventId,
            userId,
            seatId,
            row: seatInfo?.row ?? null,
            column: seatInfo?.column ?? null,
            createdAt: new Date().toISOString(),
            paymentId
          };
          const qrCode = Buffer.from(JSON.stringify(payload)).toString('base64');

          const ticket = await prisma.ticket.create({
            data: {
              userId,
              eventId,
              seatId,
              qrCode,
              stripePaymentId: paymentId,
              status: TicketStatus.PAID,
            },
            include: {
              event: true,
              seat: true,
            },
          });

          // Asociar comida
          const seatOrder = foodOrders.find((o: any) => o.seatId === seatId);
          if (seatOrder && seatOrder.foodItems) {
            for (const item of seatOrder.foodItems) {
              await prisma.ticketFood.create({
                data: {
                  ticketId: ticket.id,
                  foodItemId: item.foodId,
                  quantity: item.quantity,
                  status: 'PENDING'
                }
              });
            }
          }
          createdTickets.push(ticket);
        }

        return createdTickets;
      },
      // Aumentar el timeout de la transacción interactiva a 15s
      { timeout: 15000 }
    );

    return tickets;
  }

  async verifyTicket(qrCode: string) {
    const ticket = await this.prisma.ticket.findUnique({
      where: { qrCode },
      include: {
        event: true,
        seat: true,
        tableSeat: true,
        foodItems: { include: { foodItem: { include: { category: true } } } }, // Retornar comida al escanear
        user: {
          select: {
            id: true,
            email: true,
            firstName: true,
            lastName: true,
          },
        },
      },
    });

    if (!ticket) {
      throw new NotFoundException('Ticket no encontrado o inválido');
    }

    if (ticket.status === TicketStatus.USED) {
      return {
        valid: false,
        message: 'Este ticket ya fue usado',
        ticket,
      };
    }

    if (ticket.status === TicketStatus.CANCELLED) {
      return {
        valid: false,
        message: 'Este ticket está cancelado',
        ticket,
      };
    }

    // Marcar como usado
    await this.prisma.ticket.update({
      where: { id: ticket.id },
      data: { status: TicketStatus.USED },
    });

    return {
      valid: true,
      message: 'Ticket válido',
      ticket,
    };
  }

  async getAllTickets() {
    return this.prisma.ticket.findMany({
      include: {
        event: true,
        seat: true,
        tableSeat: { include: { table: true } },
        foodItems: { include: { foodItem: { include: { category: true } } } },
        user: {
          select: {
            id: true,
            email: true,
            firstName: true,
            lastName: true,
          },
        },
      },
      orderBy: { purchaseDate: 'desc' },
    });
  }
}
