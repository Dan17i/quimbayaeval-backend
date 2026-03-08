# Arquitectura - QuimbayaEVAL

## 📊 Vista General del Sistema

```
┌─────────────────────────────────────────────────────────────┐
│                    FRONTEND (React + TypeScript)             │
│  http://localhost:5173                                       │
├─────────────────────────────────────────────────────────────┤
│ - Pages: Login, Dashboard, Evaluaciones, PQRS, etc.        │
│ - Componentes: Button, Card, Table, Dialog, etc.           │
│ - Auth: JWT Token almacenado en localStorage               │
│ - Hooks: useCursos, useEvaluaciones, usePQRS              │
│ - Context: AuthContext (usuario + login/logout)            │
└──────────────────────┬──────────────────────────────────────┘
                       │
                    HTTP API
                  (JSON + JWT)
                       │
        ┌──────────────┴──────────────┐
        │      (REST endpoints)        │
        ▼                              ▼
┌─────────────────────────────────┐
│    BACKEND (Spring Boot Java)    │
│ http://localhost:8080/api        │
├─────────────────────────────────┤
│ Controllers:                     │
│ - AuthController    (login)      │
│ - EvaluacionCtrl    (evaluaciones)
│ - CursoController   (cursos)     │
│ - PQRSController    (pqrs)       │
│                                  │
│ Services:                        │
│ - AuthService       (auth lógica)│
│ - EvaluacionService (eval)       │
│ - CursoService      (cursos)     │
│ - PQRSService       (pqrs)       │
│                                  │
│ DAOs (JDBC):                     │
│ - UserDao                        │
│ - EvaluacionDao                  │
│ - CursoDao                       │
│ - PQRSDao                        │
│                                  │
│ Security:                        │
│ - JwtTokenProvider  (generar JWT)│
│ - JwtAuthFilter     (validar JWT)│
│ - SecurityConfig    (CORS, etc)  │
└──────────────────────┬───────────┘
                       │
                  SQL (JDBC)
                       │
        ┌──────────────┴──────────────┐
        │       PostgreSQL            │
        ▼       (puerto 5432)         │
┌─────────────────────────┐
│   BASE DE DATOS         │
├─────────────────────────┤
│ Tables:                 │
│ - users                 │
│ - cursos                │
│ - evaluaciones          │
│ - preguntas             │
│ - submissions           │
│ - respuestas_preguntas  │
│ - calificaciones        │
│ - resultados            │
│ - pqrs                  │
│ - inscripciones         │
└─────────────────────────┘
```

## 🔄 Flujo de Autenticación

```
Frontend                              Backend                     BD
  │                                    │                          │
  ├─1. POST /auth/login────────────────>                          │
  │    {email, password, role}         │                          │
  │                                    ├─2. Buscar usuario────────>
  │                                    │    <─ SELECT * FROM users
  │                                    ├─3. Validar password       │
  │                                    │    (BCrypt)               │
  │                                    ├─4. Generar JWT            │
  │                                    │    (JwtTokenProvider)     │
  │  <──────────────────── respuesta────┤                          │
  │  {token, user}                     │                          │
  │                                    │                          │
  ├─5. Guardar token en localStorage   │                          │
  │    localStorage.token              │                          │
  │                                    │                          │
  ├─6. GET /api/evaluaciones───────────>                          │
  │    Header: Authorization: Bearer X │                          │
  │                                    ├─7. Validar JWT────────────>
  │                                    ├─8. Obtener evaluaciones───>
  │                                    │    <─ SELECT * FROM...
  │  <────────────────────────respuesta ┤                          │
  │  {success, data: [...]}            │                          │
  │                                    │                          │
```

## 📡 Flujo de Creación de Evaluación

```
Frontend (Maestro)                   Backend                     BD
    │                                 │                           │
    ├─1. Llenar formulario            │                           │
    │   (nombre, tipo, deadline...)   │                           │
    │                                 │                           │
    ├─2. POST /api/evaluaciones───────>                           │
    │    {nombre, tipo, ...}          │                           │
    │    Authorization: Bearer Token  │                           │
    │                                 ├─3. EvaluacionService     │
    │                                 │    .crear(eval)           │
    │                                 │                           │
    │                                 ├─4. EvaluacionDao.save────>
    │                                 │    INSERT INTO evaluaciones
    │                                 │    <─ id generado
    │                                 │                           │
    │  <─────────────────respuesta─────┤                           │
    │  {success, data: {id, ...}}     │                           │
    │                                 │                           │
    ├─5. Redirigir a editor de preguntas
    │                                 │                           │
```

## 🗄️ Modelo de Datos

```
┌──────────────────────────────────────────────────────────────┐
│                          users                                │
├──────────────────────────────────────────────────────────────┤
│ id (PK)   │ name    │ email        │ role     │ password     │
│ 1         │ Juan    │ juan@test.com│ estudiante│ $2a$10$...  │
│ 2         │ María   │ maria@test.com│maestro   │ $2a$10$...  │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│                        cursos                                 │
├──────────────────────────────────────────────────────────────┤
│ id (PK)   │ codigo   │ nombre         │ profesor_id (FK)    │
│ 1         │ MAT-301  │ Cálculo Int.  │ 2 (María)           │
│ 2         │ FIS-201  │ Física I      │ 2 (María)           │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│                    evaluaciones                              │
├──────────────────────────────────────────────────────────────┤
│ id (PK)   │ nombre    │ curso_id(FK)  │ tipo  │ estado       │
│ 1         │ Parcial 1 │ 1             │ Exam  │ Activa       │
│ 2         │ Quiz 1    │ 2             │ Quiz  │ Programada   │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│                    submissions                                │
├──────────────────────────────────────────────────────────────┤
│ id (PK)   │ evaluacion_id(FK) │ estudiante_id(FK) │ estado   │
│ 1         │ 1 (Parcial 1)     │ 1 (Juan)          │ completa │
│ 2         │ 1 (Parcial 1)     │ 3 (otro)          │ en_prog  │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│                        pqrs                                   │
├──────────────────────────────────────────────────────────────┤
│ id (PK)   │ tipo        │ asunto    │ usuario_id(FK) │ estado │
│ 1         │ Pregunta    │ Duda eval │ 1 (Juan)       │ Pend   │
│ 2         │ Sugerencia  │ Horarios  │ 3              │ Resol  │
└──────────────────────────────────────────────────────────────┘
```

## 🔒 Seguridad y Autenticación

```
┌─────────────────────────────────────────────────┐
│          Flujo de Validación de JWT             │
├─────────────────────────────────────────────────┤
│                                                 │
│  1. Request con Authorization Header           │
│     Authorization: Bearer eyJhbGc...           │
│                                                 │
│  2. JwtAuthenticationFilter intercepta request │
│     - Extrae token del header                  │
│     - Valida con JwtTokenProvider              │
│     - Si válido → Autentica usuario            │
│     - Si inválido → Rechaza request            │
│                                                 │
│  3. SecurityConfig decide si ruta es pública   │
│     - POST /auth/login → PÚBLICA               │
│     - GET /api/** → PROTEGIDA (requiere JWT)   │
│                                                 │
│  4. Controller recibe request autenticado      │
│     - Accede a usuario en SecurityContext      │
│     - Procesa request normalmente              │
│     - Retorna respuesta JSON                   │
│                                                 │
└─────────────────────────────────────────────────┘
```

## 🔗 Relaciones de Datos

```
users (1) ──────── (N) cursos
│                  (profesor_id)
│
├────── (N) evaluaciones
│          (profesor_id)
│
├────── (N) submissions
│          (estudiante_id)
│
└────── (N) pqrs
         (usuario_id)

cursos (1) ────── (N) evaluaciones
│                 (curso_id)
│
└────── (N) inscripciones
         (curso_id)

evaluaciones (1) ────── (N) preguntas
│                       (evaluacion_id)
│
├────── (N) submissions
│          (evaluacion_id)
│
└────── (N) pqrs
         (curso_id relacionado)

submissions (1) ────── (N) respuestas_preguntas
│                     (submission_id)
│
└────── (N) calificaciones
         (submission_id)

users (1) ────── (N) inscripciones
         (estudiante_id)
```

## 📦 Stack Tecnológico

### Frontend
- **React 18** - UI library
- **TypeScript** - Type safety
- **Vite** - Build tool
- **React Router v6** - Routing
- **Shadcn/ui** - Component library
- **Tailwind CSS** - Styling
- **Context API** - State management

### Backend
- **Java 17** - Language
- **Spring Boot 3.2** - Framework
- **Spring Security** - Auth
- **JDBC Template** - DB access (sin ORM)
- **PostgreSQL 15** - Database
- **JJWT** - JWT handling
- **Maven** - Build tool

### DevOps
- **Docker** - Containerization
- **Docker Compose** - Orchestration
- **PostgreSQL (Alpine)** - Lightweight DB image

## 🚀 Pipeline de Deployment

```
Local Development
│
├─ docker-compose up
├─ Frontend: localhost:5173
├─ Backend: localhost:8080
├─ Database: localhost:5432
│
v
Staging/QA (en servidor)
│
├─ Build Docker images
├─ Push a registry
├─ Deploy con docker-compose
│
v
Production (en nube/servidor)
│
├─ Configurar HTTPS
├─ Actualizar CORS origins
├─ Cambiar JWT_SECRET
├─ Usar pool de conexiones DB
├─ Enable logging centralizado
├─ Setup monitoring
│
v
Live en URL productiva
```

---

**Todas las capas están integradas y listas para usar! 🎉**
