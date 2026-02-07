# 📖 Documentación de API - Ticketera Universitaria

## Autenticación

La API usa JWT (JSON Web Tokens). Después de login/registro, incluye el token en las peticiones:
```
Authorization: Bearer <tu_token>
```

---

## 🔐 Endpoints de Autenticación

### Registrar Usuario
**POST** `/auth/register`

**Body:**
```json
{
  "email": "usuario@example.com",
  "password": "password123",
  "firstName": "Juan",
  "lastName": "Pérez"
}
```

**Response 201:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "uuid",
    "email": "usuario@example.com",
    "firstName": "Juan",
    "lastName": "Pérez",
    "role": "CLIENT"
  }
}
```

---

### Iniciar Sesión
**POST** `/auth/login`

**Body:**
```json
{
  "email": "usuario@example.com",
  "password": "password123"
}
```

**Response 200:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "uuid",
    "email": "usuario@example.com",
    "firstName": "Juan",
    "lastName": "Pérez",
    "role": "CLIENT"
  }
}
```

---

### Obtener Perfil
**GET** `/auth/profile`

**Headers:**
```
Authorization: Bearer <token>
```

**Response 200:**
```json
{
  "id": "uuid",
  "email": "usuario@example.com",
  "firstName": "Juan",
  "lastName": "Pérez",
  "role": "CLIENT"
}
```

---

## 👥 Endpoints de Usuarios (Solo ADMIN)

### Listar Usuarios
**GET** `/users`

**Headers:** `Authorization: Bearer <admin_token>`

**Response 200:**
```json
[
  {
    "id": "uuid",
    "email": "usuario@example.com",
    "firstName": "Juan",
    "lastName": "Pérez",
    "role": "CLIENT",
    "createdAt": "2026-01-15T10:00:00.000Z",
    "updatedAt": "2026-01-15T10:00:00.000Z"
  }
]
```

---

### Obtener Usuario por ID
**GET** `/users/:id`

**Headers:** `Authorization: Bearer <admin_token>`

**Response 200:**
```json
{
  "id": "uuid",
  "email": "usuario@example.com",
  "firstName": "Juan",
  "lastName": "Pérez",
  "role": "CLIENT",
  "createdAt": "2026-01-15T10:00:00.000Z",
  "updatedAt": "2026-01-15T10:00:00.000Z",
  "tickets": []
}
```

---

### Actualizar Usuario
**PATCH** `/users/:id`

**Headers:** `Authorization: Bearer <admin_token>`

**Body:**
```json
{
  "firstName": "Carlos",
  "role": "ADMIN"
}
```

**Response 200:**
```json
{
  "id": "uuid",
  "email": "usuario@example.com",
  "firstName": "Carlos",
  "lastName": "Pérez",
  "role": "ADMIN",
  "createdAt": "2026-01-15T10:00:00.000Z",
  "updatedAt": "2026-01-15T12:00:00.000Z"
}
```

---

### Eliminar Usuario
**DELETE** `/users/:id`

**Headers:** `Authorization: Bearer <admin_token>`

**Response 200:**
```json
{
  "message": "Usuario eliminado exitosamente"
}
```

---

## 🎭 Endpoints de Eventos

### Crear Evento (Solo ADMIN)
**POST** `/events`

**Headers:** `Authorization: Bearer <admin_token>`

**Body:**
```json
{
  "name": "Concierto de Rock",
  "description": "Una noche increíble de rock",
  "date": "2026-03-15T20:00:00Z",
  "venue": "Auditorio Universitario",
  "imageUrl": "https://example.com/image.jpg",
  "ticketPrice": 25.00
}
```

**Response 201:**
```json
{
  "id": "uuid",
  "name": "Concierto de Rock",
  "description": "Una noche increíble de rock",
  "date": "2026-03-15T20:00:00.000Z",
  "venue": "Auditorio Universitario",
  "imageUrl": "https://example.com/image.jpg",
  "ticketPrice": 25.00,
  "totalSeats": 100,
  "createdAt": "2026-01-15T10:00:00.000Z",
  "updatedAt": "2026-01-15T10:00:00.000Z",
  "seats": [
    {
      "id": "uuid",
      "row": 1,
      "column": 1,
      "isOccupied": false
    }
    // ... 99 más
  ]
}
```

---

### Listar Eventos
**GET** `/events`

**Response 200:**
```json
[
  {
    "id": "uuid",
    "name": "Concierto de Rock",
    "description": "Una noche increíble de rock",
    "date": "2026-03-15T20:00:00.000Z",
    "venue": "Auditorio Universitario",
    "imageUrl": "https://example.com/image.jpg",
    "ticketPrice": 25.00,
    "totalSeats": 100,
    "createdAt": "2026-01-15T10:00:00.000Z",
    "updatedAt": "2026-01-15T10:00:00.000Z",
    "_count": {
      "seats": 95  // asientos disponibles
    }
  }
]
```

---

### Obtener Evento por ID
**GET** `/events/:id`

**Response 200:**
```json
{
  "id": "uuid",
  "name": "Concierto de Rock",
  "description": "Una noche increíble de rock",
  "date": "2026-03-15T20:00:00.000Z",
  "venue": "Auditorio Universitario",
  "imageUrl": "https://example.com/image.jpg",
  "ticketPrice": 25.00,
  "totalSeats": 100,
  "seats": [
    {
      "id": "uuid",
      "row": 1,
      "column": 1,
      "isOccupied": false
    }
    // ... todos los asientos
  ],
  "_count": {
    "tickets": 5
  }
}
```

---

### Obtener Asientos Disponibles
**GET** `/events/:id/seats`

**Response 200:**
```json
[
  {
    "id": "uuid",
    "eventId": "event-uuid",
    "row": 1,
    "column": 1,
    "isOccupied": false,
    "createdAt": "2026-01-15T10:00:00.000Z",
    "updatedAt": "2026-01-15T10:00:00.000Z"
  }
  // solo asientos disponibles
]
```

---

### Actualizar Evento (Solo ADMIN)
**PATCH** `/events/:id`

**Headers:** `Authorization: Bearer <admin_token>`

**Body:**
```json
{
  "ticketPrice": 30.00,
  "description": "Actualizado: Una noche épica de rock"
}
```

---

### Eliminar Evento (Solo ADMIN)
**DELETE** `/events/:id`

**Headers:** `Authorization: Bearer <admin_token>`

**Response 200:**
```json
{
  "message": "Evento eliminado exitosamente"
}
```

---

## 🎫 Endpoints de Tickets

### Crear Intención de Pago
**POST** `/tickets/create-payment-intent`

**Headers:** `Authorization: Bearer <token>`

**Body:**
```json
{
  "eventId": "event-uuid",
  "seatId": "seat-uuid"
}
```

**Response 201:**
```json
{
  "clientSecret": "pi_xxx_secret_xxx",
  "amount": 25.00
}
```

**Notas:**
- Usa `clientSecret` para completar el pago en el cliente con Stripe
- El asiento NO se reserva hasta confirmar el pago

---

### Confirmar Pago
**POST** `/tickets/confirm-payment/:paymentIntentId`

**Headers:** `Authorization: Bearer <token>`

**Response 201:**
```json
{
  "id": "ticket-uuid",
  "userId": "user-uuid",
  "eventId": "event-uuid",
  "seatId": "seat-uuid",
  "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANS...",
  "stripePaymentId": "pi_xxx",
  "status": "PAID",
  "purchaseDate": "2026-01-15T10:30:00.000Z",
  "event": {
    "id": "event-uuid",
    "name": "Concierto de Rock",
    "date": "2026-03-15T20:00:00.000Z",
    "venue": "Auditorio Universitario"
  },
  "seat": {
    "id": "seat-uuid",
    "row": 5,
    "column": 8,
    "isOccupied": true
  }
}
```

**Notas:**
- El QR se genera automáticamente
- El asiento se marca como ocupado
- El ticket se crea con estado PAID

---

### Mis Tickets
**GET** `/tickets/my-tickets`

**Headers:** `Authorization: Bearer <token>`

**Response 200:**
```json
[
  {
    "id": "ticket-uuid",
    "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANS...",
    "status": "PAID",
    "purchaseDate": "2026-01-15T10:30:00.000Z",
    "event": {
      "name": "Concierto de Rock",
      "date": "2026-03-15T20:00:00.000Z",
      "venue": "Auditorio Universitario"
    },
    "seat": {
      "row": 5,
      "column": 8
    }
  }
]
```

---

### Obtener Ticket por ID
**GET** `/tickets/:id`

**Headers:** `Authorization: Bearer <token>`

**Response 200:**
```json
{
  "id": "ticket-uuid",
  "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANS...",
  "status": "PAID",
  "purchaseDate": "2026-01-15T10:30:00.000Z",
  "event": {
    "name": "Concierto de Rock",
    "date": "2026-03-15T20:00:00.000Z"
  },
  "seat": {
    "row": 5,
    "column": 8
  },
  "user": {
    "firstName": "Juan",
    "lastName": "Pérez"
  }
}
```

---

### Verificar Ticket (Solo ADMIN)
**POST** `/tickets/verify`

**Headers:** `Authorization: Bearer <admin_token>`

**Body:**
```json
{
  "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANS..."
}
```

**Response 200 (Válido):**
```json
{
  "valid": true,
  "message": "Ticket válido",
  "ticket": {
    "id": "ticket-uuid",
    "status": "USED",
    "event": {
      "name": "Concierto de Rock"
    },
    "seat": {
      "row": 5,
      "column": 8
    },
    "user": {
      "firstName": "Juan",
      "lastName": "Pérez"
    }
  }
}
```

**Response 200 (Ya usado):**
```json
{
  "valid": false,
  "message": "Este ticket ya fue usado",
  "ticket": { ... }
}
```

**Notas:**
- Al verificar, el ticket se marca como USED
- No se puede usar el mismo ticket dos veces

---

### Listar Todos los Tickets (Solo ADMIN)
**GET** `/tickets`

**Headers:** `Authorization: Bearer <admin_token>`

**Response 200:**
```json
[
  {
    "id": "ticket-uuid",
    "status": "PAID",
    "purchaseDate": "2026-01-15T10:30:00.000Z",
    "event": { ... },
    "seat": { ... },
    "user": { ... }
  }
]
```

---

## ❌ Códigos de Error

- **400 Bad Request**: Datos inválidos
- **401 Unauthorized**: No autenticado o token inválido
- **403 Forbidden**: Sin permisos (requiere rol ADMIN)
- **404 Not Found**: Recurso no encontrado
- **409 Conflict**: Conflicto (ej: asiento ocupado, email duplicado)

**Ejemplo de Error:**
```json
{
  "statusCode": 409,
  "message": "Este asiento ya está ocupado",
  "error": "Conflict"
}
```

---

## 🔄 Estados de Ticket

- **PENDING**: Creado pero no pagado (no usado actualmente)
- **PAID**: Pagado exitosamente
- **USED**: Ticket escaneado y utilizado
- **CANCELLED**: Ticket cancelado (no implementado)

---

## 🧪 Testing con cURL

Ver el archivo [SETUP.md](SETUP.md) para ejemplos de comandos cURL.

---

## 📱 Integración con App Móvil

1. **Registro/Login**: Obtener y guardar `accessToken`
2. **Listar eventos**: Mostrar catálogo
3. **Seleccionar asiento**: Ver mapa de asientos
4. **Pagar**: 
   - Crear payment intent
   - Mostrar formulario de Stripe
   - Confirmar pago
5. **Ver tickets**: Mostrar QR para entrada
6. **Escanear QR** (Admin): Validar entrada

