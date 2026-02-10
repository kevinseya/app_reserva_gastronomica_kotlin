# 🎫 App Gastronomia Universitaria

Sistema completo para gestión y venta de tickets y reservas para eventos universitarios con la app móvil (`appGastronomia`) y el backend (`backend`).

## 📁 Estructura del Proyecto

```
mespinoza/
├── backend/          # API REST con NestJS + PostgreSQL + Prisma
└── appGastronomia/   # Aplicación móvil Android (Kotlin + Compose)
```

## 🚀 Backend (NestJS)

Backend RESTful con las siguientes características:

### ✨ Características Principales

- Autenticación JWT
- Roles de Usuario: ADMIN y CLIENT
- Gestión de eventos (CRUD)
- **Reserva de mesas y asientos** (Mapa interactivo)
- **Venta de comida y bebidas por categorías**
- Pagos con Stripe
- Tickets con QR
- Validación de entrada por escaneo

### 🛠️ Tecnologías

- NestJS
- PostgreSQL
- Prisma ORM
- JWT
- Stripe
- bcrypt

### 📚 Documentación

- [README Backend](backend/README.md)
- [SETUP.md](backend/SETUP.md)
- [API_DOCS.md](backend/API_DOCS.md)

IMPORTANT: cambios recientes

- Los precios ahora se manejan en formato decimal (ej. `4.50`) en la aplicación y en la base de datos. El backend convierte esos valores a centavos únicamente antes de crear el PaymentIntent en Stripe.
- Tras actualizar a esta versión debes ejecutar la migración de Prisma y (si tienes datos previos en centavos) aplicar un script de conversión de datos (ver secciones Backend y Migración).

### 🏃 Inicio Rápido

```bash
cd backend
pnpm install
cp .env.example .env
pnpm prisma migrate dev --name init
pnpm prisma generate
pnpm run prisma:seed
pnpm run start:dev
```

Servidor en http://localhost:3000

### 🔑 Credenciales de Prueba

- Admin: admin@ticketera.com / admin123
- Cliente: cliente@test.com / cliente123

## 📱 App Móvil (Android)

### 🧩 Stack Kotlin

- Kotlin 2.x + Jetpack Compose (Material 3)
- Hilt (DI)
- Retrofit + OkHttp
- Kotlinx Serialization
- Stripe Android SDK (PaymentSheet)
- ZXing (escáner QR)

### 🧱 Componentes UI (Compose)

- AppTopBar (menú contextual por rol)
- BottomNavigationBar (Eventos, Mis Tickets, Admin/Escanear, Perfil)
- EventCard + buscador en Eventos
- SeatGrid + SeatItem (selección de asientos)
- PaymentSheet (Stripe)
- TicketCard + TicketDetail (QR + detalles)
- AdminActionCard + StatCard
- ProfileOption + diálogo de edición
- Help/FAQ (acordeón)

### 🖥️ Pantallas principales

**Cliente**
- Login / Registro
- Eventos (lista + búsqueda)
- Detalle de evento + compra
- Mis Tickets
- Detalle de Ticket (QR)
- Perfil (editar datos)
- Ayuda / Acerca de

**Admin**
- Panel Admin (estadísticas)
- Gestionar Eventos (CRUD)
- Gestionar Usuarios (CRUD)
- Escanear QR (cámara)

### ✅ Funcionalidades (Cliente)

- Registro y login
- Búsqueda de eventos
- Selección de asientos
- Pago con Stripe (PaymentSheet)
- Tickets con QR
- Perfil con edición de datos

### ✅ Funcionalidades (Admin)

- Dashboard con ingresos
- CRUD de eventos
- CRUD de usuarios
- Escaneo QR para marcar tickets como USADO

### 🧪 Ejecutar en Emulador

```bash
cd appGastronomia
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 📱 Ejecutar en Celular (misma red)

La app detecta si está en emulador o dispositivo físico:

- Emulador: http://10.0.2.2:3000
- Dispositivo: http://192.168.1.8:3000

Asegúrate de tener el backend corriendo en tu PC y el celular en la misma red Wi‑Fi.

## 🗄️ Modelo de Datos

### User
- Roles: ADMIN, CLIENT
- Autenticación JWT
- Gestión de perfil

### Event
- Información del evento
- Precio de tickets
- 100 asientos (10x10)
- Imagen y descripción

### Seat
- Relación con Mesa (Table)
- Posición y número
- Estado de ocupación
- Relación con evento

### Food & Menu
- **Category**: Categorías de comida (Entradas, Platos Fuertes, Bebidas)
- **FoodItem**: Productos con precio, imagen y descripción
- **Order**: Registro de comida comprada junto con los tickets

### Ticket
- Relación con usuario, evento y asiento
- Estados: PENDING, PAID, USED, CANCELLED
- QR único para validación
- ID de pago de Stripe

## 🎯 Flujos principales

### Compra
1. Cliente selecciona evento
2. Elige **mesas y asientos** en el mapa
3. Agrega **comida y bebidas** desde el menú por categorías
4. Paga el total (Tickets + Comida) con Stripe
5. Se generan tickets y QR
5. Los tickets aparecen en Mis Tickets

### Validación
1. Admin abre Escanear
2. Se solicita permiso de cámara
3. Escanea QR
4. Ticket queda en estado USADO

## 📄 Licencia

MIT
