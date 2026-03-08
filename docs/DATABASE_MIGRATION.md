# Guía de Migración de Base de Datos

## Estado Actual

El proyecto actualmente usa **H2** (base de datos en memoria) que es ideal para:
- ✅ Desarrollo rápido
- ✅ Testing automatizado
- ✅ No requiere instalación externa
- ❌ Los datos se pierden al reiniciar
- ❌ No es adecuada para producción

## Migración a PostgreSQL (Recomendado)

### 1. Instalar PostgreSQL

**Windows:**
```bash
# Descargar desde: https://www.postgresql.org/download/windows/
# O usar Chocolatey:
choco install postgresql
```

**Linux:**
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
```

**macOS:**
```bash
brew install postgresql
brew services start postgresql
```

### 2. Crear Base de Datos

```bash
# Conectar a PostgreSQL
psql -U postgres

# Crear base de datos
CREATE DATABASE quimbayaeval;

# Crear usuario
CREATE USER quimbaya_user WITH PASSWORD 'tu_password_seguro';

# Dar permisos
GRANT ALL PRIVILEGES ON DATABASE quimbayaeval TO quimbaya_user;

# Salir
\q
```

### 3. Agregar Dependencia PostgreSQL

Editar `pom.xml` y agregar:

```xml
<!-- Después de la dependencia de H2 -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 4. Configurar application.yml

Crear un perfil de producción en `src/main/resources/application-prod.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/quimbayaeval
    username: quimbaya_user
    password: tu_password_seguro
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update  # Cambia a 'validate' en producción real
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
    show-sql: false  # Desactivar en producción
  
  sql:
    init:
      mode: never  # No ejecutar schema.sql automáticamente

# Mantener el resto de la configuración igual
jwt:
  secret: ${JWT_SECRET:tu_secreto_jwt_muy_largo_y_seguro_minimo_256_bits}
  expiration: 3600000

cors:
  allowed-origins: ${CORS_ORIGINS:http://localhost:5173,http://localhost:3000}

server:
  port: 8080
```

### 5. Actualizar .env

```env
# Base de datos
DB_URL=jdbc:postgresql://localhost:5432/quimbayaeval
DB_USERNAME=quimbaya_user
DB_PASSWORD=tu_password_seguro

# JWT
JWT_SECRET=tu_secreto_jwt_muy_largo_y_seguro_minimo_256_bits

# CORS
CORS_ORIGINS=http://localhost:5173,http://localhost:3000
```

### 6. Crear Script de Migración SQL

Crear `src/main/resources/db/migration/V1__initial_schema.sql`:

```sql
-- Tabla de usuarios
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('estudiante', 'maestro')),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de cursos
CREATE TABLE IF NOT EXISTS cursos (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    profesor_id INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (profesor_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tabla de evaluaciones
CREATE TABLE IF NOT EXISTS evaluaciones (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    curso_id INTEGER NOT NULL,
    profesor_id INTEGER NOT NULL,
    tipo VARCHAR(50) NOT NULL CHECK (tipo IN ('Examen', 'Quiz', 'Taller', 'Proyecto', 'Tarea')),
    estado VARCHAR(50) NOT NULL DEFAULT 'Borrador' CHECK (estado IN ('Borrador', 'Programada', 'Activa', 'Cerrada')),
    deadline TIMESTAMP,
    duracion_minutos INTEGER DEFAULT 60,
    intentos_permitidos INTEGER DEFAULT 1,
    publicada BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (curso_id) REFERENCES cursos(id) ON DELETE CASCADE,
    FOREIGN KEY (profesor_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tabla de preguntas
CREATE TABLE IF NOT EXISTS preguntas (
    id SERIAL PRIMARY KEY,
    evaluacion_id INTEGER NOT NULL,
    enunciado TEXT NOT NULL,
    tipo VARCHAR(50) NOT NULL CHECK (tipo IN ('seleccion_multiple', 'verdadero_falso', 'respuesta_corta', 'ensayo')),
    puntuacion DECIMAL(5, 2) DEFAULT 1.0,
    orden INTEGER,
    opciones_json TEXT,
    respuesta_correcta_json TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (evaluacion_id) REFERENCES evaluaciones(id) ON DELETE CASCADE
);

-- Tabla de submissions (entregas)
CREATE TABLE IF NOT EXISTS submissions (
    id SERIAL PRIMARY KEY,
    evaluacion_id INTEGER NOT NULL,
    estudiante_id INTEGER NOT NULL,
    respuestas_json TEXT,
    estado VARCHAR(50) NOT NULL DEFAULT 'Borrador' CHECK (estado IN ('Borrador', 'Enviada', 'Calificada')),
    intento_numero INTEGER DEFAULT 1,
    fecha_inicio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_envio TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (evaluacion_id) REFERENCES evaluaciones(id) ON DELETE CASCADE,
    FOREIGN KEY (estudiante_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tabla de calificaciones
CREATE TABLE IF NOT EXISTS calificaciones (
    id SERIAL PRIMARY KEY,
    submission_id INTEGER NOT NULL,
    puntuacion_obtenida DECIMAL(5, 2) NOT NULL,
    puntuacion_maxima DECIMAL(5, 2) NOT NULL,
    retroalimentacion TEXT,
    calificado_por_id INTEGER NOT NULL,
    fecha_calificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (submission_id) REFERENCES submissions(id) ON DELETE CASCADE,
    FOREIGN KEY (calificado_por_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tabla de PQRS
CREATE TABLE IF NOT EXISTS pqrs (
    id SERIAL PRIMARY KEY,
    tipo VARCHAR(50) NOT NULL CHECK (tipo IN ('Petición', 'Queja', 'Reclamo', 'Sugerencia')),
    asunto VARCHAR(255) NOT NULL,
    descripcion TEXT NOT NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'Pendiente' CHECK (estado IN ('Pendiente', 'En Proceso', 'Resuelta', 'Cerrada')),
    usuario_id INTEGER NOT NULL,
    curso_id INTEGER,
    respuesta TEXT,
    respondido_por_id INTEGER,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_respuesta TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (curso_id) REFERENCES cursos(id) ON DELETE SET NULL,
    FOREIGN KEY (respondido_por_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Índices para mejorar rendimiento
CREATE INDEX idx_cursos_profesor ON cursos(profesor_id);
CREATE INDEX idx_evaluaciones_curso ON evaluaciones(curso_id);
CREATE INDEX idx_evaluaciones_profesor ON evaluaciones(profesor_id);
CREATE INDEX idx_evaluaciones_estado ON evaluaciones(estado);
CREATE INDEX idx_preguntas_evaluacion ON preguntas(evaluacion_id);
CREATE INDEX idx_submissions_evaluacion ON submissions(evaluacion_id);
CREATE INDEX idx_submissions_estudiante ON submissions(estudiante_id);
CREATE INDEX idx_calificaciones_submission ON calificaciones(submission_id);
CREATE INDEX idx_pqrs_usuario ON pqrs(usuario_id);
CREATE INDEX idx_pqrs_estado ON pqrs(estado);
```

### 7. Crear Datos de Prueba (Opcional)

Crear `src/main/resources/db/migration/V2__seed_data.sql`:

```sql
-- Insertar usuarios de prueba (contraseñas hasheadas con BCrypt)
-- Contraseña para todos: "password123"
INSERT INTO users (name, email, password, role, active) VALUES
('Profesor Demo', 'profesor@quimbaya.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'maestro', true),
('Estudiante Demo', 'estudiante@quimbaya.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'estudiante', true),
('María García', 'maria@quimbaya.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'estudiante', true),
('Juan Pérez', 'juan@quimbaya.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'estudiante', true);

-- Insertar cursos de ejemplo
INSERT INTO cursos (codigo, nombre, descripcion, profesor_id) VALUES
('MAT101', 'Matemáticas Básicas', 'Curso introductorio de matemáticas', 1),
('FIS101', 'Física I', 'Fundamentos de física mecánica', 1),
('PROG101', 'Programación I', 'Introducción a la programación', 1);

-- Insertar evaluaciones de ejemplo
INSERT INTO evaluaciones (nombre, descripcion, curso_id, profesor_id, tipo, estado, duracion_minutos, publicada) VALUES
('Parcial 1 - Álgebra', 'Primera evaluación de álgebra', 1, 1, 'Examen', 'Activa', 90, true),
('Quiz 1 - Geometría', 'Quiz corto de geometría', 1, 1, 'Quiz', 'Activa', 30, true),
('Taller Cinemática', 'Taller práctico de cinemática', 2, 1, 'Taller', 'Programada', 120, false);

-- Insertar preguntas de ejemplo
INSERT INTO preguntas (evaluacion_id, enunciado, tipo, puntuacion, orden, opciones_json) VALUES
(1, '¿Cuánto es 2 + 2?', 'seleccion_multiple', 1.0, 1, 
 '[{"id":1,"texto":"3","correcta":false},{"id":2,"texto":"4","correcta":true},{"id":3,"texto":"5","correcta":false}]'),
(1, '¿Es verdadero que 5 > 3?', 'verdadero_falso', 1.0, 2,
 '[{"id":1,"texto":"Verdadero","correcta":true},{"id":2,"texto":"Falso","correcta":false}]');
```

### 8. Ejecutar la Migración

**Opción A: Usando Flyway (Recomendado para producción)**

Agregar a `pom.xml`:
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

Flyway ejecutará automáticamente los scripts en `db/migration/` al iniciar.

**Opción B: Ejecutar manualmente**

```bash
# Conectar a PostgreSQL
psql -U quimbaya_user -d quimbayaeval

# Ejecutar el script
\i src/main/resources/db/migration/V1__initial_schema.sql
\i src/main/resources/db/migration/V2__seed_data.sql
```

### 9. Ejecutar la Aplicación

```bash
# Con perfil de producción
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# O configurar en application.yml:
spring:
  profiles:
    active: prod
```

## Migración a MySQL

### 1. Instalar MySQL

```bash
# Windows
choco install mysql

# Linux
sudo apt install mysql-server

# macOS
brew install mysql
```

### 2. Crear Base de Datos

```sql
CREATE DATABASE quimbayaeval CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'quimbaya_user'@'localhost' IDENTIFIED BY 'tu_password_seguro';
GRANT ALL PRIVILEGES ON quimbayaeval.* TO 'quimbaya_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Agregar Dependencia

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 4. Configurar application-prod.yml

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/quimbayaeval?useSSL=false&serverTimezone=UTC
    username: quimbaya_user
    password: tu_password_seguro
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
```

### 5. Adaptar Script SQL para MySQL

Cambiar `SERIAL` por `AUTO_INCREMENT` y ajustar tipos de datos:

```sql
-- Ejemplo
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    -- ... resto igual
);
```

## Docker Compose (Recomendado para Desarrollo)

Crear `docker-compose.yml` en la raíz del proyecto:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: quimbayaeval-db
    environment:
      POSTGRES_DB: quimbayaeval
      POSTGRES_USER: quimbaya_user
      POSTGRES_PASSWORD: password123
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./src/main/resources/db/migration:/docker-entrypoint-initdb.d
    networks:
      - quimbaya-network

  # Opcional: PgAdmin para administrar la base de datos
  pgadmin:
    image: dpage/pgadmin4
    container_name: quimbayaeval-pgadmin
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@quimbaya.edu
      PGADMIN_DEFAULT_PASSWORD: admin123
    ports:
      - "5050:80"
    networks:
      - quimbaya-network
    depends_on:
      - postgres

volumes:
  postgres_data:

networks:
  quimbaya-network:
    driver: bridge
```

### Usar Docker Compose

```bash
# Iniciar base de datos
docker-compose up -d postgres

# Ver logs
docker-compose logs -f postgres

# Detener
docker-compose down

# Detener y eliminar datos
docker-compose down -v
```

## Configuración Multi-Perfil

Mantener H2 para desarrollo y tests, PostgreSQL para producción:

**application.yml** (desarrollo - H2):
```yaml
spring:
  profiles:
    active: dev
  datasource:
    url: jdbc:h2:mem:testdb
    # ... configuración H2
```

**application-prod.yml** (producción - PostgreSQL):
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/quimbayaeval
    # ... configuración PostgreSQL
```

**application-test.yml** (tests - H2):
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    # ... configuración H2 para tests
```

## Backup y Restauración

### PostgreSQL

**Backup:**
```bash
pg_dump -U quimbaya_user -d quimbayaeval > backup.sql
```

**Restauración:**
```bash
psql -U quimbaya_user -d quimbayaeval < backup.sql
```

### MySQL

**Backup:**
```bash
mysqldump -u quimbaya_user -p quimbayaeval > backup.sql
```

**Restauración:**
```bash
mysql -u quimbaya_user -p quimbayaeval < backup.sql
```

## Checklist de Migración

- [ ] Instalar base de datos (PostgreSQL/MySQL)
- [ ] Crear base de datos y usuario
- [ ] Agregar dependencia en pom.xml
- [ ] Crear application-prod.yml
- [ ] Crear scripts de migración SQL
- [ ] Configurar variables de entorno (.env)
- [ ] Ejecutar scripts de migración
- [ ] Probar conexión
- [ ] Insertar datos de prueba
- [ ] Ejecutar tests
- [ ] Configurar backup automático

## Troubleshooting

### Error: "Connection refused"
- Verificar que la base de datos esté corriendo
- Verificar puerto correcto (5432 para PostgreSQL, 3306 para MySQL)
- Verificar firewall

### Error: "Authentication failed"
- Verificar usuario y contraseña
- Verificar permisos del usuario

### Error: "Database does not exist"
- Crear la base de datos manualmente
- Verificar nombre de la base de datos

### Tablas no se crean
- Verificar `ddl-auto: update` en application.yml
- Ejecutar scripts SQL manualmente
- Revisar logs de Hibernate

## Recomendaciones de Producción

1. **Nunca usar `ddl-auto: create` o `create-drop` en producción**
2. **Usar Flyway o Liquibase para migraciones controladas**
3. **Configurar backups automáticos diarios**
4. **Usar conexiones SSL para la base de datos**
5. **Configurar pool de conexiones apropiado**
6. **Monitorear rendimiento de queries**
7. **Implementar índices en columnas frecuentemente consultadas**
8. **Usar variables de entorno para credenciales sensibles**

## Recursos Adicionales

- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [MySQL Documentation](https://dev.mysql.com/doc/)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Spring Boot Database Guide](https://spring.io/guides/gs/accessing-data-jpa/)
