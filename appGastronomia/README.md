# Ticketera - App Móvil Android

Aplicación móvil de venta de tickets para eventos artísticos, desarrollada con **Kotlin** y **Jetpack Compose**.

## 🎨 Diseño

La app utiliza una paleta de colores personalizada en tonos azules:
- **Dark Blue** (#3E5F8A) - Principal
- **Medium Blue** (#5783BC) - Primario
- **Light Blue** (#6A9CDE) - Secundario
- **Very Light Blue** (#A0C5F7) - Acento
- **Pastel Blue** (#CADFFB) - Fondo

El diseño está inspirado en React Native con Tailwind, adaptado a Material Design 3 con Jetpack Compose.

## 🏗️ Arquitectura

La aplicación sigue el patrón **MVVM (Model-View-ViewModel)** con las siguientes capas:

### Data Layer
- **Models**: Clases de datos serializables (User, Event, Ticket, Seat)
- **API Service**: Retrofit con Kotlinx Serialization para comunicación con el backend
- **Repository**: Maneja la lógica de negocio y las llamadas a la API
- **Local Storage**: SharedPreferences para guardar tokens y datos del usuario

### Domain Layer
- ViewModels para cada pantalla con manejo de estado usando StateFlow

### Presentation Layer
- Jetpack Compose UI con Material Design 3
- Navegación con Navigation Compose
- Tema personalizado con los colores de la marca

## 🛠️ Tecnologías

- **Kotlin** 2.0.21
- **Jetpack Compose** - UI moderna y declarativa
- **Hilt** - Inyección de dependencias
- **Retrofit** - Cliente HTTP
- **Kotlinx Serialization** - Serialización JSON
- **Coil** - Carga de imágenes
- **Navigation Compose** - Navegación entre pantallas
- **Stripe Android SDK** - Procesamiento de pagos
- **ZXing** - Escaneo y generación de códigos QR

## 📱 Funcionalidades

### Para Clientes
- ✅ Registro e inicio de sesión
- ✅ Navegación por eventos disponibles
- ✅ Visualización de detalles del evento
- ✅ Selección de asientos (matriz 10x10)
- ✅ **Pago con Stripe Payment Sheet (Integrado)**
- ✅ Visualización de tickets con QR
- ✅ Perfil de usuario

### Para Administradores
- ✅ Panel de administración
- ✅ Estadísticas de eventos y ventas
- ✅ Escaneo de códigos QR para validar tickets
- ✅ Gestión de eventos (crear, editar, eliminar)

## 🔧 Configuración

### Backend API

La app se conecta al backend NestJS en:
- **Emulador**: `http://10.0.2.2:3000`
- **Dispositivo físico**: `http://TU_IP:3000`

Modificar la URL en `NetworkModule.kt` según sea necesario.

### Stripe (Integración Completa)

La app integra **Stripe Payment Sheet** para pagos seguros:

**Claves configuradas:**
- **Publishable Key** (App): Ya configurado en `StripeConfig.kt`
- **Secret Key** (Backend): Ya configurado en backend `.env`

**Flujo de pago:**
1. Usuario selecciona asientos → Clic en "Continuar"
2. App crea Payment Intent en backend
3. Stripe muestra UI nativa con formulario de tarjeta
4. Usuario ingresa datos (seguros, nunca tocan nuestra app)
5. Stripe procesa el pago
6. Backend genera tickets con QR codes

**Tarjetas de prueba:**
```
✅ Exitosa: 4242 4242 4242 4242
❌ Rechazada: 4000 0000 0000 0002
Fecha: Cualquier futura
CVV: Cualquier 3 dígitos
```

Ver [STRIPE_INTEGRATION.md](./STRIPE_INTEGRATION.md) para documentación completa.

## 📦 Instalación

1. Clonar el repositorio
2. Abrir el proyecto en Android Studio
3. Sincronizar Gradle
4. Conectar un dispositivo o iniciar un emulador
5. Ejecutar la aplicación

## 🚀 Ejecución

```bash
# Compilar y ejecutar
./gradlew assembleDebug
./gradlew installDebug

# O desde Android Studio
Run > Run 'app'
```

## 📂 Estructura del Proyecto

```
app/
├── data/
│   ├── di/           # Módulos de Hilt
│   ├── local/        # SharedPreferences
│   ├── model/        # Modelos de datos
│   ├── remote/       # API service
│   └── repository/   # Repositorio
├── navigation/       # Navegación
├── ui/
│   ├── screens/      # Pantallas de la app
│   │   ├── auth/     # Login, Register
│   │   ├── events/   # Lista y detalle de eventos
│   │   ├── tickets/  # Mis tickets
│   │   ├── profile/  # Perfil de usuario
│   │   ├── admin/    # Panel admin
│   │   └── qr/       # Escaneo QR
│   └── theme/        # Tema y colores
└── MainActivity.kt   # Actividad principal
```

## 🎯 Próximos Pasos

1. ~~**Integración completa de Stripe**~~ ✅ **COMPLETADO**
2. **Escaneo QR real**: Integrar la cámara con ZXing
3. **Generación de QR**: Mostrar códigos QR reales en los tickets
4. **Caché local**: Room database para funcionamiento offline
5. **Notificaciones**: Push notifications para eventos y recordatorios
6. **Internacionalización**: Soporte para múltiples idiomas

## 🔐 Credenciales de Prueba

### Admin
- Email: `admin@ticketera.com`
- Password: `admin123`

### Cliente
- Email: `cliente@test.com`
- Password: `cliente123`

## 📝 Notas

- La app requiere Android 7.0 (API 24) o superior
- Se recomienda usar un emulador con Google Play Services para Stripe
- El backend debe estar corriendo en `localhost:3000` (o la IP configurada)

## 👤 Autor

Desarrollado para el sistema de ticketera universitaria.

## 📄 Licencia

Este proyecto es parte de un trabajo universitario.
