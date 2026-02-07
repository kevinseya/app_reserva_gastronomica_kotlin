# 🚀 Comandos Rápidos

## Instalación Inicial

```bash
# 1. Navegar al backend
cd backend

# 2. Instalar dependencias
pnpm install

# 3. Copiar variables de entorno
cp .env.example .env

# 4. Editar .env con tus credenciales
# Asegúrate de configurar:
# - DATABASE_URL (PostgreSQL)
# - JWT_SECRET (genera uno aleatorio)
# - STRIPE_SECRET_KEY (obtener de https://stripe.com)
```

## Base de Datos

```bash
# Crear base de datos (en psql o pgAdmin)
createdb ticketera

# O manualmente:
psql -U postgres
CREATE DATABASE ticketera;
\q

# Ejecutar migraciones
pnpm prisma migrate dev --name init

# Generar cliente Prisma
pnpm prisma generate

# Poblar datos de prueba
pnpm run prisma:seed

# Abrir Prisma Studio (GUI)
pnpm prisma studio
```

## Desarrollo

```bash
# Iniciar servidor en modo desarrollo
pnpm run start:dev

# El servidor estará en http://localhost:3000
```

## Testing de API

```bash
# Registrar usuario
curl -X POST http://localhost:3000/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'

# Login (guarda el accessToken)
curl -X POST http://localhost:3000/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@ticketera.com",
    "password": "admin123"
  }'

# Listar eventos
curl http://localhost:3000/events

# Ver perfil (reemplaza TOKEN)
curl http://localhost:3000/auth/profile \
  -H "Authorization: Bearer TOKEN"
```

## Producción

```bash
# Compilar
pnpm run build

# Ejecutar en producción
pnpm run start:prod
```

## Prisma

```bash
# Crear nueva migración
pnpm prisma migrate dev --name nombre_de_la_migracion

# Aplicar migraciones en producción
pnpm prisma migrate deploy

# Resetear base de datos (¡CUIDADO! Borra todo)
pnpm prisma migrate reset

# Generar cliente después de cambios en schema
pnpm prisma generate

# Formatear schema.prisma
pnpm prisma format

# Ver estado de migraciones
pnpm prisma migrate status
```

## Git

```bash
# Inicializar repositorio (desde la raíz)
cd d:\jwmaila
git init
git add .
git commit -m "Initial commit: Backend completo"

# Conectar con GitHub (opcional)
git remote add origin https://github.com/tu-usuario/ticketera.git
git push -u origin main
```

## Solución de Problemas

```bash
# Error: Cannot find module '@prisma/client'
pnpm prisma generate

# Error: Database connection failed
# Verificar que PostgreSQL esté corriendo
# Verificar DATABASE_URL en .env

# Reinstalar dependencias
rm -rf node_modules
rm pnpm-lock.yaml
pnpm install

# Limpiar y reconstruir
pnpm run build
```

## Generar Secreto JWT

```bash
# En Node.js (en la terminal de Node)
node -e "console.log(require('crypto').randomBytes(32).toString('hex'))"

# O en PowerShell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
```

## Credenciales de Prueba

Después de ejecutar `pnpm run prisma:seed`:

**Admin:**
- Email: `admin@ticketera.com`
- Password: `admin123`

**Cliente:**
- Email: `cliente@test.com`
- Password: `cliente123`

## Estructura de Carpetas para App Móvil

```bash
# La estructura final será:
jwmaila/
├── backend/     # ✅ COMPLETADO
└── app/         # Crear próximamente para la app Kotlin
```

## Configurar Stripe

1. Crear cuenta en https://stripe.com
2. Ir a Dashboard → Developers → API keys
3. Copiar "Secret key" (empieza con `sk_test_`)
4. Pegar en `.env` como `STRIPE_SECRET_KEY`
5. Para webhooks (opcional): Developers → Webhooks

## Variables de Entorno Importantes

```env
# Base de datos PostgreSQL
DATABASE_URL="postgresql://usuario:password@localhost:5432/ticketera?schema=public"

# JWT (generar uno aleatorio)
JWT_SECRET="tu-secreto-super-seguro-aqui"
JWT_EXPIRATION="7d"

# Stripe (obtener de https://stripe.com)
STRIPE_SECRET_KEY="sk_test_tu_clave_aqui"
STRIPE_WEBHOOK_SECRET="whsec_tu_secreto_aqui"

# App
PORT=3000
NODE_ENV=development
```

## Próximos Pasos

1. ✅ Backend completado
2. 🔄 Configurar PostgreSQL local
3. 🔄 Obtener claves de Stripe
4. 🔄 Ejecutar migraciones
5. 🔄 Iniciar servidor
6. 🔄 Probar endpoints
7. ⏳ Crear app móvil en Kotlin
8. ⏳ Integrar app con backend

---

**¿Necesitas ayuda?** Revisa [API_DOCS.md](API_DOCS.md) para ejemplos detallados de cada endpoint.
