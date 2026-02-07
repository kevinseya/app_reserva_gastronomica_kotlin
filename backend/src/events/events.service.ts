import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { CreateEventDto } from './dto/create-event.dto';
import { UpdateEventDto } from './dto/update-event.dto';

@Injectable()
export class EventsService {
  constructor(private prisma: PrismaService) {}

  async create(createEventDto: CreateEventDto, file?: Express.Multer.File) {
    let imageUrl = createEventDto.imageUrl
      ? createEventDto.imageUrl.replace(/\u0000/g, '').trim()
      : undefined;

    // Si se subió un archivo, usar su ruta
    if (file) {
      imageUrl = `/uploads/${file.filename}`;
    }

    // Calcular dimensiones del cuadrado según totalSeats
    const requestedSeats = createEventDto.totalSeats || 100;
    const sideLength = Math.ceil(Math.sqrt(requestedSeats));
    const totalSeatsInGrid = sideLength * sideLength;

    const event = await this.prisma.event.create({
      data: {
        ...createEventDto,
        imageUrl,
        date: new Date(createEventDto.date),
        totalSeats: requestedSeats, // Guardar solo los asientos solicitados
      },
    });

    // Crear los asientos en forma de cuadrado (sideLength x sideLength)
    const seats: Array<{
      eventId: string;
      row: number;
      column: number;
      isOccupied: boolean;
    }> = [];
    let seatCount = 0;
    for (let row = 1; row <= sideLength; row++) {
      for (let column = 1; column <= sideLength; column++) {
        seatCount++;
        // Solo los primeros 'requestedSeats' están disponibles, el resto bloqueados
        seats.push({
          eventId: event.id,
          row,
          column,
          isOccupied: seatCount > requestedSeats, // Bloquear asientos extras
        });
      }
    }

    await this.prisma.seat.createMany({
      data: seats,
    });

    return this.findOne(event.id);
  }

  async findAll() {
    return this.prisma.event.findMany({
      orderBy: { date: 'asc' },
      include: {
        _count: {
          select: {
            seats: {
              where: { isOccupied: false },
            },
          },
        },
      },
    });
  }

  async findOne(id: string) {
    const event = await this.prisma.event.findUnique({
      where: { id },
      include: {
        seats: {
          orderBy: [{ row: 'asc' }, { column: 'asc' }],
        },
        _count: {
          select: {
            tickets: true,
          },
        },
      },
    });

    if (!event) {
      throw new NotFoundException('Evento no encontrado');
    }

    return event;
  }

  async update(id: string, updateEventDto: UpdateEventDto, file?: Express.Multer.File) {
    const event = await this.prisma.event.findUnique({ where: { id } });

    if (!event) {
      throw new NotFoundException('Evento no encontrado');
    }

    const dataToUpdate: any = { ...updateEventDto };
    if (updateEventDto.date) {
      dataToUpdate.date = new Date(updateEventDto.date);
    }
    
    // Si se subió un archivo, usar su ruta
    if (file) {
      dataToUpdate.imageUrl = `/uploads/${file.filename}`;
    } else if (typeof updateEventDto.imageUrl === 'string') {
      dataToUpdate.imageUrl = updateEventDto.imageUrl.replace(/\u0000/g, '').trim();
    }

    return this.prisma.event.update({
      where: { id },
      data: dataToUpdate,
      include: {
        seats: {
          orderBy: [{ row: 'asc' }, { column: 'asc' }],
        },
      },
    });
  }

  async remove(id: string) {
    const event = await this.prisma.event.findUnique({ where: { id } });

    if (!event) {
      throw new NotFoundException('Evento no encontrado');
    }

    await this.prisma.event.delete({ where: { id } });
    
    return { message: 'Evento eliminado exitosamente' };
  }

  async getAvailableSeats(eventId: string) {
    const event = await this.prisma.event.findUnique({ where: { id: eventId } });

    if (!event) {
      throw new NotFoundException('Evento no encontrado');
    }

    return this.prisma.seat.findMany({
      where: {
        eventId,
        isOccupied: false,
      },
      orderBy: [{ row: 'asc' }, { column: 'asc' }],
    });
  }
}
