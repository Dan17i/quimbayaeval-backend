# 🎓 QuimbayaEVAL Backend

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Sistema backend REST API para gestión de evaluaciones académicas con autenticación JWT, autorización por roles y arquitectura en capas.

## 🚀 Inicio Rápido

### Primera vez (desde cero)

```powershell
# 1. Levantar PostgreSQL en Docker (puerto 5433)
docker-compose up -d

# 2. Ejecutar Spring Boot — Flyway aplica el schema y seed automáticamente
mvn spring-boot:run
```

Backend disponible en: `http://localhost:8080`

### Volver a correr (después de Ctrl+C o reinicio)

```powershell
# Solo esto basta — application.yml ya tiene el puerto 5433 por defecto
mvn spring-boot:run
```

> Si Docker no está corriendo, primero: `docker-compose up -d`

Ver [SETUP.md](SETUP.md) para instrucciones detalladas.

## ✨ Características

-  Autenticación JWT con roles (estudiante, maestro, coordinador)
-  Gestión completa de cursos y evaluaciones
-  Sistema PQRS (Peticiones, Quejas, Reclamos, Sugerencias)
-  Validación de datos con Bean Validation
-  Seguridad con Spring Security + CORS configurado
-  Métricas con Actuator + Prometheus
-  Rate limiting para protección de API
-  Caché para optimización de consultas
-  Documentación Swagger/OpenAPI
-  Tests completos (unitarios + integración)

##  Requisitos

- Java 17+
- Maven 3.8+
- PostgreSQL 15+
- Docker & Docker Compose (opcional)

##  Credenciales de Prueba

| Usuario | Email | Password | Rol |
|---------|-------|----------|-----|
| Juan Estudiante | estudiante@quimbaya.edu.co | password | estudiante |
| María Profesora | profesor@quimbaya.edu.co | password | maestro |
| Admin Sistema | admin@quimbaya.edu.co | password | coordinador |

Ver [CREDENCIALES.md](CREDENCIALES.md) para más detalles.

##  Endpoints Principales

### Autenticación (Públicos)
```bash
POST /api/auth/login      # Iniciar sesión (requiere campo "role")
POST /api/auth/register   # Registrar usuario
```

### Usuarios
```bash
GET    /api/users                        # Listar usuarios activos (?role=maestro)
GET    /api/users/me                     # Perfil propio + cursos (desde JWT)
PUT    /api/users/me                     # Editar nombre y fotoUrl
PUT    /api/users/me/password            # Cambiar contraseña
PATCH  /api/users/{id}/status            # Activar/bloquear usuario (coordinador)
DELETE /api/users/{id}                   # Soft delete (coordinador)
```

### Cursos
```bash
GET    /api/cursos                       # Listar cursos
POST   /api/cursos                       # Crear curso (coordinador)
PUT    /api/cursos/{id}                  # Actualizar curso
DELETE /api/cursos/{id}                  # Eliminar curso
GET    /api/cursos/{id}/estudiantes      # Estudiantes matriculados
POST   /api/cursos/{id}/estudiantes      # Matricular estudiante
DELETE /api/cursos/{id}/estudiantes/{estudianteId}  # Desmatricular
```

### Evaluaciones
```bash
GET  /api/evaluaciones                   # Listar (?profesorId, ?cursoId, ?estado, ?tipo)
GET  /api/evaluaciones/{id}              # Obtener por ID
POST /api/evaluaciones                   # Crear (maestro/coordinador)
PUT  /api/evaluaciones/{id}              # Actualizar
POST /api/evaluaciones/{id}/publicar     # Publicar (estado → Activa)
DELETE /api/evaluaciones/{id}            # Eliminar
```

### Resultados y Calificaciones
```bash
GET  /api/resultados/mis-resultados      # Resultados del estudiante autenticado
GET  /api/resultados/curso/{id}          # Notas de todos los estudiantes del curso
GET  /api/resultados/curso/{id}/resumen  # Promedio grupal por evaluación
POST /api/calificaciones                 # Calificar (calificadoPorId desde JWT)
POST /api/pqrs                           # Crear PQRS (usuarioId desde JWT)
```

### Nota escala 1-5
La nota en escala colombiana se calcula como: `nota = 1 + (porcentaje / 100) * 4`

##  Arquitectura

```
┌─────────────────────────────────────┐
│         Controllers                 │  ← REST Endpoints
├─────────────────────────────────────┤
│         Services                    │  ← Lógica de Negocio
├─────────────────────────────────────┤
│         Repositories (JPA)          │  ← Acceso a Datos
├─────────────────────────────────────┤
│         Entities                    │  ← Modelos de Datos
└─────────────────────────────────────┘
```

Ver [ARCHITECTURE.md](ARCHITECTURE.md) para detalles técnicos.

##  Testing

```powershell
# Ejecutar todos los tests (H2 en memoria, no necesita Docker)
# Si en la sesión actual NO seteaste SPRING_DATASOURCE_URL:
mvn test

# Si la seteaste antes (ej: para correr el backend), limpiarla primero:
Remove-Item Env:SPRING_DATASOURCE_URL -ErrorAction SilentlyContinue
mvn test
```

> Los tests usan H2 en memoria. La variable de entorno `SPRING_DATASOURCE_URL` tiene prioridad sobre `application.yml`, por eso hay que limpiarla si fue seteada en la sesión.

##  Métricas y Monitoreo

```bash
# Health check
curl http://localhost:8080/actuator/health

# Métricas Prometheus
curl http://localhost:8080/actuator/prometheus

# Swagger UI
http://localhost:8080/swagger-ui.html
```

##  Configuración

### Variables de Entorno Requeridas

```env
# JWT
JWT_SECRET=tu-secret-super-seguro-de-al-menos-256-bits

# Base de Datos — puerto 5433 (Docker)
# No es necesario setear estas variables si usas el application.yml por defecto
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/quimbayaeval
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# CORS (Frontend)
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
```

> El `application.yml` ya tiene `localhost:5433` como valor por defecto. Solo necesitas setear las variables si quieres sobreescribir esos valores.

## 📚 Documentación

| Archivo | Descripción |
|---------|-------------|
| [SETUP.md](SETUP.md) | Guía completa de instalación y configuración |
| [API.md](API.md) | Documentación de todos los endpoints REST |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Arquitectura técnica y patrones de diseño |
| [CREDENCIALES.md](CREDENCIALES.md) | Usuarios de prueba y ejemplos de uso |

## 📁 Estructura del Proyecto

```
quimbayaeval-backend/
├── src/main/java/com/quimbayaeval/
│   ├── config/          # Configuración (Security, Cache, Metrics)
│   ├── controller/      # REST Controllers
│   ├── service/         # Lógica de negocio
│   ├── repository/      # Repositorios JPA
│   ├── model/           # Entidades y DTOs
│   ├── security/        # JWT y autenticación
│   └── exception/       # Manejo de excepciones
├── src/main/resources/
│   ├── application.yml  # Configuración principal
│   └── db/schema.sql    # Esquema de base de datos
├── scripts/             # Scripts de utilidad (PowerShell)
├── .env.example         # Plantilla de variables de entorno
├── docker-compose.yml   # Configuración Docker
├── pom.xml              # Dependencias Maven
├── README.md            # Este archivo
├── SETUP.md             # Guía de instalación
├── API.md               # Documentación de API
├── ARCHITECTURE.md      # Arquitectura técnica
└── CREDENCIALES.md      # Credenciales de prueba
```

##  Stack Tecnológico

### Backend
- Java 17
- Spring Boot 3.2.2
- Spring Data JPA
- Spring Security
- Hibernate

### Base de Datos
- PostgreSQL 15
- H2 (tests)

### Seguridad
- JWT (JJWT 0.12.3)
- BCrypt

### Herramientas
- Maven
- Lombok
- Docker
- Swagger/OpenAPI
- Actuator + Prometheus

##  Contribuir

1. Fork el repositorio
2. Crea una rama (`git checkout -b feature/nueva-funcionalidad`)
3. Commit tus cambios (`git commit -am 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Abre un Pull Request

##  Licencia

Este proyecto está bajo la Licencia MIT. Ver [LICENSE](LICENSE) para más detalles.

##  Soporte

- Documentación: Ver archivos `.md` en el repositorio
- Issues: [GitHub Issues](https://github.com/tu-usuario/quimbayaeval-backend/issues)

---

**Versión**: 1.2.0  
**Estado**:  En Desarrollo  
**Última actualización**: Marzo 14, 2026
