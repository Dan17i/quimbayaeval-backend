# Guía de Integración Frontend - QuimbayaEVAL Backend

## Información General

**URL Base del API**: `http://localhost:8080/api`

**Formato de Respuesta**: Todas las respuestas siguen el formato estándar:

```json
{
  "success": true,
  "message": "Mensaje descriptivo",
  "data": { /* datos de respuesta */ }
}
```

## Configuración CORS

El backend está configurado para aceptar peticiones desde:
- `http://localhost:5173` (Vite)
- `http://localhost:3000` (React/Next.js)

Para agregar más orígenes, editar `application.yml`:

```yaml
cors:
  allowed-origins: http://localhost:5173,http://localhost:3000,http://tu-dominio.com
```

## Autenticación

### 1. Login

**Endpoint**: `POST /api/auth/login`

**Request Body**:
```json
{
  "email": "usuario@example.com",
  "password": "contraseña",
  "role": "estudiante" // o "maestro"
}
```

**Response**:
```json
{
  "success": true,
  "message": "Login exitoso",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "type": "Bearer",
    "id": 1,
    "name": "Nombre Usuario",
    "email": "usuario@example.com",
    "role": "estudiante"
  }
}
```

**Ejemplo con Fetch**:
```javascript
const login = async (email, password, role) => {
  const response = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ email, password, role })
  });
  
  const data = await response.json();
  
  if (data.success) {
    // Guardar token
    localStorage.setItem('token', data.data.token);
    localStorage.setItem('user', JSON.stringify(data.data));
    return data.data;
  }
  
  throw new Error(data.message);
};
```

**Ejemplo con Axios**:
```javascript
import axios from 'axios';

const login = async (email, password, role) => {
  try {
    const { data } = await axios.post('http://localhost:8080/api/auth/login', {
      email,
      password,
      role
    });
    
    if (data.success) {
      localStorage.setItem('token', data.data.token);
      localStorage.setItem('user', JSON.stringify(data.data));
      return data.data;
    }
  } catch (error) {
    throw new Error(error.response?.data?.message || 'Error en login');
  }
};
```

### 2. Registro

**Endpoint**: `POST /api/auth/register`

**Request Body**:
```json
{
  "name": "Nombre Completo",
  "email": "nuevo@example.com",
  "password": "contraseña123",
  "role": "estudiante"
}
```

### 3. Uso del Token

Todas las peticiones autenticadas deben incluir el token en el header:

```javascript
const headers = {
  'Content-Type': 'application/json',
  'Authorization': `Bearer ${localStorage.getItem('token')}`
};
```

## Configuración de Cliente HTTP

### Axios Interceptor (Recomendado)

```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  }
});

// Interceptor para agregar token automáticamente
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Interceptor para manejar errores
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expirado o inválido
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
```

### Fetch Wrapper

```javascript
const API_BASE = 'http://localhost:8080/api';

const fetchAPI = async (endpoint, options = {}) => {
  const token = localStorage.getItem('token');
  
  const config = {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` }),
      ...options.headers,
    },
  };
  
  const response = await fetch(`${API_BASE}${endpoint}`, config);
  const data = await response.json();
  
  if (!response.ok) {
    if (response.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    throw new Error(data.message || 'Error en la petición');
  }
  
  return data;
};

export default fetchAPI;
```

## Endpoints Principales

### Autenticación

#### Login
```javascript
// POST /api/auth/login
const login = async (email, password, role) => {
  const response = await api.post('/auth/login', { email, password, role });
  return response.data.data;
};
```

#### Registro
```javascript
// POST /api/auth/register
const register = async (userData) => {
  const response = await api.post('/auth/register', {
    name: "Nombre Completo",
    email: "nuevo@example.com",
    password: "contraseña123",
    role: "estudiante" // o "maestro"
  });
  return response.data.data;
};
```

### Cursos

#### Listar cursos
```javascript
// GET /api/cursos
const getCursos = async () => {
  const response = await api.get('/cursos');
  return response.data.data;
};
```

#### Obtener curso por ID
```javascript
// GET /api/cursos/{id}
const getCurso = async (id) => {
  const response = await api.get(`/cursos/${id}`);
  return response.data.data;
};
```

#### Obtener cursos por profesor
```javascript
// GET /api/cursos/profesor/{profesorId}
const getCursosPorProfesor = async (profesorId) => {
  const response = await api.get(`/cursos/profesor/${profesorId}`);
  return response.data.data;
};
```

#### Crear curso (solo maestros)
```javascript
// POST /api/cursos
const crearCurso = async (curso) => {
  const response = await api.post('/cursos', {
    codigo: "MAT101",
    nombre: "Matemáticas Básicas",
    descripcion: "Curso de matemáticas",
    profesorId: 1
  });
  return response.data.data;
};
```

#### Actualizar curso
```javascript
// PUT /api/cursos/{id}
const actualizarCurso = async (id, curso) => {
  const response = await api.put(`/cursos/${id}`, curso);
  return response.data.data;
};
```

#### Eliminar curso
```javascript
// DELETE /api/cursos/{id}
const eliminarCurso = async (id) => {
  const response = await api.delete(`/cursos/${id}`);
  return response.data;
};
```

### Evaluaciones

#### Listar evaluaciones con filtros y paginación
```javascript
// GET /api/evaluaciones?estado=Activa&tipo=Quiz&page=0&size=10&sort=nombre&direction=ASC
const getEvaluaciones = async (filtros = {}) => {
  const params = new URLSearchParams(filtros);
  const response = await api.get(`/evaluaciones?${params}`);
  return response.data.data;
};

// Ejemplo de uso con todos los filtros disponibles
const evaluaciones = await getEvaluaciones({
  estado: 'Activa',        // Borrador, Programada, Activa, Cerrada
  tipo: 'Quiz',            // Examen, Quiz, Taller, Proyecto, Tarea
  cursoId: 1,
  nombre: 'Parcial',       // Búsqueda por nombre
  publicada: true,
  page: 0,
  size: 10,
  sort: 'nombre',          // id, nombre, tipo, estado, createdAt
  direction: 'ASC'         // ASC o DESC
});
```

#### Obtener evaluación por ID
```javascript
// GET /api/evaluaciones/{id}
const getEvaluacion = async (id) => {
  const response = await api.get(`/evaluaciones/${id}`);
  return response.data.data;
};
```

#### Obtener evaluaciones por curso
```javascript
// GET /api/evaluaciones/curso/{cursoId}
const getEvaluacionesPorCurso = async (cursoId) => {
  const response = await api.get(`/evaluaciones/curso/${cursoId}`);
  return response.data.data;
};
```

#### Obtener evaluaciones activas
```javascript
// GET /api/evaluaciones/estado/activas
const getEvaluacionesActivas = async () => {
  const response = await api.get('/evaluaciones/estado/activas');
  return response.data.data;
};
```

#### Crear evaluación
```javascript
// POST /api/evaluaciones
const crearEvaluacion = async (evaluacion) => {
  const response = await api.post('/evaluaciones', {
    nombre: "Parcial 1",
    descripcion: "Primera evaluación del semestre",
    cursoId: 1,
    profesorId: 1,
    tipo: "Examen",              // Examen, Quiz, Taller, Proyecto, Tarea
    estado: "Borrador",          // Borrador, Programada, Activa, Cerrada
    deadline: "2024-12-31T23:59:59",
    duracionMinutos: 60,
    intentosPermitidos: 1,
    publicada: false
  });
  return response.data.data;
};
```

#### Actualizar evaluación
```javascript
// PUT /api/evaluaciones/{id}
const actualizarEvaluacion = async (id, evaluacion) => {
  const response = await api.put(`/evaluaciones/${id}`, evaluacion);
  return response.data.data;
};
```

#### Eliminar evaluación
```javascript
// DELETE /api/evaluaciones/{id}
const eliminarEvaluacion = async (id) => {
  const response = await api.delete(`/evaluaciones/${id}`);
  return response.data;
};
```

### Preguntas

#### Listar todas las preguntas
```javascript
// GET /api/preguntas
const getPreguntas = async () => {
  const response = await api.get('/preguntas');
  return response.data.data;
};
```

#### Obtener pregunta por ID
```javascript
// GET /api/preguntas/{id}
const getPregunta = async (id) => {
  const response = await api.get(`/preguntas/${id}`);
  return response.data.data;
};
```

#### Obtener preguntas de una evaluación
```javascript
// GET /api/preguntas/evaluacion/{evaluacionId}
const getPreguntasPorEvaluacion = async (evaluacionId) => {
  const response = await api.get(`/preguntas/evaluacion/${evaluacionId}`);
  return response.data.data;
};
```

#### Crear pregunta
```javascript
// POST /api/preguntas
const crearPregunta = async (pregunta) => {
  const response = await api.post('/preguntas', {
    evaluacionId: 1,
    enunciado: "¿Cuál es la capital de Colombia?",
    tipo: "seleccion_multiple",  // seleccion_multiple, verdadero_falso, respuesta_corta, ensayo
    puntuacion: 1.0,
    orden: 1,
    opcionesJson: JSON.stringify([
      { id: 1, texto: "Bogotá", correcta: true },
      { id: 2, texto: "Medellín", correcta: false },
      { id: 3, texto: "Cali", correcta: false },
      { id: 4, texto: "Barranquilla", correcta: false }
    ]),
    respuestaCorrectaJson: JSON.stringify({ respuesta: "Bogotá" })
  });
  return response.data.data;
};
```

#### Actualizar pregunta
```javascript
// PUT /api/preguntas/{id}
const actualizarPregunta = async (id, pregunta) => {
  const response = await api.put(`/preguntas/${id}`, pregunta);
  return response.data.data;
};
```

#### Eliminar pregunta
```javascript
// DELETE /api/preguntas/{id}
const eliminarPregunta = async (id) => {
  const response = await api.delete(`/preguntas/${id}`);
  return response.data;
};
```

### Submissions (Entregas de Evaluaciones)

#### Listar submissions
```javascript
// GET /api/submissions
const getSubmissions = async () => {
  const response = await api.get('/submissions');
  return response.data.data;
};
```

#### Obtener submission por ID
```javascript
// GET /api/submissions/{id}
const getSubmission = async (id) => {
  const response = await api.get(`/submissions/${id}`);
  return response.data.data;
};
```

#### Obtener submissions por evaluación
```javascript
// GET /api/submissions/evaluacion/{evaluacionId}
const getSubmissionsPorEvaluacion = async (evaluacionId) => {
  const response = await api.get(`/submissions/evaluacion/${evaluacionId}`);
  return response.data.data;
};
```

#### Obtener submissions por estudiante
```javascript
// GET /api/submissions/estudiante/{estudianteId}
const getSubmissionsPorEstudiante = async (estudianteId) => {
  const response = await api.get(`/submissions/estudiante/${estudianteId}`);
  return response.data.data;
};
```

#### Crear submission (entregar evaluación)
```javascript
// POST /api/submissions
const crearSubmission = async (submission) => {
  const response = await api.post('/submissions', {
    evaluacionId: 1,
    estudianteId: 1,
    respuestasJson: JSON.stringify({
      pregunta1: { respuesta: "Bogotá" },
      pregunta2: { respuesta: "B" }
    }),
    estado: "Enviada",
    intentoNumero: 1
  });
  return response.data.data;
};
```

#### Actualizar submission
```javascript
// PUT /api/submissions/{id}
const actualizarSubmission = async (id, submission) => {
  const response = await api.put(`/submissions/${id}`, submission);
  return response.data.data;
};
```

#### Eliminar submission
```javascript
// DELETE /api/submissions/{id}
const eliminarSubmission = async (id) => {
  const response = await api.delete(`/submissions/${id}`);
  return response.data;
};
```

### Calificaciones

#### Listar calificaciones
```javascript
// GET /api/calificaciones
const getCalificaciones = async () => {
  const response = await api.get('/calificaciones');
  return response.data.data;
};
```

#### Obtener calificación por ID
```javascript
// GET /api/calificaciones/{id}
const getCalificacion = async (id) => {
  const response = await api.get(`/calificaciones/${id}`);
  return response.data.data;
};
```

#### Obtener calificaciones por submission
```javascript
// GET /api/calificaciones/submission/{submissionId}
const getCalificacionesPorSubmission = async (submissionId) => {
  const response = await api.get(`/calificaciones/submission/${submissionId}`);
  return response.data.data;
};
```

#### Crear calificación
```javascript
// POST /api/calificaciones
const crearCalificacion = async (calificacion) => {
  const response = await api.post('/calificaciones', {
    submissionId: 1,
    puntuacionObtenida: 8.5,
    puntuacionMaxima: 10.0,
    retroalimentacion: "Buen trabajo, pero revisa la pregunta 3",
    calificadoPorId: 1
  });
  return response.data.data;
};
```

#### Actualizar calificación
```javascript
// PUT /api/calificaciones/{id}
const actualizarCalificacion = async (id, calificacion) => {
  const response = await api.put(`/calificaciones/${id}`, calificacion);
  return response.data.data;
};
```

#### Eliminar calificación
```javascript
// DELETE /api/calificaciones/{id}
const eliminarCalificacion = async (id) => {
  const response = await api.delete(`/calificaciones/${id}`);
  return response.data;
};
```

### PQRS

#### Listar todas las PQRS
```javascript
// GET /api/pqrs
const getPQRS = async () => {
  const response = await api.get('/pqrs');
  return response.data.data;
};
```

#### Obtener PQRS por ID
```javascript
// GET /api/pqrs/{id}
const getPQRSById = async (id) => {
  const response = await api.get(`/pqrs/${id}`);
  return response.data.data;
};
```

#### Obtener mis PQRS (del usuario autenticado)
```javascript
// GET /api/pqrs/mis-pqrs
const getMisPQRS = async () => {
  const response = await api.get('/pqrs/mis-pqrs');
  return response.data.data;
};
```

#### Obtener PQRS por estado
```javascript
// GET /api/pqrs/estado/{estado}
const getPQRSPorEstado = async (estado) => {
  // estado: Pendiente, En Proceso, Resuelta, Cerrada
  const response = await api.get(`/pqrs/estado/${estado}`);
  return response.data.data;
};
```

#### Crear PQRS
```javascript
// POST /api/pqrs
const crearPQRS = async (pqrs) => {
  const response = await api.post('/pqrs', {
    tipo: "Queja",              // Petición, Queja, Reclamo, Sugerencia
    asunto: "Problema con evaluación",
    descripcion: "No puedo acceder a la evaluación del curso",
    cursoId: 1
  });
  return response.data.data;
};
```

#### Actualizar PQRS
```javascript
// PUT /api/pqrs/{id}
const actualizarPQRS = async (id, pqrs) => {
  const response = await api.put(`/pqrs/${id}`, pqrs);
  return response.data.data;
};
```

#### Responder PQRS (solo maestros/admin)
```javascript
// PUT /api/pqrs/{id}/responder
const responderPQRS = async (id, respuesta) => {
  const response = await api.put(`/pqrs/${id}/responder`, {
    respuesta: "Hemos revisado tu caso y...",
    estado: "Resuelta"
  });
  return response.data.data;
};
```

#### Eliminar PQRS
```javascript
// DELETE /api/pqrs/{id}
const eliminarPQRS = async (id) => {
  const response = await api.delete(`/pqrs/${id}`);
  return response.data;
};
```

## Manejo de Errores

### Estructura de Error

```json
{
  "success": false,
  "message": "Descripción del error",
  "data": null
}
```

### Códigos de Estado HTTP

- `200 OK`: Operación exitosa
- `201 Created`: Recurso creado exitosamente
- `400 Bad Request`: Datos inválidos
- `401 Unauthorized`: No autenticado o token inválido
- `403 Forbidden`: No tiene permisos
- `404 Not Found`: Recurso no encontrado
- `500 Internal Server Error`: Error del servidor

### Ejemplo de Manejo de Errores

```javascript
const handleAPIError = (error) => {
  if (error.response) {
    // El servidor respondió con un código de error
    const { status, data } = error.response;
    
    switch (status) {
      case 400:
        return 'Datos inválidos: ' + data.message;
      case 401:
        localStorage.removeItem('token');
        window.location.href = '/login';
        return 'Sesión expirada';
      case 403:
        return 'No tienes permisos para esta acción';
      case 404:
        return 'Recurso no encontrado';
      case 500:
        return 'Error del servidor';
      default:
        return data.message || 'Error desconocido';
    }
  } else if (error.request) {
    // La petición se hizo pero no hubo respuesta
    return 'No se pudo conectar con el servidor';
  } else {
    // Error al configurar la petición
    return error.message;
  }
};

// Uso
try {
  const cursos = await getCursos();
} catch (error) {
  const errorMessage = handleAPIError(error);
  console.error(errorMessage);
  // Mostrar mensaje al usuario
}
```

## Ejemplos de Integración por Framework

### React

```javascript
// hooks/useAuth.js
import { useState, useEffect } from 'react';
import api from '../services/api';

export const useAuth = () => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      setUser(JSON.parse(storedUser));
    }
    setLoading(false);
  }, []);

  const login = async (email, password, role) => {
    try {
      const { data } = await api.post('/auth/login', { email, password, role });
      if (data.success) {
        localStorage.setItem('token', data.data.token);
        localStorage.setItem('user', JSON.stringify(data.data));
        setUser(data.data);
        return data.data;
      }
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Error en login');
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };

  return { user, loading, login, logout };
};
```

### Vue 3

```javascript
// composables/useAuth.js
import { ref, onMounted } from 'vue';
import api from '../services/api';

export const useAuth = () => {
  const user = ref(null);
  const loading = ref(true);

  onMounted(() => {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      user.value = JSON.parse(storedUser);
    }
    loading.value = false;
  });

  const login = async (email, password, role) => {
    try {
      const { data } = await api.post('/auth/login', { email, password, role });
      if (data.success) {
        localStorage.setItem('token', data.data.token);
        localStorage.setItem('user', JSON.stringify(data.data));
        user.value = data.data;
        return data.data;
      }
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Error en login');
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    user.value = null;
  };

  return { user, loading, login, logout };
};
```

### Angular

```typescript
// services/auth.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { map } from 'rxjs/operators';

interface User {
  id: number;
  name: string;
  email: string;
  role: string;
  token: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private userSubject: BehaviorSubject<User | null>;
  public user: Observable<User | null>;
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {
    const storedUser = localStorage.getItem('user');
    this.userSubject = new BehaviorSubject<User | null>(
      storedUser ? JSON.parse(storedUser) : null
    );
    this.user = this.userSubject.asObservable();
  }

  public get userValue(): User | null {
    return this.userSubject.value;
  }

  login(email: string, password: string, role: string) {
    return this.http.post<any>(`${this.apiUrl}/auth/login`, { email, password, role })
      .pipe(map(response => {
        if (response.success) {
          localStorage.setItem('token', response.data.token);
          localStorage.setItem('user', JSON.stringify(response.data));
          this.userSubject.next(response.data);
          return response.data;
        }
        throw new Error(response.message);
      }));
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.userSubject.next(null);
  }
}
```

## Rate Limiting

El API tiene límite de 100 peticiones por minuto por IP. El header `X-Rate-Limit-Remaining` indica cuántas peticiones quedan.

```javascript
api.interceptors.response.use(
  (response) => {
    const remaining = response.headers['x-rate-limit-remaining'];
    if (remaining && parseInt(remaining) < 10) {
      console.warn('Pocas peticiones restantes:', remaining);
    }
    return response;
  }
);
```

## Testing de Integración

### Ejemplo con Jest

```javascript
import api from './api';

describe('API Integration', () => {
  let token;

  beforeAll(async () => {
    // Login para obtener token
    const { data } = await api.post('/auth/login', {
      email: 'test@example.com',
      password: 'password',
      role: 'estudiante'
    });
    token = data.data.token;
    localStorage.setItem('token', token);
  });

  test('debe obtener lista de cursos', async () => {
    const { data } = await api.get('/cursos');
    expect(data.success).toBe(true);
    expect(Array.isArray(data.data)).toBe(true);
  });

  test('debe crear una PQRS', async () => {
    const { data } = await api.post('/pqrs', {
      tipo: 'Pregunta',
      asunto: 'Test',
      descripcion: 'Test PQRS',
      cursoId: 1
    });
    expect(data.success).toBe(true);
    expect(data.data.id).toBeDefined();
  });
});
```

## Troubleshooting

### Error de CORS

Si ves errores de CORS, verifica:
1. El origen está en la lista de `allowed-origins` en `application.yml`
2. El backend está corriendo en el puerto 8080
3. Estás usando el protocolo correcto (http/https)

### Token Expirado

Los tokens JWT expiran después de 1 hora. Implementa refresh automático o redirige al login.

### Conexión Rechazada

Verifica que el backend esté corriendo:
```bash
# En el directorio del backend
mvn spring-boot:run
```

## Recursos Adicionales

- [Documentación completa del API](./BACKEND_README.md)
- [Guía de ejecución](./GUIA_EJECUCION.md)
- [Arquitectura del sistema](./ARCHITECTURE.md)

## Referencia Rápida de Endpoints

### Autenticación
| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/login` | Iniciar sesión | No |
| POST | `/api/auth/register` | Registrar usuario | No |

### Cursos
| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/api/cursos` | Listar todos los cursos | Sí |
| GET | `/api/cursos/{id}` | Obtener curso por ID | Sí |
| GET | `/api/cursos/profesor/{profesorId}` | Cursos de un profesor | Sí |
| POST | `/api/cursos` | Crear curso | Sí (Maestro) |
| PUT | `/api/cursos/{id}` | Actualizar curso | Sí (Maestro) |
| DELETE | `/api/cursos/{id}` | Eliminar curso | Sí (Maestro) |

### Evaluaciones
| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/api/evaluaciones` | Listar con filtros y paginación | Sí |
| GET | `/api/evaluaciones/{id}` | Obtener evaluación por ID | Sí |
| GET | `/api/evaluaciones/curso/{cursoId}` | Evaluaciones de un curso | Sí |
| GET | `/api/evaluaciones/estado/activas` | Evaluaciones activas | Sí |
| POST | `/api/evaluaciones` | Crear evaluación | Sí (Maestro) |
| PUT | `/api/evaluaciones/{id}` | Actualizar evaluación | Sí (Maestro) |
| DELETE | `/api/evaluaciones/{id}` | Eliminar evaluación | Sí (Maestro) |

### Preguntas
| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/api/preguntas` | Listar todas las preguntas | Sí |
| GET | `/api/preguntas/{id}` | Obtener pregunta por ID | Sí |
| GET | `/api/preguntas/evaluacion/{evaluacionId}` | Preguntas de una evaluación | Sí |
| POST | `/api/preguntas` | Crear pregunta | Sí (Maestro) |
| PUT | `/api/preguntas/{id}` | Actualizar pregunta | Sí (Maestro) |
| DELETE | `/api/preguntas/{id}` | Eliminar pregunta | Sí (Maestro) |

### Submissions (Entregas)
| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/api/submissions` | Listar submissions | Sí |
| GET | `/api/submissions/{id}` | Obtener submission por ID | Sí |
| GET | `/api/submissions/evaluacion/{evaluacionId}` | Submissions de una evaluación | Sí |
| GET | `/api/submissions/estudiante/{estudianteId}` | Submissions de un estudiante | Sí |
| POST | `/api/submissions` | Crear submission (entregar) | Sí (Estudiante) |
| PUT | `/api/submissions/{id}` | Actualizar submission | Sí |
| DELETE | `/api/submissions/{id}` | Eliminar submission | Sí |

### Calificaciones
| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/api/calificaciones` | Listar calificaciones | Sí |
| GET | `/api/calificaciones/{id}` | Obtener calificación por ID | Sí |
| GET | `/api/calificaciones/submission/{submissionId}` | Calificaciones de una entrega | Sí |
| POST | `/api/calificaciones` | Crear calificación | Sí (Maestro) |
| PUT | `/api/calificaciones/{id}` | Actualizar calificación | Sí (Maestro) |
| DELETE | `/api/calificaciones/{id}` | Eliminar calificación | Sí (Maestro) |

### PQRS
| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/api/pqrs` | Listar todas las PQRS | Sí |
| GET | `/api/pqrs/{id}` | Obtener PQRS por ID | Sí |
| GET | `/api/pqrs/mis-pqrs` | Mis PQRS | Sí |
| GET | `/api/pqrs/estado/{estado}` | PQRS por estado | Sí |
| POST | `/api/pqrs` | Crear PQRS | Sí |
| PUT | `/api/pqrs/{id}` | Actualizar PQRS | Sí |
| PUT | `/api/pqrs/{id}/responder` | Responder PQRS | Sí (Maestro) |
| DELETE | `/api/pqrs/{id}` | Eliminar PQRS | Sí |

## Modelos de Datos

### Usuario
```typescript
interface User {
  id: number;
  name: string;
  email: string;
  role: 'estudiante' | 'maestro';
  active: boolean;
  createdAt: string;
  updatedAt: string;
}
```

### Curso
```typescript
interface Curso {
  id: number;
  codigo: string;
  nombre: string;
  descripcion: string;
  profesorId: number;
  createdAt: string;
  updatedAt: string;
}
```

### Evaluación
```typescript
interface Evaluacion {
  id: number;
  nombre: string;
  descripcion: string;
  cursoId: number;
  profesorId: number;
  tipo: 'Examen' | 'Quiz' | 'Taller' | 'Proyecto' | 'Tarea';
  estado: 'Borrador' | 'Programada' | 'Activa' | 'Cerrada';
  deadline: string | null;
  duracionMinutos: number;
  intentosPermitidos: number;
  publicada: boolean;
  createdAt: string;
  updatedAt: string;
}
```

### Pregunta
```typescript
interface Pregunta {
  id: number;
  evaluacionId: number;
  enunciado: string;
  tipo: 'seleccion_multiple' | 'verdadero_falso' | 'respuesta_corta' | 'ensayo';
  puntuacion: number;
  orden: number;
  opcionesJson: string; // JSON string con opciones
  respuestaCorrectaJson: string; // JSON string con respuesta correcta
  createdAt: string;
  updatedAt: string;
}
```

### Submission
```typescript
interface Submission {
  id: number;
  evaluacionId: number;
  estudianteId: number;
  respuestasJson: string; // JSON string con respuestas
  estado: 'Borrador' | 'Enviada' | 'Calificada';
  intentoNumero: number;
  fechaInicio: string;
  fechaEnvio: string | null;
  createdAt: string;
  updatedAt: string;
}
```

### Calificación
```typescript
interface Calificacion {
  id: number;
  submissionId: number;
  puntuacionObtenida: number;
  puntuacionMaxima: number;
  retroalimentacion: string;
  calificadoPorId: number;
  fechaCalificacion: string;
  createdAt: string;
  updatedAt: string;
}
```

### PQRS
```typescript
interface PQRS {
  id: number;
  tipo: 'Petición' | 'Queja' | 'Reclamo' | 'Sugerencia';
  asunto: string;
  descripcion: string;
  estado: 'Pendiente' | 'En Proceso' | 'Resuelta' | 'Cerrada';
  usuarioId: number;
  cursoId: number | null;
  respuesta: string | null;
  respondidoPorId: number | null;
  fechaCreacion: string;
  fechaRespuesta: string | null;
  createdAt: string;
  updatedAt: string;
}
```
