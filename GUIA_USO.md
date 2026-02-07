# 🎫 App Ticketera - Guía de Uso

## 📱 Credenciales de Acceso

### 👨‍💼 Administrador
- **Email**: `admin@ticketera.com`
- **Contraseña**: `admin123`
- **Rol**: ADMIN

### 👤 Cliente
- **Email**: `cliente@test.com`
- **Contraseña**: `cliente123`
- **Rol**: CLIENT

---

## ✨ Funcionalidades por Rol

### 🔵 CLIENTE
El cliente tiene acceso a:

#### **Header (Menú superior)**
- 🎫 **Eventos**: Ver lista de eventos disponibles
- 🎟️ **Mis Tickets**: Ver tickets comprados
- 👤 **Perfil**: Ver y editar perfil, cerrar sesión

#### **Pantallas disponibles**
1. **Eventos**: 
   - 🔍 Buscador de eventos (por nombre, lugar, descripción)
   - Lista de eventos con imagen, fecha, lugar y precio
   - Ver detalles del evento

2. **Detalle de Evento**:
   - Seleccionar asientos disponibles
   - Comprar tickets con Stripe Payment Sheet
   - Ver información completa del evento

3. **Mis Tickets**:
   - Ver todos los tickets comprados
   - Ver código QR de cada ticket
   - Detalles del evento y asiento

4. **Perfil**:
   - Ver información personal
   - Badge con rol "Cliente"
   - **Botón Cerrar Sesión** (rojo, al final)

#### **Bottom Navigation**
- 🎫 Eventos
- 🎟️ Mis Tickets
- 👤 Perfil

---

### 🔴 ADMINISTRADOR
El administrador tiene todas las funciones del cliente MÁS:

#### **Header (Menú superior adicional)**
- ⚙️ **Gestionar Eventos**: CRUD completo de eventos
- 👥 **Gestionar Usuarios**: Ver usuarios registrados
- 📊 **Panel Admin**: Estadísticas y dashboard

#### **Panel de Administración**
- **Badge de sesión**: Muestra nombre y rol "ADMIN"
- **Estadísticas en tiempo real**:
  - 📊 Total de eventos
  - 👥 Total de usuarios
  - 🎟️ Total de tickets vendidos
  - 💰 Ingresos totales
- **Acciones rápidas**:
  - ➕ Crear Evento
  - 👥 Gestionar Usuarios
  - 📈 Reportes
  - ⚙️ Configuración

#### **Gestión de Eventos** (CRUD Completo)
1. **Ver todos los eventos**:
   - Lista con botones editar ✏️ y eliminar 🗑️
   - Botón flotante ➕ para crear nuevo evento

2. **Crear Evento**:
   - Nombre *
   - Descripción
   - Fecha (YYYY-MM-DD) *
   - Lugar *
   - Precio *
   - Total de asientos *
   - URL de imagen

3. **Editar Evento**:
   - Misma pantalla que crear
   - Campos pre-llenados
   - Guardar cambios

4. **Eliminar Evento**:
   - Diálogo de confirmación
   - Eliminación permanente

#### **Bottom Navigation (adicional)**
- 🎫 Eventos
- 🎟️ Mis Tickets
- 👨‍💼 **Admin** (Panel de administración)
- 🔍 **Escanear** (Escanear QR de tickets)
- 👤 Perfil

---

## 🔄 Sistema de Roles

### Funcionamiento Automático
1. Al hacer **login**, el backend envía el rol (ADMIN o CLIENT)
2. Se guarda en `UserPreferences` localmente
3. La app detecta automáticamente el rol y muestra:
   - **Menú superior** (AppTopBar) con opciones según rol
   - **Bottom navigation** con tabs según rol
   - **Estadísticas reales** en panel admin

### Validación de Roles
- Solo usuarios ADMIN ven:
  - Tab "Admin" en bottom nav
  - Tab "Escanear" en bottom nav
  - Opciones de gestión en header
  - Estadísticas del sistema

- Usuarios CLIENT solo ven:
  - Opciones básicas de cliente
  - Sus propios tickets
  - Perfil personal

---

## 🎨 Características de UI

### Buscador de Eventos
- 🔍 Campo de búsqueda en la parte superior
- Filtra en tiempo real por:
  - Nombre del evento
  - Lugar/venue
  - Descripción
- ❌ Botón para limpiar búsqueda
- 📭 Mensaje cuando no hay resultados

### Header Inteligente (AppTopBar)
- 🏷️ **Badge de rol**: Muestra si eres "Admin" o "Cliente"
- ⋮ **Menú contextual**: Opciones diferentes según tu rol
- 🎨 Colores distintivos:
  - Admin: Azul pastel
  - Cliente: Azul muy claro

### Perfil
- 👤 Avatar con inicial del nombre
- 📧 Email del usuario
- 🏷️ Badge con rol
- 🔴 **Botón Cerrar Sesión**: Al final de la pantalla

---

## 🔐 Seguridad

### JWT Automático
- Token se guarda al hacer login
- `AuthInterceptor` lo agrega automáticamente a todas las peticiones
- Endpoints públicos (sin token):
  - POST /auth/login
  - POST /auth/register
  - GET /events

- Endpoints protegidos (con token):
  - Todos los demás

### Logout
- Limpia completamente `UserPreferences`
- Elimina token de sesión
- Redirige a pantalla de login
- No deja rastro de sesión anterior

---

## 🚀 Flujo de Uso

### Como Cliente
1. Login con `cliente@test.com` / `cliente123`
2. Ver eventos disponibles
3. Buscar evento específico
4. Seleccionar asientos
5. Pagar con Stripe
6. Ver ticket en "Mis Tickets"
7. Cerrar sesión desde Perfil

### Como Administrador
1. Login con `admin@ticketera.com` / `admin123`
2. Ver estadísticas en Panel Admin
3. Ir a "Gestionar Eventos"
4. Crear nuevo evento
5. Ver lista de todos los eventos
6. Editar o eliminar eventos existentes
7. Escanear QR de tickets vendidos
8. Ver perfil y cerrar sesión

---

## 📝 Notas Importantes

1. **Backend debe estar corriendo**: `npm run start:dev` en `d:\jwmaila\backend`
2. **Seed de datos**: Si no hay eventos, ejecutar `npx prisma db seed`
3. **Base URL**: App usa `http://10.0.2.2:3000` para emulador
4. **Stripe**: Usa claves de prueba configuradas en backend
5. **Roles**: Se asignan automáticamente al crear usuario en seed

---

## 🎯 Resumen de lo Implementado

✅ AppTopBar con menú dinámico según rol  
✅ Buscador de eventos con filtrado en tiempo real  
✅ CRUD completo de eventos para admin  
✅ Panel admin con estadísticas reales  
✅ Distinción automática de roles ADMIN/CLIENT  
✅ Cierre de sesión funcional  
✅ JWT automático con AuthInterceptor  
✅ Bottom navigation adaptable por rol  
✅ Perfil con información de usuario y rol  

---

🎉 **¡La app está completamente funcional!**
