# Inicio Rápido - PostgreSQL

Guía rápida para migrar de H2 a PostgreSQL en 5 minutos.

## Opción 1: Docker Compose (Más Fácil)

### 1. Iniciar PostgreSQL con Docker

```bash
docker-compose up -d postgres
```

### 2. Configurar perfil de producción

Edita `.env`:
```env
SPRING_PROFILES_ACTIVE=prod
```

### 3. Ejecutar aplicación

```bash
mvn spring-boot:run
```

Flyway ejecutará automáticamente las migraciones y creará las tablas con datos de prueba.

### 4. Verificar

- API: http://localhost:8080
- Base de datos: `localhost:5432`
- Usuario: `postgres` / Contraseña: `postgres`

## Opción 2: PostgreSQL Local

### 1. Instalar PostgreSQL

**Windows:**
```bash
choco install postgresql
```

**Linux:**
```bash
sudo apt install postgresql
```

**macOS:**
```bash
brew install postgresql
brew services start postgresql
```

### 2. Crear base de datos

Ejecuta el script automatizado:
```bash
.\scripts\setup-postgres.ps1
```

O manualmente:
```bash
psql -U postgres
CREATE DATABASE quimbayaeval;
CREATE USER quimbaya_user WITH PASSWORD 'quimbayaEval123';
GRANT ALL PRIVILEGES ON DATABASE quimbayaeval TO quimbaya_user;
\q
```

### 3. Configurar aplicación

Edita `.env`:
```env
DB_URL=jdbc:postgresql://localhost:5432/quimbayaeval
DB_USERNAME=quimbaya_user
DB_PASSWORD=quimbayaEval123
SPRING_PROFILES_ACTIVE=prod
```

### 4. Ejecutar aplicación

```bash
mvn spring-boot:run
```

## Usuarios de Prueba

Después de la migración, tendrás estos usuarios disponibles:

| Email | Contraseña | Rol |
|-------|-----------|-----|
| profesor@quimbaya.edu | password123 | maestro |
| ana.martinez@quimbaya.edu | password123 | maestro |
| estudiante@quimbaya.edu | password123 | estudiante |
| maria.garcia@quimbaya.edu | password123 | estudiante |
| juan.perez@quimbaya.edu | password123 | estudiante |

## Datos de Prueba Incluidos

- ✅ 6 usuarios (2 profesores, 4 estudiantes)
- ✅ 4 cursos
- ✅ 5 evaluaciones
- ✅ 10 preguntas
- ✅ 3 submissions (entregas)
- ✅ 1 calificación
- ✅ 4 PQRS

## Verificar Migración

### Probar login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"profesor@quimbaya.edu","password":"password123","role":"maestro"}'
```

### Ver cursos
```bash
curl http://localhost:8080/api/cursos
```

## Perfiles Disponibles

- `dev` (default): Usa H2 en memoria para desarrollo
- `prod`: Usa PostgreSQL para producción
- `test`: Usa H2 para tests automatizados

## Comandos Útiles

### Ver logs de PostgreSQL (Docker)
```bash
docker-compose logs -f postgres
```

### Conectar a PostgreSQL (Docker)
```bash
docker exec -it quimbayaeval-db psql -U postgres -d quimbayaeval
```

### Detener PostgreSQL (Docker)
```bash
docker-compose down
```

### Eliminar datos y reiniciar (Docker)
```bash
docker-compose down -v
docker-compose up -d postgres
```

## Troubleshooting

### Error: "Connection refused"
- Verifica que PostgreSQL esté corriendo: `docker-compose ps`
- Verifica el puerto: `netstat -an | findstr 5432`

### Error: "Database does not exist"
- Ejecuta: `docker-compose down -v && docker-compose up -d postgres`

### Error: "Authentication failed"
- Verifica credenciales en `.env`
- Verifica que coincidan con `application-prod.yml`

### Flyway no ejecuta migraciones
- Verifica que los archivos estén en `src/main/resources/db/migration/`
- Verifica nombres: `V1__initial_schema.sql`, `V2__seed_data.sql`
- Revisa logs: `mvn spring-boot:run -X`

## Siguiente Paso

Lee la [Guía Completa de Migración](DATABASE_MIGRATION.md) para configuración avanzada.
