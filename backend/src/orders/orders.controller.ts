import { Controller, Post, Param, Body } from '@nestjs/common';
import Stripe from 'stripe';
import { PrismaService } from '../prisma/prisma.service';

@Controller('orders')
export class OrdersController {
  private stripe: Stripe;
  constructor(private prisma: PrismaService) {
    // Omitimos apiVersion explícita para evitar errores de tipado
    this.stripe = new Stripe(process.env.STRIPE_SECRET_KEY || '', {} as any);
  }

  @Post(':id/pay')
  async pay(@Param('id') id: string) {
    const order = await this.prisma.order.findUnique({ where: { id }, include: { reservation: true } });
    if (!order) throw new Error('Order no encontrada');
    if (order.status === 'PAID') {
      return { message: 'Order already paid' };
    }

    const amount = order.total || 0;
    const paymentIntent = await this.stripe.paymentIntents.create({
      amount,
      currency: process.env.STRIPE_CURRENCY || 'usd',
      metadata: { orderId: order.id },
    });

    await this.prisma.order.update({ where: { id: order.id }, data: { status: 'PROCESSING', paymentIntentId: paymentIntent.id } });

    return { client_secret: paymentIntent.client_secret, paymentIntentId: paymentIntent.id };
  }

  @Post(':id/confirm')
  async confirm(@Param('id') id: string, @Body() body: any) {
    // body: { paymentIntentId }
    const { paymentIntentId } = body;
    if (!paymentIntentId) throw new Error('paymentIntentId requerido');

    const pi = await this.stripe.paymentIntents.retrieve(paymentIntentId);
    if (!pi) throw new Error('PaymentIntent no encontrado');

    if (pi.status === 'succeeded') {
      // marcar order y reservation como pagadas/confirmadas y asignar asientos solicitados
      const order = await this.prisma.order.findUnique({ where: { id }, include: { reservation: true } });
      if (!order) throw new Error('Order no encontrada');
      if (!order.reservationId) {
        // No reservation attached
        await this.prisma.order.update({ where: { id: order.id }, data: { status: 'PAID' } });
        return { ok: true };
      }

      const reservation = await this.prisma.reservation.findUnique({ where: { id: order.reservationId } });
      if (!reservation) throw new Error('Reservation no encontrada');

      // parse requested seat ids
      const requestedSeatIds: string[] = reservation.requestedSeatIds ? JSON.parse(reservation.requestedSeatIds) : [];

      // Try to claim seats in a transaction
      await this.prisma.$transaction(async (tx) => {
        if (requestedSeatIds.length > 0) {
          const upd = await tx.tableSeat.updateMany({ where: { id: { in: requestedSeatIds }, reservationId: null }, data: { reservationId: reservation.id } });
          if (upd.count !== requestedSeatIds.length) {
            throw new Error('Algunos asientos solicitados ya fueron ocupados por otra compra');
          }
        }

        await tx.order.update({ where: { id: order.id }, data: { status: 'PAID' } });
        await tx.reservation.update({ where: { id: reservation.id }, data: { status: 'CONFIRMED' } });
      });

      return { ok: true };
    }

    return { ok: false, status: pi.status };
  }
}
