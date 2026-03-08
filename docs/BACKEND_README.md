# QuimbayaEVAL Backend

Backend REST API para el sistema de gestión de evaluaciones académicas QuimbayaEVAL.

**Tecnologías**: Java 17 + Spring Boot 3.2 + PostgreSQL + JDBC puro + JWT

## 📋 Tabla de Contenidos

1. [Quick Start](#quick-start)
2. [Estructura del Proyecto](#estructura-del-proyecto)
3. [Configuración](#configuración)
4. [API Endpoints](#api-endpoints)
5. [Integración Frontend](#integración-frontend)
6. [Development](#development)
7. [Troubleshooting](#troubleshooting)

## 🚀 Quick Start

### Requisitos Previos

- Java 17 o superior
- Maven 3.8+
- PostgreSQL 12+
- Docker & Docker Compose (opcional pero recomendado)

### Opción 1: Con Docker Compose (Recomendado)

```bash
# Clonar y ejecutar
git clone <repo-url>
cd quimbayaeval-backend

# Iniciar todo (DB + Backend)
docker-compose up --build

# Backend disponible en: http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
```

### Opción 2: Local

```bash
# 1. Crear base de datos
createdb quimbayaeval

# 2. Cargar schema
psql -U postgres -d quimbayaeval < src/main/resources/db/schema.sql

# 3. Configurar variables de entorno
cp .env.example .env
# Editar .env con tus valores

# 4. Ejecutar backend
mvn spring-boot:run
```

## 📁 Estructura del Proyecto

```
src/main/java/com/quimbayaeval/
├── config/              # Configuración (Security, CORS, etc)
├── controller/          # REST Controllers
├── dao/                 # Data Access Objects (JDBC)
├── exception/           # Manejo global de excepciones
├── model/               # Entidades Java
│   └── dto/            # Data Transfer Objects
├── security/            # JWT y autenticación
├── service/             # Lógica de negocio
└── QuimbayaEvalBackendApplication.java

src/main/resources/
├── application.yml      # Configuración de propiedades
└── db/
    └── schema.sql      # Esquema SQL

root/
├── pom.xml             # Dependencias Maven
├── Dockerfile          # Para containerización
├── docker-compose.yml  # Stack local (DB + Backend)
└── INTEGRATION_GUIDE.md # Guía de integración Frontend
```

## ⚙️ Configuración

### Variables de Entorno

```java
// JWT Secret - CAMBIAR EN PRODUCCIÓN
JWT_SECRET=tu-clave-muy-larga-y-segura-minimo-256-bits
JWT_EXPIRATION=86400000  // 24 horas en ms

// PostgreSQL
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/quimbayaeval
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
```

Ver `.env.example` para todas las variables disponibles.

### Tabla de Autenticación

Para login, necesitas usuarios en tabla `users`:

```sql
INSERT INTO users (name, email, password, role, active) VALUES
('Juan Estudiante', 'juan@example.com', '$2a$10$...', 'estudiante', true),
('María Maestra', 'maria@example.com', '$2a$10$...', 'maestro', true),
('Carlos Coordinador', 'carlos@example.com', '$2a$10$...', 'coordinador', true);
```

**Nota**: Las contraseñas deben estar hasheadas con BCrypt. Para obtener hash:

```java
// Usando Spring Security
new BCryptPasswordEncoder().encode("password123")
// Resultado: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36P4/tvO

// Usar en INSERT:
INSERT INTO users (name, email, password, role) VALUES 
('Test', 'test@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36P4/tvO', 'estudiante');
```

## 📡 API Endpoints

### Parámetros de Paginación

Todos los endpoints GET que listan recursos soportan paginación, ordenación y filtrado:

```bash
# Paginación básica
GET /api/cursos?page=0&size=10

# Con ordenación
GET /api/cursos?page=0&size=10&sort=nombre&direction=ASC

# Parámetros:
# - page: número de página (0-indexado)
# - size: cantidad de registros por página
# - sort: nombre de columna para ordenar (opcional)
# - direction: ASC o DESC (por defecto: ASC)
```

### Autenticación

| Method | Endpoint | Body | Descripción |
|--------|----------|------|-------------|
| POST | `/api/auth/login` | `{email, password, role}` | Login de usuario |
| POST | `/api/auth/register` | `{email, password, role}` | Registrar usuario |
| GET | `/api/auth/validate` | - | Validar token JWT |

**Ejemplo Login**:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"juan@example.com","password":"password123","role":"estudiante"}'
```

**Respuesta**:
```json
{
  "success": true,
  "message": "Login exitoso",
  "data": {
    "token": "eyJhbGc...",
    "type": "Bearer",
    "id": 1,
    "name": "Juan",
    "email": "juan@example.com",
    "role": "estudiante"
  }
}
```

### Evaluaciones

| Method | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/evaluaciones` | Obtener todas |
| GET | `/api/evaluaciones/{id}` | Obtener por ID |
| GET | `/api/evaluaciones/curso/{cursoId}` | Por curso |
| GET | `/api/evaluaciones/estado/activas` | Solo activas |
| POST | `/api/evaluaciones` | Crear nueva |
| PUT | `/api/evaluaciones/{id}` | Actualizar |
| DELETE | `/api/evaluaciones/{id}` | Eliminar |
| POST | `/api/evaluaciones/{id}/publicar` | Publicar |
| POST | `/api/evaluaciones/{id}/submit` | Enviar respuestas |

### Cursos

| Method | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/cursos` | Obtener todos |
| GET | `/api/cursos/{id}` | Obtener por ID |
| GET | `/api/cursos/profesor/{profesorId}` | Del profesor |
| POST | `/api/cursos` | Crear nuevo |
| PUT | `/api/cursos/{id}` | Actualizar |
| DELETE | `/api/cursos/{id}` | Eliminar |

### PQRS

| Method | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/pqrs` | Obtener todos |
| GET | `/api/pqrs/{id}` | Obtener por ID |
| GET | `/api/pqrs/usuario/{usuarioId}` | Del usuario |
| GET | `/api/pqrs/estado/{estado}` | Por estado |
| POST | `/api/pqrs` | Crear nuevo |
| PUT | `/api/pqrs/{id}` | Actualizar |
| DELETE | `/api/pqrs/{id}` | Eliminar |
| POST | `/api/pqrs/{id}/respond` | Responder |

### Documentación Interactiva

Una vez que el backend esté corriendo:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## 🔗 Integración Frontend

Ver [INTEGRATION_GUIDE.md](./INTEGRATION_GUIDE.md) para detalles completos.

### Configuración Rápida en React

**1. Variables de entorno** (`.env`):
```env
VITE_API_BASE_URL=http://localhost:8080/api
```

**2. Actualizar AuthContext**:
```typescript
const login = async (email: string, password: string, role: UserRole) => {
  const res = await fetch(`${import.meta.env.VITE_API_BASE_URL}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password, role })
  });
  
  const { data } = await res.json();
  localStorage.setItem('token', data.token);
  setUser(data.user);
};
```

**3. Interceptor HTTP**:
```typescript
// Agregar header `Authorization: Bearer <token>` a todos los requests
```

Ver [INTEGRATION_GUIDE.md](./INTEGRATION_GUIDE.md#3-cambios-en-authcontext-del-frontend) para más ejemplos.

## 🛠️ Development

### Build

```bash
# Compilar sin tests
mvn clean package -DskipTests

# Compilar con tests
mvn clean package
```

### Tests

```bash
# Ejecutar tests
mvn test

# Coverage
mvn jacoco:report
```

### Logs

```bash
# Con Docker
docker-compose logs -f backend

# Local
tail -f target/*.log
```

### Agregar Nuevas Tablas

1. Editar `src/main/resources/db/schema.sql`
2. Crear entidad en `src/main/java/com/quimbayaeval/model/`
3. Crear DAO en `src/main/java/com/quimbayaeval/dao/`
4. Crear Servicio en `src/main/java/com/quimbayaeval/service/`
5. Crear Controlador en `src/main/java/com/quimbayaeval/controller/`

## ⚠️ Troubleshooting

### Error: Connection refused (PostgreSQL)

```
# Verificar que postgres está corriendo
docker-compose ps

# Reiniciar
docker-compose down && docker-compose up
```

### Error: CORS origin not allowed

- Editar `src/main/java/com/quimbayaeval/config/SecurityConfig.java`
- Agregar tu origen en `setAllowedOrigins(Arrays.asList(...))`

### Error: Token invalid or expired

- Token expira en 24 horas (configurable en application.yml)
- Necesitas re-login o implementar refresh tokens

### Puerto 8080 en uso

```bash
# Find process using port
lsof -i :8080  # o netstat -an | findstr :8080 en Windows

# Kill process
kill -9 <PID>
```

### No hay usuarios en BD

```bash
# Conectar a DB
docker-compose exec postgres psql -U postgres -d quimbayaeval

# Crear usuarios con hash bcrypt (hash: password)
INSERT INTO users (name, email, password, role, active) VALUES
('Test Student', 'student@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36P4/tvO', 'estudiante', true),
('Test Teacher', 'teacher@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36P4/tvO', 'maestro', true);
```

## 📝 Testing con Scripts

```bash
# Linux/Mac
bash test-api.sh

# Windows (PowerShell)
.\test-api.ps1
```

## 📚 Endpoints por Rol

### Estudiante
- Ver evaluaciones activas
- Responder evaluaciones
Ver mis evaluaciones
- Ver calificaciones
- Ver PQRS propio
- Crear PQRS

### Maestro
- Crear/editar evaluaciones
- Calificar evaluaciones
- Ver reportes
- Responder PQRS

### Coordinador
- Gestionar usuarios
- Ver reportes globales
- Administrar PQRS
- Configurar sistema

## 🔐 Seguridad

### JWT en Producción

1. **Cambiar JWT_SECRET**:
   ```bash
   # Generar clave aleatoria segura
   openssl rand -base64 256
   ```

2. **HTTPS**: Siempre usar en producción

3. **CORS**: Restringir a dominios conocidos

4. **Logs**: No guardar datos sensibles

5. **Rate Limiting**: Implementar en API Gateway

## 📞 Soporte

Para problemas o preguntas:

1. Ver [INTEGRATION_GUIDE.md](./INTEGRATION_GUIDE.md)
2. Revisar logs: `docker-compose logs`
3. Verificar Swagger: http://localhost:8080/swagger-ui.html
4. Ver script de test: `test-api.sh` o `test-api.ps1`

## 📄 Licencia

Proyecto QuimbayaEVAL 2024
