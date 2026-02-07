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

    // Verificar que todos los asientos existen y están disponibles
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

    // Calcular el monto total
    const totalAmount = event.ticketPrice * createTicketDto.seatIds.length;

    // Crear el PaymentIntent en Stripe
    const paymentIntent = await this.stripe.paymentIntents.create({
      amount: Math.round(totalAmount * 100), // Stripe usa centavos
      currency: 'usd',
      automatic_payment_methods: {
        enabled: true,
      },
      metadata: {
        userId,
        eventId: createTicketDto.eventId,
        seatIds: createTicketDto.seatIds.join(','),
        seatCount: createTicketDto.seatIds.length.toString(),
      },
    });

    return {
      clientSecret: paymentIntent.client_secret,
      amount: totalAmount,
      paymentIntentId: paymentIntent.id,
    };
  }

  async confirmPayment(userId: string, paymentIntentId: string) {
    // Verificar el pago en Stripe
    const paymentIntent = await this.stripe.paymentIntents.retrieve(paymentIntentId);

    if (paymentIntent.status !== 'succeeded') {
      throw new BadRequestException('El pago no ha sido completado');
    }

    const { eventId, seatIds } = paymentIntent.metadata || {};

    if (!eventId || !seatIds) {
      throw new BadRequestException('Metadata de pago incompleta');
    }

    const seatIdArray = seatIds.split(',');

    // Verificar nuevamente que los asientos estén disponibles
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

    // Crear tickets
    const tickets = await this.createTicketsWithoutStripe(
      userId,
      eventId,
      seatIdArray,
      paymentIntentId
    );

    return tickets;
  }

  async getUserTickets(userId: string) {
    return this.prisma.ticket.findMany({
      where: { userId },
      include: {
        event: true,
        seat: true,
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
    paymentId: string
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
        for (const seatId of seatIds) {
          const qrCode = `TCK-${randomUUID()}`;

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
