import { PrismaClient, UserRole } from '@prisma/client';
import { PrismaPg } from '@prisma/adapter-pg';
import { Pool } from 'pg';
import * as bcrypt from 'bcrypt';
import 'dotenv/config';

const pool = new Pool({ connectionString: process.env.DATABASE_URL });
const adapter = new PrismaPg(pool);
const prisma = new PrismaClient({ adapter });

async function main() {
  console.log('🌱 Iniciando seed de la base de datos...');

  // Crear usuario admin
  const adminPassword = await bcrypt.hash('admin123', 10);
  
  const admin = await prisma.user.upsert({
    where: { email: 'admin@ticketera.com' },
    update: {},
    create: {
      email: 'admin@ticketera.com',
      password: adminPassword,
      firstName: 'Admin',
      lastName: 'Sistema',
      role: UserRole.ADMIN,
    },
  });

  console.log('✅ Usuario admin creado:', admin.email);

  // Crear usuario cliente de prueba
  const clientPassword = await bcrypt.hash('cliente123', 10);
  
  const client = await prisma.user.upsert({
    where: { email: 'cliente@test.com' },
    update: {},
    create: {
      email: 'cliente@test.com',
      password: clientPassword,
      firstName: 'Cliente',
      lastName: 'Prueba',
      role: UserRole.CLIENT,
    },
  });

  console.log('✅ Usuario cliente creado:', client.email);

  // Crear evento de ejemplo
  const event = await prisma.event.create({
    data: {
      name: 'Concierto de Rock Universitario',
      description: 'Una noche épica de rock con las mejores bandas universitarias',
      date: new Date('2026-03-20T20:00:00Z'),
      venue: 'Auditorio Central Universidad',
      imageUrl: 'https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3',
      ticketPrice: 15.00,
      totalSeats: 100,
    },
  });

  console.log('✅ Evento creado:', event.name);

  // Crear asientos para el evento
  const seats: Array<{
    eventId: string;
    row: number;
    column: number;
    isOccupied: boolean;
  }> = [];
  for (let row = 1; row <= 10; row++) {
    for (let column = 1; column <= 10; column++) {
      seats.push({
        eventId: event.id,
        row,
        column,
        isOccupied: false,
      });
    }
  }

  await prisma.seat.createMany({
    data: seats,
  });

  console.log('✅ 100 asientos creados (10x10)');

  console.log('\n🎉 Seed completado exitosamente!');
  console.log('\n📝 Credenciales de prueba:');
  console.log('   Admin: admin@ticketera.com / admin123');
  console.log('   Cliente: cliente@test.com / cliente123');
}

main()
  .catch((e) => {
    console.error('❌ Error en seed:', e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
