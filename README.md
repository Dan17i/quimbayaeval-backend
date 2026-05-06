# QuimbayaEVAL — Backend

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![Tests](https://img.shields.io/badge/Tests-214%20passing-success.svg)](#testing)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

REST API para gestión de evaluaciones académicas. Soporta autenticación JWT, autorización por roles, gestión de cursos con inscripciones, calificaciones, resultados con escala 1-5 y sistema PQRS.

---

## Tabla de Contenidos

- [Inicio Rápido](#inicio-rápido)
- [Requisitos](#requisitos)
- [Credenciales de Prueba](#credenciales-de-prueba)
- [Endpoints](#endpoints)
- [Arquitectura](#arquitectura)
- [Base de Datos](#base-de-datos)
- [Testing](#testing)
- [Configuración](#configuración)
- [Stack Tecnológico](#stack-tecnológico)
- [Documentación](#documentación)

---

## Inicio Rápido

### Primera vez

```powershell
# 1. Levantar PostgreSQL en Docker (puerto 5433)
docker-compose up -d

# 2. Arrancar Spring Boot — Flyway aplica schema y seed automáticamente
mvn spring-boot:run
```

Backend disponible en `http://localhost:8080`

### Volver a correr (después de Ctrl+C)

```powershell
# Verificar que Docker esté corriendo
docker ps

# Si el contenedor quimbayaeval-db no aparece:
docker-compose up -d

# Arrancar
mvn spring-boot:run
```

### Puerto ocupado

Si el puerto 8080 está en uso por otro proyecto:

```powershell
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

---

## Requisitos

| Herramienta | Versión mínima |
|-------------|---------------|
| Java | 17 |
| Maven | 3.8 |
| Docker | Cualquier versión reciente |

> PostgreSQL corre en Docker en el puerto **5433** para no interferir con instalaciones locales en el 5432.

---

## Credenciales de Prueba

Todos los usuarios tienen contraseña `password`.

| Nombre | Email | Rol |
|--------|-------|-----|
| Admin Sistema | admin@quimbaya.edu.co | coordinador |
| María Profesora | profesor@quimbaya.edu.co | maestro |
| Ana Martínez | ana.martinez@quimbaya.edu.co | maestro |
| Juan Estudiante | estudiante@quimbaya.edu.co | estudiante |
| María García | maria.garcia@quimbaya.edu.co | estudiante |
| Pedro Pérez | pedro.perez@quimbaya.edu.co | estudiante |
| Carlos López | carlos.lopez@quimbaya.edu.co | estudiante |

Ejemplo de login:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"estudiante@quimbaya.edu.co","password":"password","role":"estudiante"}'
```

> El campo `role` es obligatorio en el login.

---

## Endpoints

### Autenticación — Públicos

```
POST /api/auth/login      Body: { email, password, role }
POST /api/auth/register   Body: { name, email, password, role }
```

### Usuarios

```
GET    /api/users                        Lista usuarios activos (?role=maestro|estudiante|coordinador)
GET    /api/users/me                     Perfil propio + cursos según rol (desde JWT)
PUT    /api/users/me                     Editar nombre y fotoUrl
PUT    /api/users/me/password            Cambiar contraseña (verifica passwordActual)
PATCH  /api/users/{id}/status            Activar/bloquear — body: { "status": "activo"|"bloqueado" }
DELETE /api/users/{id}                   Soft delete
```

> `GET /api/users/me` retorna el campo `cursos`: para estudiantes son los cursos inscritos, para maestros los cursos asignados.

### Cursos e Inscripciones

```
GET    /api/cursos                              Lista todos los cursos
GET    /api/cursos/{id}                         Detalle de un curso
GET    /api/cursos/profesor/{profesorId}        Cursos de un docente
POST   /api/cursos                              Crear curso — body: { codigo, nombre, descripcion, profesorId }
PUT    /api/cursos/{id}                         Actualizar curso
DELETE /api/cursos/{id}                         Eliminar curso

GET    /api/cursos/{id}/estudiantes             Estudiantes matriculados
POST   /api/cursos/{id}/estudiantes             Matricular — body: { "estudianteId": 4 }
DELETE /api/cursos/{id}/estudiantes/{eId}       Desmatricular
```

### Evaluaciones

```
GET    /api/evaluaciones                        Lista con filtros opcionales
GET    /api/evaluaciones/{id}                   Detalle
GET    /api/evaluaciones/curso/{cursoId}        Por curso
GET    /api/evaluaciones/estado/activas         Solo activas y publicadas
POST   /api/evaluaciones                        Crear (maestro/coordinador)
POST   /api/evaluaciones/{id}/publicar          Publicar (estado → Activa)
PUT    /api/evaluaciones/{id}                   Actualizar
DELETE /api/evaluaciones/{id}                   Eliminar
```

Filtros disponibles en `GET /api/evaluaciones`:
`?profesorId=`, `?cursoId=`, `?estado=`, `?tipo=`, `?publicada=true`, `?page=`, `?size=`, `?sort=`

### Preguntas

```
GET    /api/preguntas/evaluacion/{id}    Preguntas de una evaluación
POST   /api/preguntas                    Crear pregunta
PUT    /api/preguntas/{id}               Actualizar
DELETE /api/preguntas/{id}               Eliminar
```

> `opcionesJson` llega como string JSON serializado. El frontend debe hacer `JSON.parse(pregunta.opcionesJson)`.

### Submissions

```
GET    /api/submissions/evaluacion/{id}  Lista submissions de una evaluación (para calificar)
POST   /api/submissions                  Enviar respuestas (estudiante presenta)
```

### Calificaciones

```
GET    /api/calificaciones/submission/{id}   Calificaciones de una submission
POST   /api/calificaciones                   Calificar — calificadoPorId se extrae del JWT
PUT    /api/calificaciones/{id}              Actualizar calificación
```

### Resultados

```
GET    /api/resultados/mis-resultados           Historial del estudiante autenticado (desde JWT)
GET    /api/resultados/evaluacion/{id}          Resultados de una evaluación
GET    /api/resultados/submission/{id}          Resultado de una submission
GET    /api/resultados/curso/{id}               Notas de todos los estudiantes del curso (docente)
GET    /api/resultados/curso/{id}/resumen       Promedio grupal por evaluación (coordinador)
```

Respuesta de `mis-resultados` incluye: `evaluacionNombre`, `cursoNombre`, `profesorNombre`, `porcentaje`, `notaEscala`, `estadoAprobacion`, `createdAt`.

> **Escala de notas colombiana**: `notaEscala = 1 + (porcentaje / 100) × 4`

### PQRS

```
GET    /api/pqrs                         Lista todos
GET    /api/pqrs/{id}                    Por ID
GET    /api/pqrs/mis-pqrs                PQRS del usuario autenticado (desde JWT)
GET    /api/pqrs/estado/{estado}         Por estado: Pendiente | En Proceso | Resuelta | Cerrada
POST   /api/pqrs                         Crear — usuarioId se extrae del JWT
PUT    /api/pqrs/{id}                    Responder/actualizar estado
DELETE /api/pqrs/{id}                    Eliminar
```

Tipos válidos: `Petición`, `Queja`, `Reclamo`, `Sugerencia`, `Pregunta`

---

## Arquitectura

```
src/main/java/com/quimbayaeval/
├── controller/      REST endpoints — reciben requests, delegan a services
├── service/         Lógica de negocio
├── dao/             Acceso a datos con JDBC (JdbcTemplate)
├── repository/      Repositorios JPA (UserRepository, CursoRepository, etc.)
├── model/
│   ├── entity/      Entidades JPA (UserEntity, CursoEntity, EvaluacionEntity, PQRSEntity)
│   ├── dto/         DTOs de respuesta (ApiResponse, MiResultadoDTO, ResultadoDetalleDTO, etc.)
│   └── dto/request/ DTOs de entrada (LoginRequestDTO, CrearEvaluacionRequestDTO, etc.)
├── security/        JWT (JwtTokenProvider, JwtAuthenticationFilter, JwtUserDetails)
├── config/          SecurityConfig, CacheConfig, MetricsConfig, RateLimitingInterceptor
└── exception/       GlobalExceptionHandler, excepciones personalizadas
```

El proyecto usa una arquitectura híbrida: JPA para entidades con relaciones (users, cursos, evaluaciones, pqrs) y JDBC directo para queries complejas con JOINs (resultados, calificaciones, inscripciones).

Los controllers extraen el ID del usuario autenticado desde el JWT via `authentication.getDetails()` — nunca confían en el `id` que envíe el frontend.

---

## Base de Datos

PostgreSQL 15 en Docker, puerto **5433**.

### Migraciones Flyway

| Versión | Descripción |
|---------|-------------|
| V1 | Schema completo + seed data (usuarios, cursos, evaluaciones, preguntas, submissions, resultados, PQRS) |
| V2 | Agrega columna `foto_url` a la tabla `users` |

### Tablas principales

```
users           — id, name, email, password, role, active, foto_url
cursos          — id, codigo, nombre, descripcion, profesor_id
inscripciones   — estudiante_id, curso_id (UNIQUE)
evaluaciones    — id, nombre, curso_id, profesor_id, tipo, estado, publicada
preguntas       — id, evaluacion_id, enunciado, tipo, puntuacion, opciones_json
submissions     — id, evaluacion_id, estudiante_id, respuestas_json, estado
calificaciones  — id, submission_id, pregunta_id, puntuacion_obtenida, calificado_por_id
resultados      — id, submission_id, puntuacion_total, porcentaje, estado_aprobacion
pqrs            — id, tipo, asunto, descripcion, usuario_id, estado, respuesta
```

### Resetear la base de datos

```powershell
docker-compose down -v
docker-compose up -d
mvn spring-boot:run
```

Si Flyway da error de checksum:

```powershell
docker exec quimbayaeval-db psql -U postgres -d quimbayaeval -c "UPDATE flyway_schema_history SET checksum = -1725541601 WHERE version = '1';"
```

---

## Testing

Los tests usan **H2 en memoria** — no necesitan Docker ni PostgreSQL.

```powershell
# Caso normal (variable de entorno no seteada)
mvn test

# Si corriste el backend antes y seteaste SPRING_DATASOURCE_URL en la sesión
Remove-Item Env:SPRING_DATASOURCE_URL -ErrorAction SilentlyContinue
mvn test
```

> Las variables de entorno tienen prioridad sobre `application.yml`. Si `SPRING_DATASOURCE_URL` está seteada, Spring Boot intenta conectarse a PostgreSQL en vez de H2.

Para verificar si está seteada:

```powershell
echo $env:SPRING_DATASOURCE_URL
# Si no imprime nada, mvn test funciona directo
```

**Cobertura actual**: 214 tests — 0 failures, 0 errors.

Incluye tests de integración para todos los controllers, tests unitarios para services y DAOs, y tests de validación de DTOs.

---

## Configuración

### Variables de entorno

El `application.yml` tiene valores por defecto funcionales. Solo necesitas setear variables si quieres sobreescribir algo.

```env
# Base de datos (default: localhost:5433)
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/quimbayaeval
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# JWT (default definido en application.yml)
JWT_SECRET=tu-secret-de-al-menos-256-bits

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
```

### Monitoreo

```bash
# Health check
curl http://localhost:8080/actuator/health

# Métricas Prometheus
curl http://localhost:8080/actuator/prometheus
```

---

## Stack Tecnológico

| Categoría | Tecnología |
|-----------|-----------|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.2.2 |
| Seguridad | Spring Security + JWT (JJWT 0.12.3) + BCrypt |
| Persistencia | Spring Data JPA + Hibernate + JdbcTemplate |
| Base de datos | PostgreSQL 15 (producción) / H2 (tests) |
| Migraciones | Flyway |
| Validación | Bean Validation (Jakarta) |
| Métricas | Spring Actuator + Micrometer + Prometheus |
| Caché | Spring Cache (ConcurrentMapCache) |
| Rate limiting | Bucket4j |
| Build | Maven |
| Utilidades | Lombok |
| Contenedores | Docker + Docker Compose |

---

## Documentación

| Archivo | Contenido |
|---------|-----------|
| [SETUP.md](SETUP.md) | Guía detallada de instalación, configuración y solución de problemas |
| [API.md](API.md) | Referencia completa de endpoints con ejemplos de request/response |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Decisiones técnicas, patrones de diseño y diagramas |
| [CREDENCIALES.md](CREDENCIALES.md) | Usuarios de prueba y flujos de ejemplo |

---

## Estructura del Proyecto

```
quimbayaeval-backend/
├── src/
│   ├── main/
│   │   ├── java/com/quimbayaeval/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dao/
│   │   │   ├── exception/
│   │   │   ├── mapper/
│   │   │   ├── model/
│   │   │   │   ├── dto/
│   │   │   │   │   └── request/
│   │   │   │   └── entity/
│   │   │   ├── repository/
│   │   │   ├── security/
│   │   │   └── service/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/
│   │           ├── V1__initial_schema.sql
│   │           └── V2__add_foto_url_to_users.sql
│   └── test/
│       ├── java/com/quimbayaeval/
│       └── resources/
│           ├── application.yml
│           └── db/schema.sql
├── scripts/                  # Scripts PowerShell de utilidad
├── docker-compose.yml
├── pom.xml
└── .env.example
```

---

## Licencia

MIT — ver [LICENSE](LICENSE) para detalles.

---

**Versión**: 1.2.0 · **Estado**: En Desarrollo · **Última actualización**: Mayo 2026
