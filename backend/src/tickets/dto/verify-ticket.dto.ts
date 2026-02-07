import { IsString, IsNotEmpty } from 'class-validator';

export class VerifyTicketDto {
  @IsString()
  @IsNotEmpty()
  qrCode: string;
}
