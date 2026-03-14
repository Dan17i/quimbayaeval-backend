# 📡 Documentación de API - QuimbayaEVAL

Documentación completa de todos los endpoints REST disponibles en QuimbayaEVAL Backend.

## 🔗 Base URL

```
http://localhost:8080/api
```

## 🔐 Autenticación

Todos los endpoints protegidos requieren un token JWT en el header `Authorization`:

```
Authorization: Bearer <token>
```

El token se obtiene mediante el endpoint de login y tiene una validez de 24 horas.

---

## 📋 Tabla de Contenidos

1. [Autenticación](#autenticación-endpoints)
2. [Cursos](#cursos)
3. [Evaluaciones](#evaluaciones)
4. [PQRS](#pqrs)
5. [Calificaciones](#calificaciones)
6. [Paginación y Filtros](#paginación-y-filtros)
7. [Códigos de Respuesta](#códigos-de-respuesta)
8. [Ejemplos de Uso](#ejemplos-de-uso)

---

## 🔑 Autenticación Endpoints

### POST /api/auth/login

Iniciar sesión y obtener token JWT.

**Acceso**: Público

**Request Body**:
```json
{
  "email": "estudiante@quimbaya.edu.co",
  "password": "password123",
  "role": "estudiante"
}
```

**Roles válidos**: `estudiante`, `maestro`, `coordinador`

**Response 200 OK**:
```json
{
  "success": true,
  "message": "Login exitoso",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "type": "Bearer",
    "id": 4,
    "name": "Juan Estudiante",
    "email": "estudiante@quimbaya.edu.co",
    "role": "estudiante"
  }
}
```

**Response 401 Unauthorized**:
```json
{
  "success": false,
  "message": "Credenciales inválidas",
  "data": null
}
```

**Ejemplo cURL**:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"estudiante@quimbaya.edu.co","password":"password123","role":"estudiante"}'
```

### POST /api/auth/register

Registrar nuevo usuario.

**Acceso**: Público

**Request Body**:
```json
{
  "name": "Nuevo Usuario",
  "email": "nuevo@quimbaya.edu.co",
  "password": "password123",
  "role": "estudiante"
}
```

**Response 201 Created**:
```json
{
  "success": true,
  "message": "Usuario registrado exitosamente",
  "data": {
    "id": 8,
    "name": "Nuevo Usuario",
    "email": "nuevo@quimbaya.edu.co",
    "role": "estudiante"
  }
}
```

---

## 📚 Cursos

### GET /api/cursos

Listar todos los cursos con paginación.

**Acceso**: Requiere autenticación (todos los roles)

**Query Parameters**:
- `page` (opcional): Número de página (default: 0)
- `size` (opcional): Registros por página (default: 10)
- `sort` (opcional): Campo para ordenar (default: nombre)
- `direction` (opcional): ASC o DESC (default: ASC)

**Response 200 OK**:
```json
{
  "success": true,
  "message": "Cursos obtenidos exitosamente",
  "data": {
    "content": [
      {
        "id": 1,
        "codigo": "MAT-301",
        "nombre": "Cálculo Integral",
        "descripcion": "Curso de cálculo avanzado",
        "creditos": 4,
        "profesorId": 2,
        "profesorNombre": "María Profesora",
        "activo": true
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 15,
    "totalPages": 2
  }
}
```

**Ejemplo cURL**:
```bash
curl -X GET "http://localhost:8080/api/cursos?page=0&size=10&sort=nombre&direction=ASC" \
  -H "Authorization: Bearer <token>"
```

### GET /api/cursos/{id}

Obtener un curso específico por ID.

**Acceso**: Requiere autenticación

**Response 200 OK**:
```json
{
  "success": true,
  "message": "Curso obtenido exitosamente",
  "data": {
    "id": 1,
    "codigo": "MAT-301",
    "nombre": "Cálculo Integral",
    "descripcion": "Curso de cálculo avanzado",
    "creditos": 4,
    "profesorId": 2,
    "profesorNombre": "María Profesora",
    "activo": true
  }
}
```

**Response 404 Not Found**:
```json
{
  "success": false,
  "message": "Curso no encontrado",
  "data": null
}
```

### POST /api/cursos

Crear un nuevo curso.

**Acceso**: Requiere rol `coordinador`

**Request Body**:
```json
{
  "codigo": "FIS-201",
  "nombre": "Física I",
  "descripcion": "Introducción a la física",
  "creditos": 3,
  "profesorId": 2
}
```

**Response 201 Created**:
```json
{
  "success": true,
  "message": "Curso creado exitosamente",
  "data": {
    "id": 16,
    "codigo": "FIS-201",
    "nombre": "Física I",
    "descripcion": "Introducción a la física",
    "creditos": 3,
    "profesorId": 2,
    "activo": true
  }
}
```

### PUT /api/cursos/{id}

Actualizar un curso existente.

**Acceso**: Requiere rol `coordinador`

**Request Body**:
```json
{
  "codigo": "FIS-201",
  "nombre": "Física I - Actualizado",
  "descripcion": "Descripción actualizada",
  "creditos": 4,
  "profesorId": 2
}
```

**Response 200 OK**:
```json
{
  "success": true,
  "message": "Curso actualizado exitosamente",
  "data": { /* curso actualizado */ }
}
```

### DELETE /api/cursos/{id}

Eliminar un curso.

**Acceso**: Requiere rol `coordinador`

**Response 200 OK**:
```json
{
  "success": true,
  "message": "Curso eliminado exitosamente",
  "data": null
}
```

---

## 📝 Evaluaciones

### GET /api/evaluaciones

Listar todas las evaluaciones.

**Acceso**: Requiere autenticación

**Query Parameters**: Soporta paginación (ver sección Paginación)

**Response 200 OK**:
```json
{
  "success": true,
  "message": "Evaluaciones obtenidas exitosamente",
  "data": {
    "content": [
      {
        "id": 1,
        "nombre": "Parcial 1",
        "descripcion": "Primera evaluación",
        "tipo": "Examen",
        "cursoId": 1,
        "cursoNombre": "Cálculo Integral",
        "profesorId": 2,
        "estado": "Activa",
        "fechaInicio": "2026-03-15T08:00:00",
        "fechaFin": "2026-03-15T10:00:00",
        "duracionMinutos": 120,
        "intentosPermitidos": 1,
        "puntajeTotal": 100
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 25,
    "totalPages": 3
  }
}
```

### GET /api/evaluaciones/{id}

Obtener una evaluación específica.

**Acceso**: Requiere autenticación

**Response 200 OK**:
```json
{
  "success": true,
  "message": "Evaluación obtenida exitosamente",
  "data": {
    "id": 1,
    "nombre": "Parcial 1",
    "descripcion": "Primera evaluación",
    "tipo": "Examen",
    "cursoId": 1,
    "profesorId": 2,
    "estado": "Activa",
    "fechaInicio": "2026-03-15T08:00:00",
    "fechaFin": "2026-03-15T10:00:00",
    "duracionMinutos": 120,
    "preguntas": [
      {
        "id": 1,
        "texto": "¿Cuál es la derivada de x²?",
        "tipo": "Selección Múltiple",
        "puntaje": 10,
        "opciones": ["2x", "x", "2", "x²"]
      }
    ]
  }
}
```

### GET /api/evaluaciones/curso/{cursoId}

Obtener evaluaciones de un curso específico.

**Acceso**: Requiere autenticación

**Response 200 OK**: Similar a GET /api/evaluaciones

### GET /api/evaluaciones/estado/activas

Obtener solo evaluaciones activas.

**Acceso**: Requiere autenticación

**Response 200 OK**: Similar a GET /api/evaluaciones

### POST /api/evaluaciones

Crear una nueva evaluación.

**Acceso**: Requiere rol `maestro` o `coordinador`

**Request Body**:
```json
{
  "nombre": "Quiz 1",
  "descripcion": "Evaluación rápida",
  "tipo": "Quiz",
  "cursoId": 1,
  "profesorId": 2,
  "fechaInicio": "2026-03-20T08:00:00",
  "fechaFin": "2026-03-20T09:00:00",
  "duracionMinutos": 60,
  "intentosPermitidos": 2,
  "puntajeTotal": 50,
  "preguntas": [
    {
      "texto": "¿Cuál es la integral de 2x?",
      "tipo": "Selección Múltiple",
      "puntaje": 10,
      "opciones": ["x²", "2x²", "x", "2"],
      "respuestaCorrecta": "x²"
    }
  ]
}
```

**Response 201 Created**:
```json
{
  "success": true,
  "message": "Evaluación creada exitosamente",
  "data": {
    "id": 26,
    "nombre": "Quiz 1",
    "estado": "Programada",
    /* ... resto de campos ... */
  }
}
```

### POST /api/evaluaciones/{id}/publicar

Publicar una evaluación (cambiar estado a "Activa").

**Acceso**: Requiere rol `maestro` o `coordinador`

**Response 200 OK**:
```json
{
  "success": true,
  "message": "Evaluación publicada exitosamente",
  "data": {
    "id": 26,
    "estado": "Activa",
    /* ... resto de campos ... */
  }
}
```

### POST /api/evaluaciones/{id}/submit

Enviar respuestas de una evaluación.

**Acceso**: Requiere rol `estudiante`

**Request Body**:
```json
{
  "estudianteId": 4,
  "respuestas": [
    {
      "preguntaId": 1,
      "respuesta": "x²"
    },
    {
      "preguntaId": 2,
      "respuesta": "2x"
    }
  ]
}
```

**Response 200 OK**:
```json
{
  "success": true,
  "message": "Evaluación enviada exitosamente",
  "data": {
    "submissionId": 42,
    "evaluacionId": 1,
    "estudianteId": 4,
    "estado": "Completa",
    "fechaEnvio": "2026-03-15T09:45:00",
    "puntajeObtenido": 85,
    "puntajeTotal": 100
  }
}
```

### PUT /api/evaluaciones/{id}

Actualizar una evaluación.

**Acceso**: Requiere rol `maestro` o `coordinador`

**Request Body**: Similar a POST /api/evaluaciones

**Response 200 OK**: Similar a POST /api/evaluaciones

### DELETE /api/evaluaciones/{id}

Eliminar una evaluación.

**Acceso**: Requiere rol `coordinador`

**Response 200 OK**:
```json
{
  "success": true,
  "message": "Evaluación eliminada exitosamente",
  "data": null
}
```

---

## 📮 PQRS

### GET /api/pqrs

Listar todos los PQRS.

**Acceso**: Requiere autenticación

**Query Parameters**: Soporta paginación

**Response 200 OK**:
```json
{
  "success": true,
  "message": "PQRS obtenidos exitosamente",
  "data": {
    "content": [
      {
        "id": 1,
        "tipo": "Pregunta",
        "asunto": "Duda sobre evaluación",
        "descripcion": "Tengo una duda sobre el parcial",
        "usuarioId": 4,
        "usuarioNombre": "Juan Estudiante",
        "estado": "Pendiente",
        "fechaCreacion": "2026-03-10T14:30:00",
        "fechaRespuesta": null,
        "respuesta": null
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 8,
    "totalPages": 1
  }
}
```

### GET /api/pqrs/{id}

Obtener un PQRS específico.

**Acceso**: Requiere autenticación

**Response 200 OK**:
```json
{
  "success": true,
  "message": "PQRS obtenido exitosamente",
  "data": {
    "id": 1,
    "tipo": "Pregunta",
    "asunto": "Duda sobre evaluación",
    "descripcion": "Tengo una duda sobre el parcial",
    "usuarioId": 4,
    "usuarioNombre": "Juan Estudiante",
    "estado": "Resuelta",
    "fechaCreacion": "2026-03-10T14:30:00",
    "fechaRespuesta": "2026-03-11T09:15:00",
    "respuesta": "La evaluación se realizará el día 15 de marzo"
  }
}
```

### GET /api/pqrs/mis-pqrs

Obtener los PQRS del usuario autenticado (lee el ID desde el JWT).

**Acceso**: Requiere autenticación

**Response 200 OK**: Similar a GET /api/pqrs

### GET /api/pqrs/usuario/{usuarioId}

Obtener PQRS de un usuario específico.

**Acceso**: Requiere autenticación

**Response 200 OK**: Similar a GET /api/pqrs

### GET /api/pqrs/estado/{estado}

Obtener PQRS por estado.

**Acceso**: Requiere autenticación

**Estados válidos**: `Pendiente`, `En Proceso`, `Resuelta`, `Cerrada`

**Response 200 OK**: Similar a GET /api/pqrs

### POST /api/pqrs

Crear un nuevo PQRS.

**Acceso**: Requiere autenticación (todos los roles)

**Request Body**:
```json
{
  "tipo": "Sugerencia",
  "asunto": "Mejorar horarios",
  "descripcion": "Sería bueno tener más flexibilidad en los horarios",
  "usuarioId": 4
}
```

**Tipos válidos**: `Petición`, `Queja`, `Reclamo`, `Sugerencia`

**Response 201 Created**:
```json
{
  "success": true,
  "message": "PQRS creado exitosamente",
  "data": {
    "id": 9,
    "tipo": "Sugerencia",
    "asunto": "Mejorar horarios",
    "descripcion": "Sería bueno tener más flexibilidad en los horarios",
    "usuarioId": 4,
    "estado": "Pendiente",
    "fechaCreacion": "2026-03-12T10:20:00"
  }
}
```

### PUT /api/pqrs/{id}

Actualizar un PQRS (cambiar estado o agregar respuesta).

**Acceso**: Requiere rol `maestro` o `coordinador`

**Request Body**:
```json
{
  "estado": "Resuelta",
  "respuesta": "Hemos tomado en cuenta tu sugerencia"
}
```

**Response 200 OK**:
```json
{
  "success": true,
  "message": "PQRS actualizado exitosamente",
  "data": { /* PQRS actualizado */ }
}
```

### DELETE /api/pqrs/{id}

Eliminar un PQRS.

**Acceso**: Requiere rol `coordinador`

**Response 200 OK**:
```json
{
  "success": true,
  "message": "PQRS eliminado exitosamente",
  "data": null
}
```

---

## 📊 Calificaciones

### GET /api/calificaciones/estudiante/{estudianteId}

Obtener calificaciones de un estudiante.

**Acceso**: Requiere autenticación

**Response 200 OK**:
```json
{
  "success": true,
  "message": "Calificaciones obtenidas exitosamente",
  "data": [
    {
      "id": 1,
      "evaluacionId": 1,
      "evaluacionNombre": "Parcial 1",
      "cursoNombre": "Cálculo Integral",
      "estudianteId": 4,
      "puntajeObtenido": 85,
      "puntajeTotal": 100,
      "porcentaje": 85.0,
      "fecha": "2026-03-15T10:00:00"
    }
  ]
}
```

### GET /api/calificaciones/evaluacion/{evaluacionId}

Obtener todas las calificaciones de una evaluación.

**Acceso**: Requiere rol `maestro` o `coordinador`

**Response 200 OK**: Similar a GET /api/calificaciones/estudiante/{estudianteId}

---

## 📄 Paginación y Filtros

Todos los endpoints GET que retornan listas soportan los siguientes parámetros:

### Parámetros de Paginación

| Parámetro | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| `page` | Integer | 0 | Número de página (0-indexado) |
| `size` | Integer | 10 | Registros por página |
| `sort` | String | - | Campo para ordenar |
| `direction` | String | ASC | Dirección de ordenamiento (ASC o DESC) |

### Ejemplo de Uso

```bash
# Página 2, 20 registros, ordenado por nombre descendente
GET /api/cursos?page=1&size=20&sort=nombre&direction=DESC

# Primera página con 5 registros
GET /api/evaluaciones?page=0&size=5

# Ordenar por fecha de creación
GET /api/pqrs?sort=fechaCreacion&direction=DESC
```

### Respuesta Paginada

```json
{
  "success": true,
  "message": "Datos obtenidos exitosamente",
  "data": {
    "content": [ /* array de elementos */ ],
    "page": 1,
    "size": 20,
    "totalElements": 45,
    "totalPages": 3,
    "first": false,
    "last": false
  }
}
```

---

## 🔢 Códigos de Respuesta

| Código | Descripción |
|--------|-------------|
| 200 | OK - Solicitud exitosa |
| 201 | Created - Recurso creado exitosamente |
| 400 | Bad Request - Datos inválidos o faltantes |
| 401 | Unauthorized - Token inválido o faltante |
| 403 | Forbidden - Sin permisos para esta acción |
| 404 | Not Found - Recurso no encontrado |
| 409 | Conflict - Conflicto con el estado actual |
| 500 | Internal Server Error - Error del servidor |

### Formato de Error

```json
{
  "success": false,
  "message": "Descripción del error",
  "data": {
    "campo": "Mensaje de validación específico"
  }
}
```

---

## 💡 Ejemplos de Uso

### Flujo Completo: Login y Crear Evaluación

```bash
# 1. Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"profesor@quimbaya.edu.co","password":"password123","role":"maestro"}' \
  | jq -r '.data.token')

# 2. Crear evaluación
curl -X POST http://localhost:8080/api/evaluaciones \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Quiz 1",
    "tipo": "Quiz",
    "cursoId": 1,
    "profesorId": 2,
    "fechaInicio": "2026-03-20T08:00:00",
    "fechaFin": "2026-03-20T09:00:00",
    "duracionMinutos": 60
  }'

# 3. Publicar evaluación
curl -X POST http://localhost:8080/api/evaluaciones/26/publicar \
  -H "Authorization: Bearer $TOKEN"
```

### Flujo: Estudiante Responde Evaluación

```bash
# 1. Login como estudiante
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"estudiante@quimbaya.edu.co","password":"password123","role":"estudiante"}' \
  | jq -r '.data.token')

# 2. Ver evaluaciones activas
curl -X GET http://localhost:8080/api/evaluaciones/estado/activas \
  -H "Authorization: Bearer $TOKEN"

# 3. Enviar respuestas
curl -X POST http://localhost:8080/api/evaluaciones/1/submit \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "estudianteId": 4,
    "respuestas": [
      {"preguntaId": 1, "respuesta": "x²"},
      {"preguntaId": 2, "respuesta": "2x"}
    ]
  }'
```

---

## 📖 Documentación Interactiva

Para explorar y probar los endpoints de forma interactiva, accede a Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

---

**Última actualización**: Marzo 2026  
**Versión API**: 1.0.0
