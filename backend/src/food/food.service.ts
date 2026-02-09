import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class FoodService {
  constructor(private prisma: PrismaService) {}

  async getMenu() {
    return this.prisma.foodCategory.findMany({
      include: { items: true },
      orderBy: { name: 'asc' }
    });
  }

  async createCategory(name: string) {
    return this.prisma.foodCategory.create({
      data: { name }
    });
  }

  async createFoodItem(categoryId: string, data: { name: string; description?: string; price: number; imageUrl?: string }) {
    return this.prisma.foodItem.create({
      data: {
        categoryId,
        name: data.name,
        description: data.description,
        price: data.price,
        imageUrl: data.imageUrl
      }
    });
  }

  async updateFoodItem(id: string, data: { categoryId?: string; name?: string; description?: string; price?: number; imageUrl?: string }) {
    return this.prisma.foodItem.update({
      where: { id },
      data: {
        categoryId: data.categoryId,
        name: data.name,
        description: data.description,
        price: data.price,
        imageUrl: data.imageUrl
      }
    });
  }

  async deleteFoodItem(id: string) {
    return this.prisma.foodItem.delete({ where: { id } });
  }
}