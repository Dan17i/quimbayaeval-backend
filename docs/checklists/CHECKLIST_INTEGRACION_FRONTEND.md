# ✅ Checklist de Integración Frontend - QuimbayaEVAL

## Estado Actual del Backend

### ✅ Completado

#### 1. Migración a JPA
- [x] Entidades JPA creadas (UserEntity, EvaluacionEntity, CursoEntity, PQRSEntity)
- [x] Repositorios JPA implementados
- [x] Configuración de Hibernate en application.yml
- [x] CacheManager configurado

#### 2. Seguridad
- [x] JWT secret en variables de entorno
- [x] CORS configurado centralmente (sin @CrossOrigin en controladores)
- [x] Autorización por rol implementada
- [x] Rate limiting activado
- [x] Roles incluidos en JWT y Authentication

#### 3. Validación
- [x] DTOs de request con @Valid
- [x] Bean Validation configurado
- [x] Excepciones personalizadas creadas
- [x] GlobalExceptionHandler mejorado

#### 4. Logging y Monitoreo
- [x] @Slf4j en servicios y controladores
- [x] Spring Boot Actuator configurado
- [x] Prometheus habilitado
- [x] Logs estructurados

#### 5. API de Autenticación
- [x] AuthController actualizado con DTOs
- [x] AuthService migrado a JPA
- [x] Endpoint POST /api/auth/login funcional
- [x] Endpoint POST /api/auth/register funcional
- [x] Endpoint GET /api/auth/validate funcional

### ⚠️ Pendiente (No Bloqueante)

#### 1. Controladores Restantes
- [ ] Actualizar EvaluacionController para usar JPA
- [ ] Actualizar CursoController para usar JPA
- [ ] Actualizar PQRSController para usar JPA
- [ ] Eliminar @CrossOrigin de controladores restantes

#### 2. Servicios
- [ ] Migrar EvaluacionService a JPA
- [ ] Migrar CursoService a JPA
- [ ] Migrar PQRSService a JPA
- [ ] Agregar @Slf4j a servicios restantes

#### 3. Limpieza de Código Legacy
- [ ] Eliminar clases DAO antiguas (UserDao, EvaluacionDao, etc.)
- [ ] Eliminar JdbcQueryBuilder
- [ ] Eliminar modelo User antiguo (usar UserEntity)
- [ ] Eliminar LoginRequest antiguo (usar LoginRequestDTO)

---

## Compatibilidad con Frontend React

### ✅ Endpoints Requeridos por Frontend

#### Autenticación (LISTO ✅)
```typescript
// Frontend espera:
POST /api/auth/login
Body: { email: string, password: string, role: string }
Response: {
  success: boolean,
  message: string,
  data: {
    token: string,
    id: number,
    name: string,
    email: string,
    role: string
  }
}
```

**Estado**: ✅ Implementado y funcional

#### Cursos (PENDIENTE ⚠️)
```typescript
GET /api/cursos
Headers: { Authorization: "Bearer <token>" }
Response: {
  success: boolean,
  data: Curso[]
}
```

**Estado**: ⚠️ Endpoint existe pero usa DAO antiguo

#### Evaluaciones (PENDIENTE ⚠️)
```typescript
GET /api/evaluaciones
GET /api/evaluaciones/{id}
POST /api/evaluaciones (solo maestro/coordinador)
POST /api/evaluaciones/{id}/submit (estudiante)
```

**Estado**: ⚠️ Endpoints existen pero usan DAO antiguo

#### PQRS (PENDIENTE ⚠️)
```typescript
GET /api/pqrs
POST /api/pqrs
POST /api/pqrs/{id}/respond
```

**Estado**: ⚠️ Endpoints existen pero usan DAO antiguo

---

## Pruebas de Integración

### Test 1: Login desde Frontend ✅

**Frontend (React)**:
```typescript
const response = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    email: 'estudiante@test.com',
    password: 'password',
    role: 'estudiante'
  })
});

const data = await response.json();
localStorage.setItem('token', data.data.token);
localStorage.setItem('user', JSON.stringify(data.data));
```

**Estado**: ✅ Funcional

### Test 2: Request Autenticado ⚠️

**Frontend (React)**:
```typescript
const token = localStorage.getItem('token');
const response = await fetch('http://localhost:8080/api/cursos', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
```

**Estado**: ⚠️ Funcional pero puede fallar si no hay datos en BD

### Test 3: Autorización por Rol ✅

**Frontend (React)**:
```typescript
// Estudiante intenta crear evaluación (debe fallar con 403)
const response = await fetch('http://localhost:8080/api/evaluaciones', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${tokenEstudiante}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({ ... })
});
// Esperado: 403 Forbidden
```

**Estado**: ✅ Funcional

---

## Configuración del Frontend

### 1. Variables de Entorno

Crear archivo `.env` en el proyecto React:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

### 2. Actualizar AuthContext

**Archivo**: `src/contexts/AuthContext.tsx`

```typescript
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

const login = async (email: string, password: string, role: UserRole) => {
  try {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password, role })
    });

    if (!response.ok) {
      throw new Error('Login failed');
    }

    const result = await response.json();
    
    if (result.success) {
      const { token, ...userData } = result.data;
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(userData));
      setUser(userData);
      toast.success(result.message);
    } else {
      throw new Error(result.message);
    }
  } catch (error) {
    toast.error('Error al iniciar sesión');
    throw error;
  }
};
```

### 3. Crear HTTP Interceptor

**Archivo**: `src/utils/api.ts`

```typescript
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

export async function apiRequest(endpoint: string, options: RequestInit = {}) {
  const token = localStorage.getItem('token');
  
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  if (response.status === 401) {
    // Token expirado, hacer logout
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/';
    throw new Error('Sesión expirada');
  }

  return response.json();
}
```

### 4. Actualizar Hooks

**Archivo**: `src/hooks/useCursos.ts`

```typescript
import { apiRequest } from '@/utils/api';

export function useCursos() {
  const [cursos, setCursos] = useState<Curso[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchCursos() {
      try {
        const result = await apiRequest('/cursos');
        if (result.success) {
          setCursos(result.data);
        }
      } catch (error) {
        console.error('Error fetching cursos:', error);
      } finally {
        setLoading(false);
      }
    }
    fetchCursos();
  }, []);

  return { cursos, loading };
}
```

---

## Pasos para Conectar Frontend

### Paso 1: Verificar Backend Funcionando

```bash
# Terminal 1: Iniciar backend
cd quimbayaeval-backend
mvn spring-boot:run

# Verificar que esté corriendo
curl http://localhost:8080/actuator/health
```

### Paso 2: Configurar Frontend

```bash
# Terminal 2: Ir al proyecto frontend
cd quimbayaeval-frontend

# Crear .env
echo "VITE_API_BASE_URL=http://localhost:8080/api" > .env

# Instalar dependencias
npm install

# Iniciar frontend
npm run dev
```

### Paso 3: Probar Login

1. Abrir navegador en `http://localhost:5173`
2. Ir a página de login
3. Ingresar credenciales:
   - Email: `estudiante@test.com`
   - Password: `password`
   - Rol: `estudiante`
4. Click "Iniciar Sesión"
5. Verificar que redirige a dashboard

### Paso 4: Verificar Token

Abrir DevTools → Application → Local Storage:
- Debe existir clave `token` con valor JWT
- Debe existir clave `user` con datos del usuario

### Paso 5: Probar Endpoints Protegidos

1. Navegar a "Mis Cursos"
2. Verificar que se muestran cursos (o mensaje de vacío)
3. Verificar en Network tab que el request incluye header `Authorization`

---

## Problemas Comunes y Soluciones

### Problema 1: CORS Error

**Error**: `Access to fetch at 'http://localhost:8080/api/auth/login' from origin 'http://localhost:5173' has been blocked by CORS policy`

**Solución**: Verificar que `CORS_ALLOWED_ORIGINS` incluya el origen del frontend:
```bash
export CORS_ALLOWED_ORIGINS="http://localhost:5173,http://localhost:3000"
```

### Problema 2: 401 Unauthorized

**Error**: Todos los requests retornan 401

**Solución**: 
1. Verificar que el token se esté guardando en localStorage
2. Verificar que el header `Authorization` se esté enviando
3. Verificar que el token no haya expirado (24 horas)

### Problema 3: 403 Forbidden

**Error**: Request retorna 403

**Solución**: Verificar que el usuario tenga el rol correcto para el endpoint:
- Crear evaluación: solo `maestro` o `coordinador`
- Ver usuarios: solo `coordinador`

### Problema 4: Empty Response

**Error**: Backend retorna `data: []`

**Solución**: Normal si no hay datos en la BD. Insertar datos de prueba:
```sql
-- Insertar curso de prueba
INSERT INTO cursos (codigo, nombre, descripcion, profesor_id) VALUES
('MAT-101', 'Matemáticas Básicas', 'Curso introductorio', 2);

-- Insertar evaluación de prueba
INSERT INTO evaluaciones (nombre, curso_id, profesor_id, tipo, estado, publicada) VALUES
('Parcial 1', 1, 2, 'Examen', 'Activa', true);
```

---

## Estado Final: ¿Listo para Integración?

### ✅ SÍ - Puedes Conectar el Frontend

**Razones**:
1. ✅ Endpoint de login funcional y compatible
2. ✅ JWT generándose correctamente
3. ✅ CORS configurado
4. ✅ Autorización por rol funcionando
5. ✅ Estructura de respuesta compatible con frontend

**Limitaciones Actuales**:
- ⚠️ Algunos endpoints usan código legacy (JDBC) pero funcionan
- ⚠️ Puede que no haya datos en BD (insertar datos de prueba)
- ⚠️ Algunos endpoints pueden necesitar ajustes menores

### Recomendación

**PUEDES EMPEZAR LA INTEGRACIÓN AHORA** con estas prioridades:

1. **Fase 1 (Ahora)**: Integrar login y autenticación
2. **Fase 2 (Después)**: Integrar listado de cursos y evaluaciones
3. **Fase 3 (Luego)**: Completar migración de endpoints restantes a JPA

---

## Próximos Pasos

1. ✅ Iniciar backend: `mvn spring-boot:run`
2. ✅ Configurar variables de entorno en frontend
3. ✅ Actualizar AuthContext en frontend
4. ✅ Probar login desde frontend
5. ⏳ Migrar endpoints restantes a JPA (no bloqueante)
6. ⏳ Insertar datos de prueba en BD
7. ⏳ Probar flujos completos

---

**Documento generado**: Marzo 6, 2026  
**Estado**: Backend listo para integración con frontend  
**Próxima revisión**: Después de completar integración de login
