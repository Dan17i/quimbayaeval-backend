# Guía de Ejecución - QuimbayaEVAL Backend

## Paso 1: Verificar Requisitos Previos

### 1.1 Verificar Java
```bash
java -version
# Debe mostrar Java 17 o superior
```

### 1.2 Verificar Maven
```bash
mvn -version
# Debe mostrar Maven 3.8+
```

### 1.3 Verificar PostgreSQL
```bash
# Windows
pg_isready

# O verificar que el servicio esté corriendo
# Servicios de Windows -> PostgreSQL
```

## Paso 2: Configurar Base de Datos

### 2.1 Crear Base de Datos
```bash
# Abrir psql
psql -U postgres

# Dentro de psql:
CREATE DATABASE quimbayaeval;
\q
```

### 2.2 Cargar Schema
```bash
psql -U postgres -d quimbayaeval -f src/main/resources/db/schema.sql
```

### 2.3 Crear Usuario de Prueba
```bash
psql -U postgres -d quimbayaeval
```

```sql
-- Insertar usuarios de prueba (password: "password")
INSERT INTO users (name, email, password, role, active) VALUES
('Juan Estudiante', 'estudiante@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36P4/tvO', 'estudiante', true),
('María Maestra', 'maestro@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36P4/tvO', 'maestro', true),
('Carlos Coordinador', 'coordinador@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36P4/tvO', 'coordinador', true);

-- Verificar
SELECT id, name, email, role FROM users;
\q
```

## Paso 3: Configurar Variables de Entorno

### 3.1 Generar JWT Secret
```bash
# Windows PowerShell
$bytes = New-Object byte[] 64
(New-Object Security.Cryptography.RNGCryptoServiceProvider).GetBytes($bytes)
[Convert]::ToBase64String($bytes)
```

Copia el resultado (será algo como: `xK7v9w...`)

### 3.2 Crear archivo .env
Crea un archivo `.env` en la raíz del proyecto:

```env
# JWT Configuration
JWT_SECRET=pega-aqui-el-secret-generado-en-paso-anterior
JWT_EXPIRATION=86400000

# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/quimbayaeval
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
```

### 3.3 Configurar Variables en Windows
```powershell
# PowerShell (temporal para esta sesión)
$env:JWT_SECRET="tu-secret-aqui"
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/quimbayaeval"
$env:SPRING_DATASOURCE_USERNAME="quimbayaeval"
$env:SPRING_DATASOURCE_PASSWORD="quimbayaEval123"
$env:CORS_ALLOWED_ORIGINS="http://localhost:5173,http://localhost:3000"

& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U quimbayaeval -d quimbayaeval
```

## Paso 4: Compilar el Proyecto

```bash
# Limpiar y compilar (sin tests por ahora)
mvn clean package -DskipTests
```

**Posibles Errores**:

### Error: "Cannot resolve symbol 'jakarta'"
**Solución**: Actualizar dependencias
```bash
mvn clean install -U
```

### Error: "Lombok not working"
**Solución**: 
1. Instalar Lombok plugin en tu IDE
2. Enable annotation processing en IDE settings

## Paso 5: Ejecutar el Proyecto

### Opción A: Con Maven
```bash
mvn spring-boot:run
```

### Opción B: Con JAR compilado
```bash
java -jar target/quimbayaeval-backend-1.0.0-SNAPSHOT.jar
```

### Opción C: Con Docker Compose
```bash
docker-compose up --build
```

## Paso 6: Verificar que Está Corriendo

### 6.1 Verificar Logs
Deberías ver en la consola:
```
Started QuimbayaEvalBackendApplication in X.XXX seconds
Tomcat started on port(s): 8080 (http)
```

### 6.2 Verificar Health Check
```bash
curl http://localhost:8080/actuator/health
```

Respuesta esperada:
```json
{
  "status": "UP"
}
```

## Paso 7: Probar Endpoints

### 7.1 Test de Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"estudiante@test.com\",\"password\":\"password\",\"role\":\"estudiante\"}"
```

**Respuesta esperada**:
```json
{
  "success": true,
  "message": "Login exitoso",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "type": "Bearer",
    "id": 1,
    "name": "Juan Estudiante",
    "email": "estudiante@test.com",
    "role": "estudiante"
  }
}
```

### 7.2 Guardar el Token
Copia el token de la respuesta anterior y úsalo en los siguientes requests:

```bash
# Guardar en variable (PowerShell)
$TOKEN="pega-aqui-el-token"

# O en bash
export TOKEN="pega-aqui-el-token"
```

### 7.3 Test de Endpoint Protegido
```bash
# PowerShell
curl -X GET http://localhost:8080/api/cursos `
  -H "Authorization: Bearer $TOKEN"

# Bash
curl -X GET http://localhost:8080/api/cursos \
  -H "Authorization: Bearer $TOKEN"
```

### 7.4 Test de Autorización por Rol
```bash
# Intentar crear evaluación como estudiante (debe fallar)
curl -X POST http://localhost:8080/api/evaluaciones `
  -H "Authorization: Bearer $TOKEN" `
  -H "Content-Type: application/json" `
  -d "{\"nombre\":\"Test\",\"cursoId\":1,\"profesorId\":2,\"tipo\":\"Quiz\"}"
```

**Respuesta esperada**: 403 Forbidden

```bash
# Login como maestro
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d "{\"email\":\"maestro@test.com\",\"password\":\"password\",\"role\":\"maestro\"}"

# Guardar nuevo token
$TOKEN_MAESTRO="nuevo-token-aqui"

# Intentar crear evaluación como maestro (debe funcionar)
curl -X POST http://localhost:8080/api/evaluaciones `
  -H "Authorization: Bearer $TOKEN_MAESTRO" `
  -H "Content-Type: application/json" `
  -d "{\"nombre\":\"Parcial 1\",\"cursoId\":1,\"profesorId\":2,\"tipo\":\"Examen\",\"duracionMinutos\":120}"
```

## Paso 8: Verificar Métricas

### 8.1 Actuator Health
```bash
curl http://localhost:8080/actuator/health
```

### 8.2 Métricas
```bash
curl http://localhost:8080/actuator/metrics
```

### 8.3 Prometheus
```bash
curl http://localhost:8080/actuator/prometheus
```

## Paso 9: Verificar Swagger UI

Abre en navegador:
```
http://localhost:8080/swagger-ui.html
```

Deberías ver la documentación interactiva de la API.

## Paso 10: Verificar Logs

### 10.1 Logs de Seguridad
Busca en la consola:
```
Configurando Security Filter Chain
Security Filter Chain configurado exitosamente
Configurando CORS con orígenes permitidos: http://localhost:5173,http://localhost:3000
```

### 10.2 Logs de Autenticación
Después de hacer login, busca:
```
Token generado para usuario: estudiante@test.com con rol: estudiante
Usuario autenticado: estudiante@test.com con rol: estudiante
```

### 10.3 Logs de Errores
Si hay errores, busca líneas con `ERROR` o `WARN`.

## Problemas Comunes y Soluciones

### Problema 1: "JWT_SECRET not found"
**Solución**: Asegúrate de que la variable de entorno esté configurada
```bash
echo $env:JWT_SECRET  # PowerShell
echo $JWT_SECRET      # Bash
```

### Problema 2: "Connection refused to PostgreSQL"
**Solución**: 
1. Verificar que PostgreSQL esté corriendo
2. Verificar puerto (por defecto 5432)
3. Verificar credenciales en application.yml

### Problema 3: "Table 'users' doesn't exist"
**Solución**: Cargar el schema
```bash
psql -U postgres -d quimbayaeval -f src/main/resources/db/schema.sql
```

### Problema 4: "Port 8080 already in use"
**Solución**: Cambiar puerto en application.yml
```yaml
server:
  port: 8081
```

### Problema 5: "Cannot resolve dependencies"
**Solución**: Limpiar caché de Maven
```bash
mvn clean install -U
```

### Problema 6: "Lombok annotations not working"
**Solución**:
1. Instalar Lombok plugin en IDE
2. Enable annotation processing:
   - IntelliJ: Settings → Build → Compiler → Annotation Processors → Enable
   - Eclipse: Project Properties → Java Compiler → Annotation Processing → Enable

## Checklist de Verificación

- [ ] Java 17+ instalado
- [ ] Maven 3.8+ instalado
- [ ] PostgreSQL corriendo
- [ ] Base de datos `quimbayaeval` creada
- [ ] Schema cargado
- [ ] Usuarios de prueba insertados
- [ ] JWT_SECRET configurado
- [ ] Variables de entorno configuradas
- [ ] Proyecto compila sin errores
- [ ] Aplicación inicia correctamente
- [ ] Health check responde OK
- [ ] Login funciona
- [ ] Token se genera correctamente
- [ ] Endpoints protegidos requieren token
- [ ] Autorización por rol funciona
- [ ] Swagger UI accesible
- [ ] Logs se muestran correctamente
- [ ] Métricas disponibles

## Siguiente Paso: Integrar con Frontend

Una vez que el backend esté funcionando, puedes:

1. Iniciar el frontend React
2. Configurar la URL del backend en frontend
3. Probar el flujo completo de login
4. Verificar que los roles funcionen en la UI

Ver `INTEGRATION_GUIDE.md` para más detalles.

---

**Documento generado**: Marzo 6, 2026  
**Última actualización**: Después de migración a JPA
