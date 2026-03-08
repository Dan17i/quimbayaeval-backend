# 🎓 QuimbayaEVAL Backend

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**Sistema de gestión de evaluaciones académicas con arquitectura REST API**

QuimbayaEVAL es una plataforma backend robusta construida con Spring Boot que proporciona servicios completos para la gestión de evaluaciones académicas, cursos, calificaciones y sistema de tickets (PQRS). Diseñado con arquitectura en capas, seguridad JWT, validación de datos y preparado para integración con frontend React.

> **Estado del Proyecto**: ✅ Listo para producción (desarrollo) | 🚀 Integración con frontend disponible

---

## 📋 Tabla de Contenidos

- [Características Principales](#-características-principales)
- [Tecnologías](#-tecnologías)
- [Arquitectura](#-arquitectura)
- [Requisitos Previos](#-requisitos-previos)
- [Instalación y Configuración](#️-instalación-y-configuración)
- [Ejecución del Proyecto](#-ejecución-del-proyecto)
- [Endpoints de la API](#-endpoints-de-la-api)
- [Autenticación y Seguridad](#-autenticación-y-seguridad)
- [Testing](#-testing)
- [Documentación Adicional](#-documentación-adicional)
- [Contribuir](#-contribuir)

---

##  Características Principales

### Funcionalidades Core
-  **Autenticación JWT** con roles (estudiante, maestro, coordinador)
-  **Gestión de Cursos** - CRUD completo con paginación
-  **Sistema de Evaluaciones** - Creación, publicación y gestión
-  **Calificaciones** - Registro y consulta de resultados
-  **Sistema PQRS** - Peticiones, Quejas, Reclamos y Sugerencias
-  **Gestión de Usuarios** - Registro y autenticación por roles

### Características Técnicas
-  **Seguridad Completa**: JWT, CORS configurado, autorización por rol
-  **Validación de Datos**: Bean Validation con DTOs
-  **Manejo de Errores**: GlobalExceptionHandler con respuestas estructuradas
-  **Logging Estructurado**: SLF4J con niveles configurables
-  **Métricas**: Actuator + Prometheus para monitoreo
-  **Rate Limiting**: Protección contra abuso de API
-  **Caché**: Optimización de consultas frecuentes
-  **Documentación**: Swagger UI integrado
-  **Tests Completos**: Unitarios, integración y E2E

---

## 🛠 Tecnologías

### Backend Framework
- **Java 17** - Lenguaje de programación
- **Spring Boot 3.2.2** - Framework principal
- **Spring Data JPA** - Persistencia de datos
- **Spring Security** - Autenticación y autorización
- **Hibernate** - ORM

### Base de Datos
- **PostgreSQL 15** - Base de datos principal
- **H2** - Base de datos en memoria para tests

### Seguridad
- **JWT (JJWT 0.12.3)** - Tokens de autenticación
- **BCrypt** - Encriptación de contraseñas

### Documentación y Monitoreo
- **Swagger/OpenAPI** - Documentación de API
- **Spring Actuator** - Health checks y métricas
- **Prometheus** - Métricas de aplicación

### Testing
- **JUnit 5** - Framework de testing
- **Mockito** - Mocking
- **MockMvc** - Tests de integración
- **AssertJ** - Assertions fluidas

### Herramientas
- **Maven** - Gestión de dependencias
- **Lombok** - Reducción de boilerplate
- **Docker** - Containerización

---

## 🏗 Arquitectura

### Patrón de Capas

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

### Componentes Principales

```
src/main/java/com/quimbayaeval/
├── config/              # Configuración (Security, CORS, Cache)
├── controller/          # REST Controllers
├── service/             # Lógica de negocio
├── repository/          # Repositorios JPA
├── model/
│   ├── entity/         # Entidades JPA
│   └── dto/            # Data Transfer Objects
├── security/            # JWT, Filtros, Autenticación
├── exception/           # Excepciones personalizadas
└── mapper/              # Conversión Entity ↔ DTO
```

---

## 📦 Requisitos Previos

- **Java 17** (JDK) - [Descargar](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.8+** - [Descargar](https://maven.apache.org/download.cgi)
- **PostgreSQL 15** - [Descargar](https://www.postgresql.org/download/)
- **Docker & Docker Compose** (opcional) - [Descargar](https://www.docker.com/)

---

## ⚙️ Instalación y Configuración

### 1. Clonar el Repositorio

```bash
git clone https://github.com/tu-usuario/quimbayaeval-backend.git
cd quimbayaeval-backend
```

### 2. Configurar Variables de Entorno

Crea un archivo `.env` en la raíz del proyecto:

```env
# Base de Datos
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/quimbayaeval
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# Seguridad
JWT_SECRET=tu-secret-super-seguro-de-al-menos-256-bits-aqui

# CORS (Frontend)
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
```

> **Importante**: Genera un JWT_SECRET seguro usando:
> ```bash
> openssl rand -base64 64
> ```

### 3. Crear Base de Datos

```sql
CREATE DATABASE quimbayaeval;
```

El esquema se crea automáticamente al iniciar la aplicación gracias a `schema.sql`.

### 4. Instalar Dependencias

```bash
mvn clean install
```

---

## 🚀 Ejecución del Proyecto

### Opción 1: Ejecución Local con Maven

```bash
# Compilar el proyecto
mvn clean package -DskipTests

# Ejecutar la aplicación
mvn spring-boot:run
```

La aplicación estará disponible en: `http://localhost:8080`

### Opción 2: Ejecución con Docker Compose

```bash
# Iniciar todos los servicios (PostgreSQL + Backend)
docker-compose up --build
```

Servicios disponibles:
- Backend: `http://localhost:8080`
- PostgreSQL: `localhost:5432`

### Opción 3: Script de Configuración Automática (Windows)

```powershell
# Ejecutar script de setup
.\setup.ps1
```

Este script:
- ✅ Verifica requisitos (Java, Maven, PostgreSQL)
- ✅ Configura variables de entorno
- ✅ Crea la base de datos
- ✅ Compila el proyecto
- ✅ Inserta datos de prueba

### Verificar que Funciona

```bash
# Health check
curl http://localhost:8080/actuator/health

# Respuesta esperada:
# {"status":"UP"}
```

---

## 📡 Endpoints de la API

### Autenticación (Públicos)

| Método | Endpoint | Descripción | Body |
|--------|----------|-------------|------|
| POST | `/api/auth/login` | Iniciar sesión | `{email, password, role}` |
| POST | `/api/auth/register` | Registrar usuario | `{name, email, password, role}` |

### Cursos (Protegidos)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/api/cursos` | Listar cursos | Todos |
| GET | `/api/cursos/{id}` | Obtener curso | Todos |
| POST | `/api/cursos` | Crear curso | Coordinador |
| PUT | `/api/cursos/{id}` | Actualizar curso | Coordinador |
| DELETE | `/api/cursos/{id}` | Eliminar curso | Coordinador |

### Evaluaciones (Protegidos)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/api/evaluaciones` | Listar evaluaciones | Todos |
| GET | `/api/evaluaciones/{id}` | Obtener evaluación | Todos |
| POST | `/api/evaluaciones` | Crear evaluación | Maestro, Coordinador |
| POST | `/api/evaluaciones/{id}/publicar` | Publicar evaluación | Maestro, Coordinador |

### PQRS (Protegidos)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/api/pqrs` | Listar PQRS | Todos |
| GET | `/api/pqrs/{id}` | Obtener PQRS | Todos |
| POST | `/api/pqrs` | Crear PQRS | Todos |
| PUT | `/api/pqrs/{id}` | Actualizar PQRS | Coordinador |

### Paginación y Ordenamiento

Todos los endpoints GET que listan recursos soportan:

```bash
# Parámetros opcionales
?page=0          # Número de página (0-indexado)
&size=10         # Registros por página
&sort=nombre     # Campo para ordenar
&direction=ASC   # Dirección (ASC o DESC)

# Ejemplo
GET /api/cursos?page=0&size=10&sort=nombre&direction=ASC
```

### Documentación Interactiva

Accede a Swagger UI para probar los endpoints:

```
http://localhost:8080/swagger-ui.html
```

---

## 🔐 Autenticación y Seguridad

### Flujo de Autenticación

1. **Login**: El cliente envía credenciales a `/api/auth/login`
2. **Token JWT**: El servidor responde con un token JWT
3. **Requests**: El cliente incluye el token en el header `Authorization`
4. **Validación**: El servidor valida el token en cada request

### Ejemplo de Login

```bash
# Request
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "estudiante@test.com",
  "password": "password",
  "role": "estudiante"
}

# Response
{
  "success": true,
  "message": "Login exitoso",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "id": 1,
    "name": "Juan Estudiante",
    "email": "estudiante@test.com",
    "role": "estudiante"
  }
}
```

### Usar el Token

```bash
# Incluir token en header Authorization
GET http://localhost:8080/api/cursos
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Credenciales de Prueba

| Usuario | Email | Password | Rol |
|---------|-------|----------|-----|
| Juan Estudiante | estudiante@test.com | password | estudiante |
| María Maestra | maestro@test.com | password | maestro |
| Carlos Coordinador | coordinador@test.com | password | coordinador |

### Roles y Permisos

- **Estudiante**: Consultar cursos, evaluaciones, enviar PQRS
- **Maestro**: Todo lo anterior + crear evaluaciones
- **Coordinador**: Acceso completo (CRUD en todos los recursos)

---

## 🧪 Testing

### Ejecutar Todos los Tests

```bash
mvn test
```

### Ejecutar Tests Específicos

```bash
# Tests de repositorio
mvn test -Dtest="*RepositoryTest"

# Tests de servicio
mvn test -Dtest="*ServiceTest"

# Tests de controlador
mvn test -Dtest="*ControllerTest"

# Tests de integración
mvn test -Dtest="*IntegrationTest"
```

### Cobertura de Tests

```bash
# Generar reporte de cobertura
mvn test jacoco:report

# Ver reporte
start target/site/jacoco/index.html
```

### Estructura de Tests

```
src/test/java/com/quimbayaeval/
├── repository/          # Tests de JPA Repositories
├── service/             # Tests unitarios de servicios
├── controller/          # Tests de integración de controllers
├── security/            # Tests de JWT y seguridad
├── validation/          # Tests de validación de DTOs
├── exception/           # Tests de manejo de excepciones
└── integration/         # Tests E2E completos
```

### Tests Implementados

- ✅ **UserRepositoryTest** - Tests de repositorio de usuarios
- ✅ **EvaluacionRepositoryTest** - Tests de repositorio de evaluaciones
- ✅ **CursoRepositoryTest** - Tests de repositorio de cursos
- ✅ **PQRSRepositoryTest** - Tests de repositorio de PQRS
- ✅ **AuthServiceJpaTest** - Tests unitarios de autenticación
- ✅ **AuthControllerJpaTest** - Tests de integración de auth
- ✅ **JwtTokenProviderTest** - Tests de generación y validación JWT
- ✅ **DTOValidationTest** - Tests de validación de DTOs
- ✅ **GlobalExceptionHandlerTest** - Tests de manejo de errores
- ✅ **SecurityConfigTest** - Tests de configuración de seguridad
- ✅ **AuthenticationFlowTest** - Tests E2E de flujo completo

---

## 📚 Documentación Adicional

### Guías de Usuario
- **[INICIO_RAPIDO.md](INICIO_RAPIDO.md)** - Guía de inicio rápido (5 minutos)
- **[GUIA_EJECUCION.md](GUIA_EJECUCION.md)** - Guía detallada paso a paso
- **[CHECKLIST_INTEGRACION_FRONTEND.md](CHECKLIST_INTEGRACION_FRONTEND.md)** - Integración con frontend React

### Documentación Técnica
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Arquitectura del sistema
- **[BACKEND_README.md](BACKEND_README.md)** - Documentación técnica completa
- **[MEJORAS_IMPLEMENTADAS.md](MEJORAS_IMPLEMENTADAS.md)** - Historial de mejoras

### Análisis y Calidad
- **[ANALISIS_BUENAS_PRACTICAS.md](ANALISIS_BUENAS_PRACTICAS.md)** - Análisis de código
- **[ANALISIS_TESTS.md](ANALISIS_TESTS.md)** - Cobertura de tests
- **[RESUMEN_ESTADO_PROYECTO.md](RESUMEN_ESTADO_PROYECTO.md)** - Estado actual

---

## 🤝 Contribuir

### Proceso de Contribución

1. **Fork** el repositorio
2. Crea una **rama** con nombre descriptivo (`feature/nueva-funcionalidad`)
3. **Implementa** tu cambio con tests
4. Asegúrate de que `mvn test` pase
5. **Commit** con mensajes claros
6. **Push** a tu fork
7. Abre un **Pull Request**

### Estándares de Código

- Seguir convenciones de Java (camelCase, PascalCase)
- Documentar métodos públicos con Javadoc
- Mantener cobertura de tests > 80%
- Usar Lombok para reducir boilerplate
- Validar datos con Bean Validation

### Reportar Issues

Si encuentras un bug o tienes una sugerencia:

1. Verifica que no exista un issue similar
2. Crea un nuevo issue con:
   - Descripción clara del problema
   - Pasos para reproducir
   - Comportamiento esperado vs actual
   - Logs relevantes

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver [LICENSE](LICENSE) para más detalles.

---

## 👥 Equipo

Desarrollado por el equipo QuimbayaEVAL

---

## 📞 Soporte

- **Documentación**: Ver carpeta de documentos `.md`
- **Issues**: [GitHub Issues](https://github.com/tu-usuario/quimbayaeval-backend/issues)
- **Email**: soporte@quimbayaeval.com

---

## 🎯 Roadmap

### Versión Actual (2.0.0)
- ✅ Migración completa a JPA
- ✅ Seguridad JWT con roles
- ✅ Validación de datos con DTOs
- ✅ Tests completos (unitarios + integración)
- ✅ Documentación Swagger
- ✅ Métricas con Prometheus

### Próximas Versiones
- 🔄 Refresh tokens
- 🔄 Filtrado avanzado en endpoints
- 🔄 Notificaciones por email
- 🔄 Exportación de reportes (PDF/Excel)
- 🔄 CI/CD con GitHub Actions
- 🔄 Despliegue en la nube (AWS/Azure)

---

**Última actualización**: Marzo 6, 2026  
**Versión**: 2.0.0  
**Estado**: ✅ Producción Ready (Desarrollo)

---

⭐ Si este proyecto te fue útil, considera darle una estrella en GitHub
