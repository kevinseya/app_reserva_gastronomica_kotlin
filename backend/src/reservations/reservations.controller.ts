import { Controller, Get, Post, Body, Param, UseGuards } from '@nestjs/common';
import { ReservationsService } from './reservations.service';
import { CreateReservationDto } from './dto/create-reservation.dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { CurrentUser } from '../auth/decorators/current-user.decorator';

@Controller('reservations')
@UseGuards(JwtAuthGuard)
export class ReservationsController {
  constructor(private readonly reservationsService: ReservationsService) {}

  @Post()
  create(@CurrentUser() user: any, @Body() dto: CreateReservationDto) {
    return this.reservationsService.create(user.userId, dto);
  }

  @Post(':id/order')
  createOrder(@Param('id') id: string, @Body() body: any) {
    // body: { items: [{ menuItemId, quantity }] }
    return this.reservationsService.createOrder(id, body.items || []);
  }

  @Get('my')
  myReservations(@CurrentUser() user: any) {
    return this.reservationsService.findByUser(user.userId);
  }

  @Get(':id')
  findOne(@Param('id') id: string) {
    return this.reservationsService.findOne(id);
  }
}
