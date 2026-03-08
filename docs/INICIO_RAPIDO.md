# 🚀 Inicio Rápido - QuimbayaEVAL Backend

## Opción 1: Setup Automático (Recomendado)

### Paso 1: Ejecutar Script de Setup
```powershell
# Abrir PowerShell como Administrador en la carpeta del proyecto
.\setup.ps1
```

Este script:
- ✅ Verifica requisitos (Java, Maven, PostgreSQL)
- ✅ Genera JWT secret seguro
- ✅ Crea archivo .env
- ✅ Configura variables de entorno
- ✅ Crea base de datos
- ✅ Carga schema
- ✅ Inserta usuarios de prueba
- ✅ Compila el proyecto

### Paso 2: Iniciar el Servidor
```powershell
mvn spring-boot:run
```

Espera a ver:
```
Started QuimbayaEvalBackendApplication in X.XXX seconds
```

### Paso 3: Probar la API
```powershell
# En otra terminal PowerShell
.\test-api-quick.ps1
```

Este script prueba:
- ✅ Health check
- ✅ Login de usuarios
- ✅ Endpoints protegidos
- ✅ Autorización por rol
- ✅ Métricas
- ✅ Swagger UI

---

## Opción 2: Setup Manual

### 1. Generar JWT Secret
```powershell
$bytes = New-Object byte[] 64
(New-Object Security.Cryptography.RNGCryptoServiceProvider).GetBytes($bytes)
[Convert]::ToBase64String($bytes)
```

### 2. Configurar Variables de Entorno
```powershell
$env:JWT_SECRET="pega-el-secret-generado-aqui"
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/quimbayaeval"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="tu-password-postgres"
```

### 3. Crear Base de Datos
```powershell
psql -U postgres -c "CREATE DATABASE quimbayaeval;"
psql -U postgres -d quimbayaeval -f src/main/resources/db/schema.sql
```

### 4. Insertar Usuarios de Prueba
```powershell
psql -U postgres -d quimbayaeval
```

```sql
INSERT INTO users (name, email, password, role, active) VALUES
('Juan Estudiante', 'estudiante@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36P4/tvO', 'estudiante', true),
('María Maestra', 'maestro@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36P4/tvO', 'maestro', true),
('Carlos Coordinador', 'coordinador@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36P4/tvO', 'coordinador', true);
\q
```

### 5. Compilar y Ejecutar
```powershell
mvn clean package -DskipTests
mvn spring-boot:run
```

---

## Opción 3: Con Docker Compose

```powershell
docker-compose up --build
```

Esto inicia:
- PostgreSQL en puerto 5432
- Backend en puerto 8080

---

## Verificación Rápida

### 1. Health Check
```powershell
curl http://localhost:8080/actuator/health
```

Debe responder:
```json
{"status":"UP"}
```

### 2. Login
```powershell
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{"email":"estudiante@test.com","password":"password","role":"estudiante"}'
```

Debe responder con un token JWT.

### 3. Swagger UI
Abre en navegador:
```
http://localhost:8080/swagger-ui.html
```

---

## Credenciales de Prueba

| Usuario | Email | Password | Rol |
|---------|-------|----------|-----|
| Juan Estudiante | estudiante@test.com | password | estudiante |
| María Maestra | maestro@test.com | password | maestro |
| Carlos Coordinador | coordinador@test.com | password | coordinador |

---

## Endpoints Principales

| Endpoint | Descripción |
|----------|-------------|
| `POST /api/auth/login` | Login de usuario |
| `GET /api/cursos` | Listar cursos (requiere auth) |
| `GET /api/evaluaciones` | Listar evaluaciones (requiere auth) |
| `POST /api/evaluaciones` | Crear evaluación (solo maestro/coordinador) |
| `GET /api/pqrs` | Listar PQRS (requiere auth) |
| `GET /actuator/health` | Estado del servidor |
| `GET /swagger-ui.html` | Documentación interactiva |

---

## Solución de Problemas

### Error: "JWT_SECRET not found"
```powershell
# Verificar variable
echo $env:JWT_SECRET

# Si está vacía, configurar:
$env:JWT_SECRET="tu-secret-aqui"
```

### Error: "Connection refused PostgreSQL"
```powershell
# Verificar que PostgreSQL esté corriendo
Get-Service -Name postgresql*

# Si no está corriendo, iniciar:
Start-Service postgresql-x64-XX
```

### Error: "Port 8080 already in use"
```powershell
# Encontrar proceso usando el puerto
netstat -ano | findstr :8080

# Matar proceso (reemplaza PID)
taskkill /PID XXXX /F
```

### Error: "Table 'users' doesn't exist"
```powershell
# Recargar schema
psql -U postgres -d quimbayaeval -f src/main/resources/db/schema.sql
```

---

## Próximos Pasos

1. ✅ Backend funcionando
2. 📱 Iniciar frontend React (ver FRONTEND.md)
3. 🔗 Integrar frontend con backend (ver INTEGRATION_GUIDE.md)
4. 🧪 Ejecutar tests completos
5. 🚀 Deploy a producción

---

## Recursos Adicionales

- 📖 [Guía de Ejecución Completa](GUIA_EJECUCION.md)
- 🔧 [Mejoras Implementadas](MEJORAS_IMPLEMENTADAS.md)
- 📊 [Análisis de Buenas Prácticas](ANALISIS_BUENAS_PRACTICAS.md)
- 🔗 [Guía de Integración Frontend](INTEGRATION_GUIDE.md)

---

**¿Necesitas ayuda?** Revisa los logs en la consola o ejecuta el script de prueba para diagnóstico.
