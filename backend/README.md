# Gastronomia API

API backend para `Gastronomia App`, desarrollada con NestJS, PostgreSQL, Prisma y Stripe. Provee endpoints para eventos, gestión de asientos, pagos con Stripe y generación/verificación de tickets.

## 🚀 Tecnologías

- **NestJS** - Framework backend
- **PostgreSQL** - Base de datos
- **Prisma** - ORM
- **JWT** - Autenticación
- **Stripe** - Procesamiento de pagos
- **QRCode** - Generación de códigos QR para tickets

## 📋 Prerequisitos

- Node.js >= 18
- pnpm
- PostgreSQL >= 14

## 🔧 Instalación

1. Instalar dependencias:
```bash
cd backend
pnpm install
```

2. Configurar variables de entorno:
```bash
cp .env.example .env
```

Editar `.env` con tus credenciales:
```env
DATABASE_URL="postgresql://usuario:password@localhost:5432/ticketera?schema=public"
JWT_SECRET="tu-secreto-super-seguro"
JWT_EXPIRATION="7d"
STRIPE_SECRET_KEY="sk_test_tu_clave_de_stripe"
STRIPE_WEBHOOK_SECRET="whsec_tu_secreto_de_webhook"
PORT=3000
NODE_ENV=development
```

3. Ejecutar migraciones de Prisma:
```bash
pnpm prisma migrate dev --name init
```

4. Generar cliente de Prisma:
```bash
pnpm prisma generate
```

## 🏃 Ejecutar el servidor

```bash
# Desarrollo
pnpm run start:dev

# Producción
pnpm run build
pnpm run start:prod
```

El servidor estará disponible en `http://localhost:3000`

## 📚 Estructura del Proyecto

```
backend/
├── prisma/
│   └── schema.prisma          # Esquema de base de datos
├── src/
│   ├── auth/                  # Módulo de autenticación
│   ├── events/                # Módulo de eventos
│   ├── prisma/                # Módulo de Prisma
│   ├── tickets/               # Módulo de tickets
│   ├── users/                 # Módulo de usuarios
│   ├── app.module.ts
│   └── main.ts
└── package.json
```

## 🗄️ Modelo de Datos

### User
- Roles: ADMIN, CLIENT
- Campos: email, password (hasheado), firstName, lastName

### Event
- Información del evento, precio (decimal, e.g. 3.50), total de asientos.

### Cambios importantes sobre precios

- En versiones anteriores algunos precios (asientos, items de menú) se almacenaban como enteros en centavos. En esta versión los campos de precio se almacenan como decimales (Float) y representan el importe en la moneda base (por ejemplo `4.50`).
- Al crear un `PaymentIntent` para Stripe el backend multiplicará cada importe decimal por 100 para obtener centavos (valor entero) que exige Stripe.

Si actualizarás desde una versión anterior que usaba centavos (INT), realiza estos pasos (ejemplo PostgreSQL) para convertir los valores existentes:

```sql
-- Sólo ejecutar si tus datos actuales están en centavos (p. ej. 450 para $4.50)
BEGIN;
UPDATE table_seats SET price = price::numeric / 100.0 WHERE price IS NOT NULL;
UPDATE event_tables SET seatPrice = seatPrice::numeric / 100.0 WHERE seatPrice IS NOT NULL;
UPDATE menu_items SET price = price::numeric / 100.0 WHERE price IS NOT NULL;
UPDATE order_items SET price = price::numeric / 100.0 WHERE price IS NOT NULL;
UPDATE orders SET total = total::numeric / 100.0 WHERE total IS NOT NULL;
COMMIT;
```

Luego ejecuta la migración de Prisma para adaptar el esquema (se recomienda respaldar la BD antes).

### Migración Prisma

1. Crear y aplicar migración:

```bash
pnpm prisma migrate dev --name prices-to-float
pnpm prisma generate
```

2. Si migraste datos manualmente (script SQL), reinicia el servidor.

### Seat
- Posición: fila (1-10), columna (1-10), Estado: isOccupied

### Ticket
- Relación con User, Event, Seat, QR único
- Estados: PENDING, PAID, USED, CANCELLED

## 🔐 API Endpoints

### Autenticación
- `POST /auth/register` - Registrar usuario
- `POST /auth/login` - Iniciar sesión
- `GET /auth/profile` - Perfil (requiere JWT)

### Usuarios (Solo ADMIN)
- `GET /users` - Listar usuarios
- `GET /users/:id` - Usuario por ID
- `PATCH /users/:id` - Actualizar usuario
- `DELETE /users/:id` - Eliminar usuario

### Eventos
- `POST /events` - Crear evento (ADMIN)
- `GET /events` - Listar eventos
- `GET /events/:id` - Evento por ID
- `GET /events/:id/seats` - Asientos disponibles
- `PATCH /events/:id` - Actualizar (ADMIN)
- `DELETE /events/:id` - Eliminar (ADMIN)

### Tickets
- `POST /tickets/create-payment-intent` - Crear pago (JWT)
- `POST /tickets/confirm-payment/:paymentIntentId` - Confirmar pago
- `GET /tickets/my-tickets` - Mis tickets
- `GET /tickets/:id` - Ticket por ID
- `POST /tickets/verify` - Verificar QR (ADMIN)
- `GET /tickets` - Listar todos (ADMIN)

## 📦 Scripts

```bash
pnpm run start:dev      # Desarrollo
pnpm run build          # Build
pnpm run start:prod     # Producción
pnpm prisma studio      # GUI para BD
```

## License

MIT
