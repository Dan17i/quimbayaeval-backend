# 📡 Documentación de API - QuimbayaEVAL

Base URL: `http://localhost:8080/api`

Todos los endpoints protegidos requieren header:
```
Authorization: Bearer <token>
```

---

## Autenticación

### POST /api/auth/login
**Acceso**: Público

**Body**:
```json
{ "email": "estudiante@quimbaya.edu.co", "password": "password", "role": "estudiante" }
```
> `role` es obligatorio. Valores: `estudiante`, `maestro`, `coordinador`

**Response 200**:
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGci...",
    "type": "Bearer",
    "id": 4,
    "name": "Juan Estudiante",
    "email": "estudiante@quimbaya.edu.co",
    "role": "estudiante"
  }
}
```

### POST /api/auth/register
**Acceso**: Público

**Body**:
```json
{ "name": "Nuevo Usuario", "email": "nuevo@quimbaya.edu.co", "password": "password", "role": "estudiante" }
```

---

## Usuarios

### GET /api/users
Lista usuarios activos. Sin password en respuesta.

**Query params**: `?role=maestro` (opcional) — filtra por rol

**Acceso**: Autenticado

**Response**:
```json
{
  "data": [
    { "id": 2, "name": "María Profesora", "email": "profesor@quimbaya.edu.co", "role": "maestro", "active": true, "fotoUrl": null }
  ]
}
```

### GET /api/users/me
Perfil del usuario autenticado. Incluye cursos según rol.

**Acceso**: Autenticado

**Response**:
```json
{
  "data": {
    "id": 4,
    "name": "Juan Estudiante",
    "email": "estudiante@quimbaya.edu.co",
    "role": "estudiante",
    "fotoUrl": "https://i.imgur.com/abc.jpg",
    "cursos": [
      { "id": 1, "codigo": "MAT101", "nombre": "Matemáticas", "descripcion": "...", "profesorId": 2 }
    ]
  }
}
```
> Estudiante: `cursos` = cursos inscritos. Maestro: `cursos` = cursos asignados. Coordinador: sin campo `cursos`.

### PUT /api/users/me
Editar nombre y foto de perfil.

**Acceso**: Autenticado

**Body**:
```json
{ "name": "Nuevo Nombre", "fotoUrl": "https://i.imgur.com/abc.jpg" }
```
> `fotoUrl` es una URL externa (Imgur, Gravatar, etc.). No hay upload de archivos.

### PUT /api/users/me/password
Cambiar contraseña.

**Acceso**: Autenticado

**Body**:
```json
{ "passwordActual": "password", "passwordNueva": "nueva123" }
```
> `passwordNueva` debe tener al menos 6 caracteres.

### PATCH /api/users/{id}/status
Activar o bloquear usuario.

**Acceso**: Coordinador

**Body**:
```json
{ "status": "activo" }
```
> Valores: `activo`, `bloqueado`

### DELETE /api/users/{id}
Soft delete — marca `active = false`.

**Acceso**: Coordinador

---

## Cursos

### GET /api/cursos
Lista todos los cursos.

**Acceso**: Autenticado

**Query params**: `?page=0&size=10&sort=nombre&direction=ASC`

### GET /api/cursos/{id}
Obtener curso por ID.

### POST /api/cursos
Crear curso.

**Acceso**: Coordinador

**Body**:
```json
{ "codigo": "MAT101", "nombre": "Matemáticas", "descripcion": "...", "profesorId": 2 }
```

### PUT /api/cursos/{id}
Actualizar curso. **Acceso**: Coordinador

### DELETE /api/cursos/{id}
Eliminar curso. **Acceso**: Coordinador

### GET /api/cursos/{id}/estudiantes
Lista estudiantes matriculados en el curso.

### POST /api/cursos/{id}/estudiantes
Matricular estudiante en el curso.

**Body**:
```json
{ "estudianteId": 4 }
```

### DELETE /api/cursos/{id}/estudiantes/{estudianteId}
Desmatricular estudiante del curso.

---

## Evaluaciones

### GET /api/evaluaciones
Lista evaluaciones con filtros opcionales.

**Query params**:
- `?profesorId={id}` — evaluaciones de un profesor
- `?cursoId={id}` — evaluaciones de un curso
- `?estado=Activa` — por estado
- `?tipo=Examen` — por tipo
- `?publicada=true`
- `?page=0&size=10&sort=nombre`

**Tipos válidos**: `Examen`, `Quiz`, `Taller`, `Proyecto`, `Tarea`

**Estados válidos**: `Borrador`, `Programada`, `Activa`, `Cerrada`

### GET /api/evaluaciones/{id}
Obtener evaluación por ID.

### GET /api/evaluaciones/curso/{cursoId}
Evaluaciones de un curso.

### GET /api/evaluaciones/estado/activas
Solo evaluaciones activas (publicada = true, estado = Activa).

### POST /api/evaluaciones
Crear evaluación.

**Acceso**: Maestro / Coordinador

**Body**:
```json
{
  "nombre": "Quiz 1",
  "descripcion": "Evaluación rápida",
  "tipo": "Quiz",
  "cursoId": 1,
  "profesorId": 2,
  "duracionMinutos": 60,
  "intentosPermitidos": 1
}
```
> Estado inicial: `Borrador`. `publicada` = false.

### POST /api/evaluaciones/{id}/publicar
Publica la evaluación (estado → `Activa`, publicada → true).

**Acceso**: Maestro / Coordinador

### PUT /api/evaluaciones/{id}
Actualizar evaluación. **Acceso**: Maestro / Coordinador

### DELETE /api/evaluaciones/{id}
Eliminar evaluación. **Acceso**: Coordinador

---

## Preguntas

### GET /api/preguntas/evaluacion/{evaluacionId}
Lista preguntas de una evaluación.

**Response**:
```json
{
  "data": [
    {
      "id": 1,
      "evaluacionId": 1,
      "enunciado": "¿Cuál es la derivada de x²?",
      "tipo": "seleccion_multiple",
      "puntuacion": 10.0,
      "orden": 1,
      "opcionesJson": "[\"2x\",\"x\",\"2\",\"x²\"]",
      "respuestaCorrectaJson": "\"2x\""
    }
  ]
}
```

**Tipos de pregunta**: `seleccion_multiple`, `verdadero_falso`, `respuesta_corta`, `ensayo`

> `opcionesJson` es un string JSON serializado: `"[\"Opción A\",\"Opción B\"]"`

### POST /api/preguntas
Crear pregunta.

**Acceso**: Maestro / Coordinador

**Body**:
```json
{
  "evaluacionId": 1,
  "enunciado": "¿Cuál es la derivada de x²?",
  "tipo": "seleccion_multiple",
  "puntuacion": 10.0,
  "orden": 1,
  "opcionesJson": "[\"2x\",\"x\",\"2\",\"x²\"]",
  "respuestaCorrectaJson": "\"2x\""
}
```

---

## Submissions

### GET /api/submissions/evaluacion/{evaluacionId}
Lista submissions de una evaluación (para calificar).

**Acceso**: Maestro / Coordinador

### POST /api/submissions
Enviar respuestas de una evaluación (estudiante presenta).

**Acceso**: Estudiante

**Body**:
```json
{
  "evaluacionId": 1,
  "estudianteId": 4,
  "respuestasJson": "{\"1\":\"2x\",\"2\":\"Verdadero\"}"
}
```

---

## Calificaciones

### GET /api/calificaciones/estudiante/{estudianteId}
Calificaciones de un estudiante.

### GET /api/calificaciones/evaluacion/{evaluacionId}
Calificaciones de una evaluación.

### POST /api/calificaciones
Calificar una submission.

**Acceso**: Maestro / Coordinador

**Body**:
```json
{
  "submissionId": 1,
  "estudianteId": 4,
  "evaluacionId": 1,
  "puntuacionObtenida": 42.5,
  "puntuacionMaxima": 50.0,
  "comentarios": "Buen trabajo"
}
```
> `calificadoPorId` se extrae automáticamente del JWT — no es necesario enviarlo.

---

## Resultados

### GET /api/resultados/mis-resultados
Resultados del estudiante autenticado (ID desde JWT).

**Acceso**: Autenticado

**Response**:
```json
{
  "data": [
    {
      "id": 1,
      "evaluacionNombre": "Parcial 1",
      "cursoNombre": "Matemáticas",
      "profesorNombre": "María Profesora",
      "puntuacionTotal": 42.5,
      "puntuacionMaxima": 50.0,
      "porcentaje": 85.0,
      "notaEscala": 4.40,
      "createdAt": "2026-03-15T10:00:00"
    }
  ]
}
```

### GET /api/resultados/curso/{cursoId}
Notas de todos los estudiantes del curso.

**Acceso**: Autenticado (docentes)

**Response**:
```json
{
  "data": [
    {
      "estudianteNombre": "Juan Estudiante",
      "estudianteEmail": "estudiante@quimbaya.edu.co",
      "evaluacionNombre": "Parcial 1",
      "cursoNombre": "Matemáticas",
      "profesorNombre": "María Profesora",
      "puntuacionTotal": 42.5,
      "puntuacionMaxima": 50.0,
      "porcentaje": 85.0,
      "notaEscala": 4.40,
      "estadoAprobacion": "Aprobado"
    }
  ]
}
```

### GET /api/resultados/curso/{cursoId}/resumen
Resumen estadístico por evaluación del curso.

**Acceso**: Autenticado (coordinadores)

**Response**:
```json
{
  "data": [
    {
      "evaluacionId": 1,
      "evaluacionNombre": "Parcial 1",
      "promedioGrupo": 78.50,
      "promedioEscala": 4.14,
      "totalEstudiantes": 30,
      "aprobados": 25,
      "reprobados": 5
    }
  ]
}
```

> Fórmula nota escala colombiana: `notaEscala = 1 + (porcentaje / 100) * 4`

### GET /api/resultados/evaluacion/{evaluacionId}
Resultados de todos los estudiantes de una evaluación.

### GET /api/resultados/submission/{submissionId}
Resultado de una submission específica.

---

## PQRS

### GET /api/pqrs
Lista todos los PQRS. **Acceso**: Autenticado

### GET /api/pqrs/{id}
Obtener PQRS por ID.

### GET /api/pqrs/mis-pqrs
PQRS del usuario autenticado (ID desde JWT).

### GET /api/pqrs/usuario/{usuarioId}
PQRS de un usuario específico.

### GET /api/pqrs/estado/{estado}
PQRS por estado. Valores: `Pendiente`, `En Proceso`, `Resuelta`, `Cerrada`

### POST /api/pqrs
Crear PQRS.

**Acceso**: Autenticado

**Body**:
```json
{ "tipo": "Sugerencia", "asunto": "Mejorar horarios", "descripcion": "..." }
```
> `usuarioId` se extrae del JWT automáticamente. `estado` por defecto: `Pendiente`.
> Tipos: `Petición`, `Queja`, `Reclamo`, `Sugerencia`, `Pregunta`

### PUT /api/pqrs/{id}
Actualizar estado o agregar respuesta. **Acceso**: Maestro / Coordinador

**Body**:
```json
{ "estado": "Resuelta", "respuesta": "Hemos tomado en cuenta tu sugerencia" }
```

### DELETE /api/pqrs/{id}
Eliminar PQRS. **Acceso**: Coordinador

---

## Códigos de Respuesta

| Código | Descripción |
|--------|-------------|
| 200 | OK |
| 201 | Creado |
| 400 | Datos inválidos |
| 401 | Token inválido o faltante |
| 403 | Sin permisos |
| 404 | No encontrado |
| 500 | Error del servidor |

---

**Versión**: 1.2.0 — Última actualización: Marzo 14, 2026
