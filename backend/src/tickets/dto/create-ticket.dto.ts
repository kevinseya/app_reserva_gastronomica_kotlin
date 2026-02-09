import { IsString, IsArray, IsOptional, ValidateNested, IsNumber } from 'class-validator';
import { Type } from 'class-transformer';

class FoodItemOrderDto {
  @IsString()
  foodId: string;

  @IsNumber()
  quantity: number;
}

class SeatFoodOrderDto {
  @IsString()
  seatId: string;

  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => FoodItemOrderDto)
  foodItems: FoodItemOrderDto[];
}

export class CreateTicketDto {
  @IsString()
  eventId: string;

  @IsArray()
  @IsString({ each: true })
  seatIds: string[];

  @IsOptional()
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => SeatFoodOrderDto)
  foodOrders?: SeatFoodOrderDto[];
}