# 🚀 Guía de Inicio Rápido

## Paso 1: Configurar PostgreSQL

Asegúrate de tener PostgreSQL instalado y corriendo en tu máquina.

Crea la base de datos:
```bash
# Conéctate a PostgreSQL
psql -U postgres

# Crea la base de datos
CREATE DATABASE ticketera;

# Sal de psql
\q
```

## Paso 2: Configurar Variables de Entorno

Copia el archivo de ejemplo y edita las variables:
```bash
cd backend
cp .env.example .env
```

Edita `.env` con tus credenciales reales:
- `DATABASE_URL`: URL de conexión a tu PostgreSQL local
- `JWT_SECRET`: Genera un secreto seguro
- `STRIPE_SECRET_KEY`: Tu clave de Stripe (obtener en https://stripe.com)

## Paso 3: Ejecutar Migraciones

```bash
# Generar y aplicar migraciones
pnpm prisma migrate dev --name init

# Generar cliente de Prisma
pnpm prisma generate
```

**Nota sobre Prisma 7:** Este proyecto usa Prisma 7 con el adaptador de PostgreSQL (`@prisma/adapter-pg`). El DATABASE_URL se configura en el archivo `prisma.config.ts` y se pasa al PrismaClient mediante el adaptador.

## Paso 4: Poblar la Base de Datos (Opcional)

Ejecuta el seed para crear usuarios de prueba y un evento de ejemplo:
```bash
pnpm run prisma:seed
```

Esto creará:
- **Admin**: admin@ticketera.com / admin123
- **Cliente**: cliente@test.com / cliente123
- Un evento de ejemplo con 100 asientos

## Paso 5: Iniciar el Servidor

```bash
# Modo desarrollo (hot-reload)
pnpm run start:dev
```

El servidor estará disponible en: http://localhost:3000

## 🧪 Probar la API

### Registrar un nuevo usuario:
```bash
curl -X POST http://localhost:3000/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'
```

### Login:
```bash
curl -X POST http://localhost:3000/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@ticketera.com",
    "password": "admin123"
  }'
```

### Listar eventos:
```bash
curl http://localhost:3000/events
```

## 🔍 Herramientas Útiles

### Prisma Studio (GUI para la BD)
```bash
pnpm prisma studio
```
Abre en: http://localhost:5555

### Ver logs del servidor
El servidor mostrará logs en la terminal donde ejecutaste `start:dev`

## 🆘 Solución de Problemas

### Error de conexión a PostgreSQL
- Verifica que PostgreSQL esté corriendo
- Revisa que la URL en `.env` sea correcta
- Confirma que la base de datos `ticketera` exista

### Error de Prisma Client
```bash
pnpm prisma generate
```

### Resetear base de datos
```bash
pnpm prisma migrate reset
pnpm run prisma:seed
```

## 📱 Siguiente Paso

Una vez que el backend esté funcionando correctamente, puedes proceder a crear la aplicación móvil en Kotlin que consumirá esta API.
