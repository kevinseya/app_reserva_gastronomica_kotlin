import { Controller, Get, Post, Body, Param, UseGuards } from '@nestjs/common';
import { TicketsService } from './tickets.service';
import { CreateTicketDto } from './dto/create-ticket.dto';
import { VerifyTicketDto } from './dto/verify-ticket.dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { RolesGuard } from '../auth/guards/roles.guard';
import { Roles } from '../auth/decorators/roles.decorator';
import { CurrentUser } from '../auth/decorators/current-user.decorator';
import { UserRole } from '@prisma/client';

@Controller('tickets')
@UseGuards(JwtAuthGuard)
export class TicketsController {
  constructor(private readonly ticketsService: TicketsService) {}

  @Post('create-payment-intent')
  createPaymentIntent(
    @CurrentUser() user: any,
    @Body() createTicketDto: CreateTicketDto,
  ) {
    return this.ticketsService.createPaymentIntent(user.userId, createTicketDto);
  }

  @Post('confirm-payment/:paymentIntentId')
  confirmPayment(
    @CurrentUser() user: any,
    @Param('paymentIntentId') paymentIntentId: string,
  ) {
    return this.ticketsService.confirmPayment(user.userId, paymentIntentId);
  }

  @Get('my-tickets')
  getUserTickets(@CurrentUser() user: any) {
    return this.ticketsService.getUserTickets(user.userId);
  }

  @Get(':id')
  getTicketById(@CurrentUser() user: any, @Param('id') id: string) {
    return this.ticketsService.getTicketById(id, user.userId);
  }

  @Post('verify')
  @UseGuards(RolesGuard)
  @Roles(UserRole.ADMIN)
  verifyTicket(@Body() verifyTicketDto: VerifyTicketDto) {
    return this.ticketsService.verifyTicket(verifyTicketDto.qrCode);
  }

  @Get()
  @UseGuards(RolesGuard)
  @Roles(UserRole.ADMIN)
  getAllTickets() {
    return this.ticketsService.getAllTickets();
  }
}
