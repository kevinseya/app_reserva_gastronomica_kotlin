import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class ReservationsService {
  constructor(private prisma: PrismaService) {}

  // Crear reserva con hold en asientos (transacción)
  async create(userId: string, dto: any) {
    const eventId = dto.eventId;
    const eventTableId = dto.tableId;
    const seatIds: string[] = dto.seatIds || [];
    const partySize = dto.partySize || seatIds.length || 1;

    if (!eventId) throw new Error('eventId es requerido');
    if (!eventTableId) throw new Error('tableId (eventTableId) es requerido');

    if (seatIds.length > 6) throw new Error('Máximo 6 asientos por mesa');

    const now = new Date();
    const expiresAt = new Date(now.getTime() + 10 * 60 * 1000); // 10 minutos

    const result = await this.prisma.$transaction(async (tx) => {
      // Verificar que la mesa exista y capacidad
      const table = await tx.eventTable.findUnique({ where: { id: eventTableId }, include: { seats: true } });
      if (!table) throw new Error('Mesa no encontrada');
      if (seatIds.length > table.capacity) throw new Error('Se solicitaron más asientos que la capacidad de la mesa');

      const reservation = await tx.reservation.create({
        data: {
          userId,
          eventId,
          eventTableId,
          datetime: dto.datetime ? new Date(dto.datetime) : new Date(),
          partySize,
          status: 'PENDING',
          notes: dto.notes,
          expiresAt,
          requestedSeatIds: seatIds.length > 0 ? JSON.stringify(seatIds) : null,
        },
      });

      // Calcular precio total de asientos (no marcamos seats aquí)
      let seatsTotal = 0;
      if (seatIds.length > 0) {
        const seats = await tx.tableSeat.findMany({ where: { id: { in: seatIds } } });
        seatsTotal = seats.reduce((acc, s) => acc + (s.price || 0), 0);
      } else if (table.seatPrice) {
        seatsTotal = table.seatPrice * partySize;
      }

      return { reservation, seatsTotal };
    });

    return result;
  }

  findByUser(userId: string) {
    return this.prisma.reservation.findMany({ where: { userId }, orderBy: { datetime: 'desc' } });
  }

  findOne(id: string) {
    return this.prisma.reservation.findUnique({ where: { id } });
  }

  // Crear orden ligada a una reserva
  async createOrder(reservationId: string, items: Array<{ menuItemId: string; quantity: number }>) {
    // transacción: crear order y orderItems y actualizar total
    const result = await this.prisma.$transaction(async (tx) => {
      const reservation = await tx.reservation.findUnique({ where: { id: reservationId } });
      if (!reservation) throw new Error('Reserva no encontrada');

      // calcular total de items
      let total = 0;
      const itemsToCreate = [] as any[];
      for (const it of items) {
        const menu = await tx.menuItem.findUnique({ where: { id: it.menuItemId } });
        if (!menu) throw new Error('MenuItem no encontrado: ' + it.menuItemId);
        const price = menu.price || 0;
        total += price * (it.quantity || 1);
        itemsToCreate.push({ menuItemId: it.menuItemId, quantity: it.quantity || 1, price });
      }

      const order = await tx.order.create({ data: { reservationId, total, status: 'CREATED' } });

      for (const it of itemsToCreate) {
        await tx.orderItem.create({ data: { orderId: order.id, menuItemId: it.menuItemId, quantity: it.quantity, price: it.price } });
      }

      return tx.order.findUnique({ where: { id: order.id }, include: { items: true } });
    });

    return result;
  }
}
