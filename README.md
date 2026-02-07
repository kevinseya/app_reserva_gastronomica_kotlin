# 🎫 Ticketera Universitaria

Sistema completo para gestión y venta de tickets para eventos universitarios con app móvil y backend.

## 📁 Estructura del Proyecto

```
jwmaila/
├── backend/          # API REST con NestJS + PostgreSQL + Prisma
└── appTicketera/     # Aplicación móvil Android (Kotlin + Compose)
```

## 🚀 Backend (NestJS)

Backend RESTful con las siguientes características:

### ✨ Características Principales

- Autenticación JWT
- Roles de Usuario: ADMIN y CLIENT
- Gestión de eventos (CRUD)
- Sistema de asientos (10x10)
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

### 🎨 Paleta de colores

- DarkBlue: #3E5F8A
- MediumBlue: #5783BC
- LightBlue: #6A9CDE
- VeryLightBlue: #A0C5F7
- PastelBlue: #CADFFB
- LightBlueGray: #E8F1F8
- Gray: #9E9E9E
- LightGray: #F5F5F5
- ErrorRed: #D32F2F
- SuccessGreen: #388E3C
- WarningOrange: #F57C00

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
cd appTicketera
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
- Posición (fila, columna)
- Estado de ocupación
- Relación con evento

### Ticket
- Relación con usuario, evento y asiento
- Estados: PENDING, PAID, USED, CANCELLED
- QR único para validación
- ID de pago de Stripe

## 🎯 Flujos principales

### Compra
1. Cliente selecciona evento
2. Elige asientos
3. Paga con Stripe
4. Se generan tickets y QR
5. Los tickets aparecen en Mis Tickets

### Validación
1. Admin abre Escanear
2. Se solicita permiso de cámara
3. Escanea QR
4. Ticket queda en estado USADO

## 📄 Licencia

MIT
