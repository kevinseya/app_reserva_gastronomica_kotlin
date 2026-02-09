export class CreateReservationDto {
  eventId?: string;
  tableId?: string; // eventTable id
  seatIds?: string[];
  datetime: string; // ISO string
  partySize?: number;
  notes?: string;
}
