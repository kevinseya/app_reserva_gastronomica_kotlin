export class UpdateEventTableDto {
  name?: string;
  x?: number;
  y?: number;
  rotation?: number;
  capacity?: number; // 1..6
  seatPrice?: number; // in cents
}
