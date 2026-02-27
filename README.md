# QuimbayaEVAL Backend

**Repositorio del servicio REST que alimenta la aplicación QuimbayaEVAL.**

Se trata de un backend construido con **Java17 y SpringBoot 3.2**, persistencia mediante **JdbcTemplate** y base de datos **PostgreSQL15**. La API ofrece autenticación con JWT, gestión de cursos, evaluaciones y sistema de tickets (PQRS). Está pensado para integrarse con un frontend React/TypeScript; todas las rutas están documentadas con OpenAPI/Swagger.

> Esta README contiene instrucciones de instalación, ejecución, pruebas y recomendaciones para documentar y mantener el proyecto.

---

##  Requisitos previos

- Java17 (JDK)
- Maven 3.8+
- Docker & DockerCompose (para el entorno de desarrollo)
- PostgreSQL 15 (si se quiere ejecutar sin contenedor)

---

##  Iniciar el proyecto

### 1. Configuración de variables

Crea un fichero .env en la raíz con:

`	ext
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/quimbaya
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
JWT_SECRET=unaClaveMuySecreta1234567890
`

(para Docker Compose el ejemplo está en .env.example).

### 2. Levantar con Docker Compose

`ash
docker-compose up --build
`

- Backend disponible en http://localhost:8080
- Swagger UI en http://localhost:8080/swagger-ui.html

> El contenedor de PostgreSQL carga automáticamente src/main/resources/db/schema.sql en la inicialización.

### 3. Ejecutar localmente con Maven

`ash
mvn clean package -DskipTests
mvn spring-boot:run
`

Esto usará la configuración de tu pplication.yml; asegúrate de apuntar a una instancia PostgreSQL válida.

---

##  Testing

- **Unitarios**: se ejecutan con Mockito y JUnit en src/test/java/.../service.
- **Integración**: MockMvc + H2 en memoria; los tests se encuentran en src/test/java/.../controller.

Ejecuta todo con:

`ash
mvn clean test
`

La configuración específica para pruebas figura en src/test/resources/application-test.yml.

---
##  Paginación, Ordenación y Filtrado

Todos los endpoints GET que listan recursos (`/api/cursos`, `/api/evaluaciones`, `/api/pqrs`, `/api/preguntas`, `/api/submissions`, `/api/calificaciones`) soportan:

**Parámetros opcionales:**
- `page` (número de página, 0-indexado)
- `size` (registros por página)
- `sort` (columna para ordenar)
- `direction` (ASC o DESC)

**Ejemplos:**

```bash
# Sin paginación (devuelve todos)
GET /api/cursos

# Con paginación: primeros 10 registros
GET /api/cursos?page=0&size=10

# Página 2 con 10 registros
GET /api/cursos?page=1&size=10

# Ordenado por nombre ascendente
GET /api/cursos?page=0&size=10&sort=nombre&direction=ASC

# Ordenado descendente
GET /api/evaluaciones?page=0&size=5&sort=id&direction=DESC
```

---
##  Estructura principal del proyecto

`	ext
src/main/java/com/quimbayaeval/
 config/                  # seguridad, CORS, filtros
 controller/              # REST controllers por recurso
 dao/                     # acceso a datos con JdbcTemplate
 model/                   # entidades y DTOs
 security/                # JWT helpers y filtros
 service/                 # lógica de negocio
 exception/               # manejo global de excepciones

src/main/resources/
 application.yml          # propiedades de runtime
 db/schema.sql            # script DDL de la base de datos
`

---

##  Endpoints disponibles (resumen)

| Recurso        | Método  | Ruta                         | Descripción                  | Autenticación |
|----------------|---------|------------------------------|------------------------------|---------------|
| Auth           | POST    | /api/auth/login              | Iniciar sesión               | No            |
| Auth           | POST    | /api/auth/register           | Crear usuario                | No            |
| Cursos         | GET     | /api/cursos                  | Listar cursos                | Sí            |
| Cursos         | POST    | /api/cursos                  | Crear curso                  | Sí            |
| Cursos         | PUT     | /api/cursos/{id}             | Actualizar curso             | Sí            |
| Cursos         | DELETE  | /api/cursos/{id}             | Eliminar curso               | Sí            |
| Evaluaciones   | GET     | /api/evaluaciones            | Listar                     | Sí            |
| Evaluaciones   | POST    | /api/evaluaciones            | Crear evaluación             | Sí            |
| Evaluaciones   | POST    | /api/evaluaciones/{id}/publicar | Publicar evaluación     | Sí            |
| PQRS           | GET     | /api/pqrs                    | Listar tickets               | Sí            |
| PQRS           | POST    | /api/pqrs                    | Crear ticket                 | Sí            |
| Preguntas      | GET     | /api/preguntas               | Listar todas                 | Sí            |
| Preguntas      | GET     | /api/preguntas/{id}          | Obtener por id               | Sí            |
| Preguntas      | GET     | /api/preguntas/evaluacion/{evaluacionId} | Filtrar por evaluación | Sí            |
| Preguntas      | POST    | /api/preguntas               | Crear pregunta               | Sí            |
| Preguntas      | PUT     | /api/preguntas/{id}          | Actualizar pregunta          | Sí            |
| Preguntas      | DELETE  | /api/preguntas/{id}          | Eliminar pregunta            | Sí            |
| Submissions    | GET     | /api/submissions             | Listar todas                 | Sí            |
| Submissions    | GET     | /api/submissions/{id}        | Obtener por id               | Sí            |
| Submissions    | GET     | /api/submissions/evaluacion/{evaluacionId} | Filtrar por evaluación | Sí            |
| Submissions    | GET     | /api/submissions/estudiante/{estudianteId} | Filtrar por estudiante | Sí            |
| Submissions    | POST    | /api/submissions             | Crear submission             | Sí            |
| Submissions    | PUT     | /api/submissions/{id}        | Actualizar submission        | Sí            |
| Submissions    | DELETE  | /api/submissions/{id}        | Eliminar submission          | Sí            |
| Calificaciones | GET     | /api/calificaciones          | Listar todas                 | Sí            |
| Calificaciones | GET     | /api/calificaciones/{id}     | Obtener por id               | Sí            |
| Calificaciones | GET     | /api/calificaciones/submission/{submissionId} | Filtrar por submission | Sí            |
| Calificaciones | POST    | /api/calificaciones          | Crear calificación           | Sí            |
| Calificaciones | PUT     | /api/calificaciones/{id}     | Actualizar calificación      | Sí            |
| Calificaciones | DELETE  | /api/calificaciones/{id}     | Eliminar calificación        | Sí            |

*(Ver BACKEND_README.md para la documentación completa, incluyendo parámetros y tipos.)*

---

##  Contenedores
docker-compose.yml describe dos servicios:

1. **postgres**: imagen oficial postgres:15-alpine, volumen persistente postgres_data.
2. **backend**: construye desde Dockerfile multi-stage; expone puerto 8080.

El backend depende del estado sano de PostgreSQL (healthcheck incorporado).

---

##  Documentación adicional

- **BACKEND_README.md**: guía técnica extensiva con configuración, endpoints detallados y notas de arquitectura.
- **INTEGRATION_GUIDE.md**: pasos y ejemplos para conectar el frontend React (env, autenticación, hooks, etc.).
- **QUICK_START.md**: instrucciones ultra-rápidas para arrancar en menos de 5 minutos.
- **ARCHITECTURE.md**: diagramas y explicaciones de alto nivel.

Revisa esos archivos para profundizar.

---

##  Estado actual

El scaffold está **completo y funcional**. Se pueden consumir todos los endpoints, la autenticación JWT funciona, y el proyecto arranca tanto en contenedor como en modo local.

---

##  Pasos siguientes sugeridos

1. Implementar filtrado avanzado (búsqueda por campos específicos).
2. Agregar límites de rate y caché para endpoints frecuentes.
3. Desplegar en un entorno de staging (Azure/AWS/GCP) con CI/CD.
4. Documentar cambios futuros en un CHANGELOG siguiendo [Keep a Changelog](https://keepachangelog.com/).
5. Implementar refresh tokens y revocación de sesiones.

---

##  Contribuir

1. Crea un fork del repositorio.
2. Abre una rama con nombre descriptivo (eature/login-refresh-token).
3. Añade tests para cada nuevo comportamiento.
4. Asegúrate de que mvn test pasa antes de abrir un PR.
5. Describe los cambios en el PR y actualiza la documentación cuando sea necesario.

---

 2026 QuimbayaEVAL Team  Este proyecto está bajo la licencia del repositorio principal.

---

*Última actualización: Febrero26,2026.*
