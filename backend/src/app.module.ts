import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { PrismaModule } from './prisma/prisma.module';
import { AuthModule } from './auth/auth.module';
import { UsersModule } from './users/users.module';
import { EventsModule } from './events/events.module';
import { TicketsModule } from './tickets/tickets.module';
import { FoodModule } from './food/food.module';

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
    }),
    PrismaModule,
    AuthModule,
    UsersModule,
    EventsModule,
    TicketsModule,
    FoodModule,
    // new modules
    require('./reservations/reservations.module').ReservationsModule,
    require('./orders/orders.module').OrdersModule,
  ],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}
