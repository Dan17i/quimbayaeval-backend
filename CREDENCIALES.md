# 🔑 Credenciales de Prueba - QuimbayaEVAL

## Usuarios de Prueba

Todos los usuarios tienen la misma contraseña: **`password`**

| ID | Nombre | Email | Password | Rol | Descripción |
|----|--------|-------|----------|-----|-------------|
| 1 | Admin Sistema | admin@quimbaya.edu.co | password | coordinador | Administrador del sistema |
| 2 | María Profesora | profesor@quimbaya.edu.co | password | maestro | Profesora de Matemáticas y Física |
| 3 | Ana Martínez | ana.martinez@quimbaya.edu.co | password | maestro | Profesora de Programación y BD |
| 4 | Juan Estudiante | estudiante@quimbaya.edu.co | password | estudiante | Estudiante de prueba principal |
| 5 | María García | maria.garcia@quimbaya.edu.co | password | estudiante | Estudiante de prueba |
| 6 | Pedro Pérez | pedro.perez@quimbaya.edu.co | password | estudiante | Estudiante de prueba |
| 7 | Carlos López | carlos.lopez@quimbaya.edu.co | password | estudiante | Estudiante de prueba |

---

## Roles Válidos

Los roles deben escribirse en **minúsculas**:

- ✅ `estudiante`
- ✅ `maestro`
- ✅ `coordinador`

❌ NO usar mayúsculas: `ESTUDIANTE`, `MAESTRO`, `COORDINADOR`

---

## Ejemplos de Login

### PowerShell

```powershell
# Login como Estudiante
$response = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/auth/login" `
  -Headers @{ "Content-Type" = "application/json" } `
  -Body '{"email":"estudiante@quimbaya.edu.co","password":"password","role":"estudiante"}'

$TOKEN = $response.data.token
```

```powershell
# Login como Profesor
$response = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/auth/login" `
  -Headers @{ "Content-Type" = "application/json" } `
  -Body '{"email":"profesor@quimbaya.edu.co","password":"password","role":"maestro"}'

$TOKEN_PROFESOR = $response.data.token
```

```powershell
# Login como Coordinador
$response = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/auth/login" `
  -Headers @{ "Content-Type" = "application/json" } `
  -Body '{"email":"admin@quimbaya.edu.co","password":"password","role":"coordinador"}'

$TOKEN_ADMIN = $response.data.token
```

### cURL (CMD)

```cmd
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"email\":\"estudiante@quimbaya.edu.co\",\"password\":\"password\",\"role\":\"estudiante\"}"
```

### JSON Body

```json
{
  "email": "estudiante@quimbaya.edu.co",
  "password": "password",
  "role": "estudiante"
}
```

---

## Permisos por Rol

### Estudiante
- ✅ Ver cursos
- ✅ Ver evaluaciones
- ✅ Enviar submissions
- ✅ Crear PQRS
- ❌ Crear evaluaciones
- ❌ Calificar
- ❌ Gestionar cursos

### Maestro
- ✅ Todo lo del estudiante
- ✅ Crear evaluaciones
- ✅ Calificar submissions
- ✅ Ver PQRS de sus cursos
- ❌ Eliminar cursos
- ❌ Gestionar usuarios

### Coordinador
- ✅ Acceso completo
- ✅ Gestionar cursos
- ✅ Gestionar evaluaciones
- ✅ Gestionar PQRS
- ✅ Ver reportes

---

## Verificar Login

Después de hacer login, deberías recibir:

```json
{
  "success": true,
  "message": "Login exitoso",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "type": "Bearer",
    "id": 4,
    "name": "Juan Estudiante",
    "email": "estudiante@quimbaya.edu.co",
    "role": "estudiante"
  }
}
```

---

## Usar el Token

```powershell
# Listar cursos con el token
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/cursos" `
  -Headers @{ "Authorization" = "Bearer $TOKEN" }
```

---

## Errores Comunes

### Error: "El rol es obligatorio"
**Causa**: Falta el campo `role` en el body
**Solución**: Incluir `"role":"estudiante"` en el JSON

### Error: "El rol debe ser: estudiante, maestro o coordinador"
**Causa**: El rol está en mayúsculas o es inválido
**Solución**: Usar minúsculas: `estudiante`, `maestro`, o `coordinador`

### Error: "Credenciales inválidas"
**Causa**: Email o password incorrectos
**Solución**: Verificar que el email termine en `@quimbaya.edu.co` y la password sea `password`

### Error: "Usuario no encontrado"
**Causa**: El email no existe en la base de datos
**Solución**: Usar uno de los emails de la tabla de arriba

---

**Última actualización**: Marzo 13, 2026
