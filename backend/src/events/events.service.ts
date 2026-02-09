import { Injectable, NotFoundException, BadRequestException, Logger } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { CreateEventDto } from './dto/create-event.dto';
import { UpdateEventDto } from './dto/update-event.dto';

interface AutoTablesDto {
  tablesCount: number;
  capacity: number; // max 6 enforced by caller
}

@Injectable()
export class EventsService {
  private readonly logger = new Logger(EventsService.name);

  constructor(private prisma: PrismaService) {}

  async create(createEventDto: CreateEventDto, file?: Express.Multer.File) {
    let imageUrl = createEventDto.imageUrl
      ? createEventDto.imageUrl.replace(/\u0000/g, '').trim()
      : undefined;

    // Si se subió un archivo, usar su ruta
    if (file) {
      imageUrl = `/uploads/${file.filename}`;
    }

    // Ya NO crear asientos automáticamente - se crearán con las mesas
    const event = await this.prisma.event.create({
      data: {
        ...createEventDto,
        imageUrl,
        date: new Date(createEventDto.date),
        totalSeats: createEventDto.totalSeats || 0,
      },
    });

    return event;
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

  // Genera mesas automáticamente para un evento en forma de grid
  async generateTables(eventId: string, dto: AutoTablesDto) {
    const event = await this.prisma.event.findUnique({ where: { id: eventId } });
    if (!event) throw new NotFoundException('Evento no encontrado');

    const tablesCount = dto.tablesCount || 1;
    const capacity = Math.min(dto.capacity || 4, 6);

    const columns = Math.ceil(Math.sqrt(tablesCount));
    const rows = Math.ceil(tablesCount / columns);

    const createdTables = [] as any[];

    for (let i = 0; i < tablesCount; i++) {
      const row = Math.floor(i / columns);
      const col = i % columns;
      const x = col + 1; // simple grid coords
      const y = row + 1;

      const table = await this.prisma.eventTable.create({
        data: {
          eventId,
          name: `Mesa ${i + 1}`,
          x,
          y,
          capacity,
        },
      });

      // crear asientos para la mesa
      const seatsData = [] as any[];
      for (let s = 0; s < capacity; s++) {
        // store price as decimal (e.g. 4.50)
        seatsData.push({ tableId: table.id, index: s + 1, price: event.ticketPrice ?? 0 });
      }

      await this.prisma.tableSeat.createMany({ data: seatsData });
      createdTables.push(table);
    }

    return this.prisma.eventTable.findMany({ where: { eventId }, include: { seats: true } });
  }

  // Crear una mesa individual con sus asientos
  async createEventTable(eventId: string, data: any) {
    this.logger.log(`createEventTable called for eventId=${eventId} data=${JSON.stringify(data)}`);
    const event = await this.prisma.event.findUnique({ where: { id: eventId } });
    if (!event) throw new NotFoundException('Evento no encontrado');

    const capacity = Math.min(data.capacity || 4, 8); // máximo 8 asientos
    // seatPrice comes as decimal (e.g. 4.50) - store directly
    const seatPrice = typeof data.seatPrice === 'number' ? data.seatPrice : (event.ticketPrice ?? 0);

    const table = await this.prisma.eventTable.create({
      data: {
        eventId,
        name: data.name || 'Mesa',
        x: data.x || 0,
        y: data.y || 0,
        rotation: data.rotation || 0,
        capacity,
        seatPrice,
      },
    });

    this.logger.log(`Created EventTable id=${table.id} for eventId=${eventId}`);

    // Crear asientos para la mesa
    const seatsData: any[] = [];
    for (let i = 0; i < capacity; i++) {
      seatsData.push({
        tableId: table.id,
        index: i + 1,
        price: seatPrice,
      });
    }

    await this.prisma.tableSeat.createMany({ data: seatsData });

    this.logger.log(`Created ${seatsData.length} seats for tableId=${table.id}`);

    return this.prisma.eventTable.findUnique({
      where: { id: table.id },
      include: { seats: true },
    });
  }

  // Obtener todas las mesas de un evento
  async getEventTables(eventId: string) {
    const event = await this.prisma.event.findUnique({ where: { id: eventId } });
    if (!event) throw new NotFoundException('Evento no encontrado');

    return this.prisma.eventTable.findMany({
      where: { eventId },
      include: { seats: true },
      orderBy: { name: 'asc' },
    });
  }

  // Update an event table (capacity/seatPrice/name/coords)
  async updateEventTable(eventId: string, tableId: string, body: any) {
    const table = await this.prisma.eventTable.findUnique({ where: { id: tableId }, include: { seats: true } });
    if (!table || table.eventId !== eventId) throw new NotFoundException('Mesa no encontrada');

    const dataToUpdate: any = {};
    if (typeof body.name === 'string') dataToUpdate.name = body.name;
    if (typeof body.x === 'number') dataToUpdate.x = body.x;
    if (typeof body.y === 'number') dataToUpdate.y = body.y;
    if (typeof body.rotation === 'number') dataToUpdate.rotation = body.rotation;
    if (typeof body.seatPrice === 'number') dataToUpdate.seatPrice = body.seatPrice;

    // Handle capacity change
    if (typeof body.capacity === 'number') {
      const newCap = Math.max(1, Math.min(6, Number(body.capacity)));
      if (newCap !== table.capacity) {
        // If decreasing, ensure no reserved seats would be removed
        if (newCap < table.capacity) {
          const occupied = table.seats.filter(s => s.reservationId !== null).length;
          if (occupied > newCap) {
            throw new BadRequestException('No se puede reducir la capacidad por debajo de asientos ya reservados');
          }
          // delete extra seats (highest index)
          const seatsToDelete = table.seats
            .sort((a,b) => b.index - a.index)
            .slice(0, table.seats.length - newCap)
            .map(s => s.id);
          if (seatsToDelete.length) {
            await this.prisma.tableSeat.deleteMany({ where: { id: { in: seatsToDelete } } });
          }
        } else {
          // increasing capacity: create new seats
          const toAdd = newCap - table.capacity;
          const seatsData: any[] = [];
          const basePrice = typeof body.seatPrice === 'number' ? body.seatPrice : (table.seatPrice ?? 0);
          const startIndex = table.seats.length ? Math.max(...table.seats.map(s=>s.index)) : 0;
          for (let i = 1; i <= toAdd; i++) {
            seatsData.push({ tableId: tableId, index: startIndex + i, price: basePrice });
          }
          if (seatsData.length) await this.prisma.tableSeat.createMany({ data: seatsData });
        }
        dataToUpdate.capacity = newCap;
      }
    }

    const updated = await this.prisma.eventTable.update({ where: { id: tableId }, data: dataToUpdate, include: { seats: true } });
    return updated;
  }

  async removeEventTable(eventId: string, tableId: string) {
    const table = await this.prisma.eventTable.findUnique({ where: { id: tableId }, include: { seats: true } });
    if (!table || table.eventId !== eventId) throw new NotFoundException('Mesa no encontrada');

    // Prevent deletion if any seat is reserved
    const occupied = table.seats.filter(s => s.reservationId !== null).length;
    if (occupied > 0) {
      throw new BadRequestException('No se puede eliminar una mesa con asientos reservados');
    }

    await this.prisma.tableSeat.deleteMany({ where: { tableId } });
    await this.prisma.eventTable.delete({ where: { id: tableId } });

    return { ok: true };
  }
}
