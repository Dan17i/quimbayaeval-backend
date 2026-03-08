# QuimbayaEVAL Backend - Guía de Integración Frontend

Este documento describe cómo integrar el frontend React con este backend Spring Boot.

## 1. Configuración Inicial

### 1.1 Variables de Entorno del Frontend

En tu proyecto React, crear/actualizar `.env`:

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_API_TIMEOUT=30000
```

### 1.2 Instalación de Dependencias

```bash
# En el proyecto backend
mvn clean install

# En el proyecto frontend  
npm install
```

## 2. Iniciar Servicios

### Opción A: Con Docker Compose (Recomendado)

```bash
# En la carpeta del backend
docker-compose up --build

# La base de datos se cargará automáticamente con el schema.sql
# Backend disponible en: http://localhost:8080
# Base de datos en: localhost:5432
```

### Opción B: Local

**terminal 1 - Base de datos:**
```bash
# Asegúrate de tener PostgreSQL instalado
# Crear base de datos:
createdb quimbayaeval

# O con psql:
psql -U postgres -c "CREATE DATABASE quimbayaeval;"

# Cargar esquema:
psql -U postgres -d quimbayaeval < src/main/resources/db/schema.sql
```

**terminal 2 - Backend:**
```bash
# Variables de entorno
export POSTGRES_PASSWORD=postgres
export JWT_SECRET=tu-clave-secreta-...

# Ejecutar
mvn spring-boot:run
```

**terminal 3 - Frontend:**
```bash
npm run dev
# Accesible en http://localhost:5173
```

## 3. Cambios en AuthContext del Frontend

El backend USA JWT para autenticación. Necesitas actualizar tu AuthContext:

### 3.1 Guardar Token

```typescript
// src/contexts/AuthContext.tsx

const login = async (email: string, password: string, role: UserRole) => {
  const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password, role })
  });

  const data = await response.json();

  if (data.success) {
    const { user, token } = data.data;
    
    // Guardar usuario
    setUser(user);
    
    // Guardar token en localStorage
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(user));
  }
};
```

### 3.2 Interceptor HTTP con Authorization

```typescript
// src/services/api.ts (crear archivo nuevo)

export const apiCall = async (endpoint: string, options: RequestInit = {}) => {
  const token = localStorage.getItem('token');
  
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(
    `${import.meta.env.VITE_API_BASE_URL}${endpoint}`,
    { ...options, headers }
  );

  // Si 401 - token expirado, logout
  if (response.status === 401) {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/login';
  }

  return response.json();
};
```

### 3.3 Usar en Hooks

```typescript
// Antes:
const [cursos] = useState(mockCursos);

// Después:
const [cursos, setCursos] = useState([]);

useEffect(() => {
  apiCall('/cursos')
    .then(data => {
      if (data.success) {
        setCursos(data.data);
      }
    });
}, []);
```

## 4. Endpoints Disponibles

### 4.1 Autenticación

```
POST /api/auth/login
Body: { "email": "user@example.com", "password": "secret", "role": "estudiante" }
Response: { "success": true, "data": { "token": "...", "user": {...} } }

POST /api/auth/register
Body: { "email": "new@example.com", "password": "secret", "role": "estudiante" }
Response: similar a login

GET /api/auth/validate
Header: Authorization: Bearer <token>
```

### 4.2 Evaluaciones

```
GET /api/evaluaciones
GET /api/evaluaciones/{id}
GET /api/evaluaciones/curso/{cursoId}
GET /api/evaluaciones/estado/activas
POST /api/evaluaciones { "nombre": "...", "cursoId": 1, ... }
PUT /api/evaluaciones/{id} { campos a actualizar }
DELETE /api/evaluaciones/{id}
POST /api/evaluaciones/{id}/publicar
POST /api/evaluaciones/{id}/submit { respuestas del estudiante }
```

### 4.3 Cursos

```
GET /api/cursos
GET /api/cursos/{id}
GET /api/cursos/profesor/{profesorId}
POST /api/cursos { "codigo": "MAT-301", "nombre": "...", "profesorId": 1 }
PUT /api/cursos/{id}
DELETE /api/cursos/{id}
```

### 4.4 PQRS

```
GET /api/pqrs
GET /api/pqrs/{id}
GET /api/pqrs/usuario/{usuarioId}
```

### 4.5 Preguntas

```
GET /api/preguntas
GET /api/preguntas/{id}
GET /api/preguntas/evaluacion/{evaluacionId}
POST /api/preguntas     { /* JSON pregunta */ }
PUT  /api/preguntas/{id}
DELETE /api/preguntas/{id}
```

### 4.6 Submissions

```
GET /api/submissions
GET /api/submissions/{id}
GET /api/submissions/evaluacion/{evaluacionId}
GET /api/submissions/estudiante/{estudianteId}
POST /api/submissions    { /* JSON submission */ }
PUT  /api/submissions/{id}
DELETE /api/submissions/{id}
```

### 4.7 Calificaciones

```
GET /api/calificaciones
GET /api/calificaciones/{id}
GET /api/calificaciones/submission/{submissionId}
POST /api/calificaciones   { /* JSON calificacion */ }
PUT  /api/calificaciones/{id}
DELETE /api/calificaciones/{id}
```

GET /api/pqrs/estado/{estado}
POST /api/pqrs { "tipo": "Pregunta", "asunto": "...", "usuarioId": 1, ... }
PUT /api/pqrs/{id}
DELETE /api/pqrs/{id}
POST /api/pqrs/{id}/respond { "respuesta": "...", "respondidoPorId": 2 }
```

## 5. Ejemplos de Uso

### Login y Obtener Evaluaciones

```typescript
// 1. Login
const loginRes = await apiCall('/auth/login', {
  method: 'POST',
  body: JSON.stringify({
    email: 'estudiante@example.com',
    password: 'secret',
    role: 'estudiante'
  })
});

const token = loginRes.data.token;

// 2. Obtener evaluaciones del estudiante
const evalsRes = await apiCall('/evaluaciones/estado/activas', {
  headers: { 'Authorization': `Bearer ${token}` }
});

console.log(evalsRes.data); // Array de evaluaciones
```

## 6. Swagger/OpenAPI

Documentación interactiva disponible en:
```
http://localhost:8080/swagger-ui.html
```

## 7. Troubleshooting

### Error: "CORS error"
- Verificar que CORS está habilitado en `SecurityConfig.java`
- Asegurar que el origen del frontend está permitido
- Headers necesarios: `Content-Type`, `Authorization`

### Error: "Token inválido o expirado"
- El token expira en 24 horas
- Implementar refresh tokens para extender sesión
- O re-login automático

### Error: "Connection refused"
- Verificar que PostgreSQL está corriendo
- Backend debe estar en puerto 8080
- Revisar logs: `docker-compose logs backend`

### Base de datos vacía
- Ejecutar: `docker-compose exec postgres psql -U postgres -d quimbayaeval < schema.sql`
- O reiniciar containers: `docker-compose down && docker-compose up`

## 8. Próximas Implementaciones

Tareas pendientes para completar el backend:

- [ ] DAO para Preguntas y Respuestas
- [ ] DAO para Calificaciones y Resultados
- [ ] Servicio y Controlador de Calificaciones
- [ ] Servicio y DAO de Inscripciones
- [ ] Endpoint de reportes
- [ ] Endpoint de usuarios (CRUD)
- [ ] Tests unitarios e integración
- [ ] Validación más robusta
- [ ] Manejo de archivos (para reportes PDF, etc)

## 9. Contacto y Soporte

Para preguntas o issues, referirse a:
- Frontend: https://www.figma.com/design/P2CRl5kxgRyecFqPkuye3k/Sitemap-y-Flujos-QuimbayaEVAL
- Documentación: README.md (backend)
