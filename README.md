# 🎓 QuimbayaEVAL Backend

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Sistema backend REST API para gestión de evaluaciones académicas con autenticación JWT, autorización por roles y arquitectura en capas.

## 🚀 Inicio Rápido

### Con Docker (Recomendado)

```bash
# Clonar repositorio
git clone <repo-url>
cd quimbayaeval-backend

# 1. Levantar PostgreSQL en Docker (puerto 5433 para evitar conflicto con PostgreSQL local)
docker-compose up postgres -d

# 2. Ejecutar Spring Boot con variables de entorno
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5433/quimbayaeval"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="postgres"
mvn clean spring-boot:run

# Backend disponible en: http://localhost:8080
```

### Sin Docker

```bash
# 1. Crear base de datos
createdb quimbayaeval

# 2. Configurar variables de entorno
cp .env.example .env
# Editar .env con tus valores

# 3. Ejecutar backend
mvn spring-boot:run
```

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
POST /api/auth/login      # Iniciar sesión
POST /api/auth/register   # Registrar usuario
```

### Recursos Protegidos
```bash
GET  /api/cursos              # Listar cursos
GET  /api/evaluaciones        # Listar evaluaciones
POST /api/evaluaciones        # Crear evaluación (maestro/coordinador)
GET  /api/pqrs                # Listar PQRS
POST /api/pqrs                # Crear PQRS
```

Ver [API.md](API.md) para documentación completa de endpoints.

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

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar tests específicos
mvn test -Dtest="*ControllerTest"

# Generar reporte de cobertura
mvn test jacoco:report
```

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

# Base de Datos
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/quimbayaeval
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# CORS (Frontend)
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
```

Ver [SETUP.md](SETUP.md) para configuración completa.

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

**Versión**: 1.0.0  
**Estado**:  En Desarrollo  
**Última actualización**: Marzo 13, 2026
