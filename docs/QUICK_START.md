# QuickStart - QuimbayaEVAL Backend

**Guía rápida para levantar el backend en 5 minutos**

## 🚀 Opción 1: Docker (Recomendado - Más Fácil)

```bash
# 1. Posicionarse en carpeta del backend
cd quimbayaeval-backend

# 2. Iniciar todo (Base de datos + Backend)
docker-compose up --build

# 3. Esperar a que esté listo (verá logs como "Started QuimbayaEvalBackendApplication")

# 4. Probar en navegador
http://localhost:8080/swagger-ui.html
```

✅ **HECHO!** El backend está corriendo.

---

## 🖥️ Opción 2: Local (Sin Docker)

### Requisitos
- Java 17+ instalado
- PostgreSQL instalado y corriendo
- Maven

### Pasos

```bash
# 1. Crear base de datos
createdb quimbayaeval

# 2. Cargar esquema
psql -U postgres -d quimbayaeval < src/main/resources/db/schema.sql

# 3. Copiar configuración
cp .env.example .env

# 4. Ejecutar backend
mvn spring-boot:run

# Backend estará en: http://localhost:8080
```

---

## 📝 Agregar Usuarios de Prueba

**Opción A: Via Docker**
```bash
# Conectar a base de datos
docker-compose exec postgres psql -U postgres -d quimbayaeval

# Dentro de psql, ejecutar:
INSERT INTO users (name, email, password, role, active) VALUES
('Juan Estudiante', 'juan@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36P4/tvO', 'estudiante', true),
('María Maestra', 'maria@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36P4/tvO', 'maestro', true);

# Salir con: \q
```

**Opción B: Local**
```bash
# Conectar a PostgreSQL
psql -U postgres -d quimbayaeval

# Ejecutar INSERT (mismo que arriba)
```

**Nota**: El hash `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36P4/tvO` es la contraseña `password` hasheada con bcrypt.

---

## 🔑 Probar Login

```bash
# Con curl (cualquier terminal)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "juan@test.com",
    "password": "password",
    "role": "estudiante"
  }'

# Deberías recibir un token JWT
```

### Con Swagger (En navegador)

1. Ir a: http://localhost:8080/swagger-ui.html
2. Click en "Authorize" (arriba a la derecha)
3. Ingresar token del login anterior
4. Probar endpoints

---

## 🔗 Conectar Frontend

En tu proyecto React:

**1. Crear `.env`:**
```env
VITE_API_BASE_URL=http://localhost:8080/api
```

**2. Actualizar `AuthContext.tsx`:**
```typescript
const login = async (email: string, password: string, role: string) => {
  const res = await fetch(`${import.meta.env.VITE_API_BASE_URL}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password, role })
  });
  
  const data = await res.json();
  localStorage.setItem('token', data.data.token);
  setUser(data.data.user);
};
```

**3. Crear interceptor para todos los requests:**
```typescript
// En cada fetch, agregar header:
headers: {
  'Authorization': `Bearer ${localStorage.getItem('token')}`
}
```

Ver [INTEGRATION_GUIDE.md](./INTEGRATION_GUIDE.md) para detalles completos.

---

## 📚 Endpoints Principales

```
POST   /api/auth/login              # Login
GET    /api/evaluaciones            # Ver evaluaciones
POST   /api/evaluaciones            # Crear evaluación
GET    /api/cursos                  # Ver cursos
GET    /api/pqrs                    # Ver PQRS
POST   /api/pqrs                    # Crear PQRS
```

**Documentación completa:**
- Swagger: http://localhost:8080/swagger-ui.html
- Markdown: [BACKEND_README.md](./BACKEND_README.md)
- Integración: [INTEGRATION_GUIDE.md](./INTEGRATION_GUIDE.md)

---

## ✅ Verificación Rápida

- [ ] Backend corriendo: http://localhost:8080/swagger-ui.html
- [ ] Login funciona
- [ ] Frontend conecta a `/api/auth/login`
- [ ] Token se guarda en localStorage
- [ ] Headers `Authorization: Bearer <token>` en otros requests

---

## 🆘 Problemas?

### "Connection refused"
```bash
# Reiniciar Docker
docker-compose down
docker-compose up --build
```

### "Port 8080 already in use"
```bash
# Cambiar puerto en docker-compose.yml:
# ports: ["8081:8080"]  # Usa 8081 en lugar de 8080
```

### "No users in database"
Ver sección "Agregar Usuarios de Prueba" arriba.

---

## 🎯 Próximos Pasos

1. **Agregar usuarios** (ver sección anterior)
2. **Conectar frontend** (ver instrucciones de integración)
3. **Probar en Swagger** antes de implementar en React
4. **Crear datos de prueba** (evaluaciones, cursos, etc)
5. **Extender backend** según necesidades (más DAOs, servicios)

---

**¡Listo para empezar!** 🚀
