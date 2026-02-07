import { NestFactory } from '@nestjs/core';
import { ValidationPipe } from '@nestjs/common';
import { AppModule } from './app.module';
import { NestExpressApplication } from '@nestjs/platform-express';
import { join } from 'path';

async function bootstrap() {
  const app = await NestFactory.create<NestExpressApplication>(AppModule);
  
  // Servir archivos estáticos usando ruta absoluta al directorio public del proyecto
  // (no relativa a dist, porque dist se regenera en cada compilación)
  const publicPath = join(process.cwd(), 'public');
  console.log('📁 PUBLIC PATH:', publicPath);
  
  app.useStaticAssets(publicPath, {
    prefix: '/',
  });
  
  // Habilitar CORS
  app.enableCors({
    origin: true, // En producción, especificar los dominios permitidos
    credentials: true,
  });

  // Habilitar validación global
  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true,
      forbidNonWhitelisted: true,
      transform: true,
    }),
  );

  const port = process.env.PORT || 3000;
  await app.listen(port);
  
  console.log(`🚀 Servidor corriendo en http://localhost:${port}`);
}
bootstrap();
