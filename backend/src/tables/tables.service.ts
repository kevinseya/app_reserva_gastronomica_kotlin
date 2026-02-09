import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class TablesService {
  constructor(private prisma: PrismaService) {}

  findAll() {
    return this.prisma.table.findMany({ orderBy: { number: 'asc' } });
  }

  create(data: any) {
    return this.prisma.table.create({ data });
  }

  update(id: string, data: any) {
    return this.prisma.table.update({ where: { id }, data });
  }
}
