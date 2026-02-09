import { Controller, Get, Post, Patch, Delete, Body, Param, UseGuards, UseInterceptors, UploadedFile } from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import { FoodService } from './food.service';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { RolesGuard } from '../auth/guards/roles.guard';
import { Roles } from '../auth/decorators/roles.decorator';
import { UserRole } from '@prisma/client';
import { diskStorage } from 'multer';
import { extname } from 'path';

@Controller('food')
export class FoodController {
  constructor(private readonly foodService: FoodService) {}

  @Get('menu')
  getMenu() {
    return this.foodService.getMenu();
  }

  @Post('categories')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN)
  createCategory(@Body() body: { name: string }) {
    return this.foodService.createCategory(body.name);
  }

  @Post('items')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN)
  @UseInterceptors(FileInterceptor('image', {
    storage: diskStorage({
      destination: './public/uploads',
      filename: (req, file, cb) => {
        const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
        const ext = extname(file.originalname);
        cb(null, `food-${uniqueSuffix}${ext}`);
      },
    }),
  }))
  createItem(
    @Body() body: { categoryId: string; name: string; description?: string; price: any },
    @UploadedFile() file: Express.Multer.File,
  ) {
    const imageUrl = file ? `/uploads/${file.filename}` : undefined;
    return this.foodService.createFoodItem(body.categoryId, {
      ...body,
      price: parseFloat(body.price),
      imageUrl
    });
  }

  @Patch('items/:id')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN)
  @UseInterceptors(FileInterceptor('image', {
    storage: diskStorage({
      destination: './public/uploads',
      filename: (req, file, cb) => {
        const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
        const ext = extname(file.originalname);
        cb(null, `food-${uniqueSuffix}${ext}`);
      },
    }),
  }))
  updateItem(
    @Param('id') id: string,
    @Body() body: { categoryId?: string; name?: string; description?: string; price?: any },
    @UploadedFile() file: Express.Multer.File,
  ) {
    const imageUrl = file ? `/uploads/${file.filename}` : undefined;
    return this.foodService.updateFoodItem(id, {
      ...body,
      price: body.price ? parseFloat(body.price) : undefined,
      imageUrl
    });
  }

  @Delete('items/:id')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN)
  deleteItem(@Param('id') id: string) {
    return this.foodService.deleteFoodItem(id);
  }
}