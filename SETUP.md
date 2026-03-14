# 🔧 Guía de Instalación y Configuración

Esta guía te ayudará a configurar y ejecutar QuimbayaEVAL Backend en tu entorno local.

## 📋 Requisitos Previos

### Software Requerido

- **Java 17+** - [Descargar](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.8+** - [Descargar](https://maven.apache.org/download.cgi)
- **PostgreSQL 15+** - [Descargar](https://www.postgresql.org/download/)
- **Docker & Docker Compose** (opcional) - [Descargar](https://www.docker.com/)

### Verificar Instalación

```bash
# Verificar Java
java -version
# Debe mostrar: java version "17.x.x"

# Verificar Maven
mvn -version
# Debe mostrar: Apache Maven 3.8.x

# Verificar PostgreSQL
psql --version
# Debe mostrar: psql (PostgreSQL) 15.x
```

## 🚀 Opción 1: Instalación con Docker (Recomendado)

### Paso 1: Clonar el Repositorio

```bash
git clone <repo-url>
cd quimbayaeval-backend
```

### Paso 2: Levantar PostgreSQL en Docker

> El contenedor usa el puerto **5433** (no 5432) para evitar conflicto con un PostgreSQL local.

```powershell
docker-compose up -d

# Verificar que esté corriendo
docker logs quimbayaeval-db
```

### Paso 3: Ejecutar Spring Boot

```powershell
# El application.yml ya tiene puerto 5433 por defecto — no necesitas setear variables
mvn spring-boot:run
```

> Flyway aplica automáticamente el schema y los datos de prueba (`V1__initial_schema.sql`) al arrancar.

### Volver a correr después de Ctrl+C

```powershell
# Solo esto basta
mvn spring-boot:run
```

### Si Docker no estaba corriendo

```powershell
docker-compose up -d
mvn spring-boot:run
```

### Paso 4: Verificar que Funciona

```powershell
curl http://localhost:8080/actuator/health
# Respuesta esperada: {"status":"UP"}
```

### Servicios Disponibles

- Backend: http://localhost:8080
- PostgreSQL (Docker): localhost:**5433**

### Resetear la base de datos

Si necesitas partir desde cero:

```powershell
docker-compose down -v
docker-compose up -d
# Esperar ~10 segundos y luego arrancar Spring Boot
mvn spring-boot:run
```

> Si Flyway da error de checksum al reiniciar, ejecuta:
> ```powershell
> docker exec quimbayaeval-db psql -U postgres -d quimbayaeval -c "UPDATE flyway_schema_history SET checksum = -1725541601 WHERE version = '1';"
> ```

## 🖥️ Opción 2: Instalación Local (Sin Docker)

### Paso 1: Configurar PostgreSQL

```bash
# Crear base de datos
createdb quimbayaeval

# O usando psql
psql -U postgres
CREATE DATABASE quimbayaeval;
\q
```

### Paso 2: Cargar Esquema de Base de Datos

El esquema se carga automáticamente al iniciar la aplicación gracias a JPA/Hibernate. Si prefieres cargarlo manualmente:

```bash
psql -U postgres -d quimbayaeval -f src/main/resources/db/schema.sql
```

### Paso 3: Insertar Usuarios de Prueba

Con Docker + Flyway esto es automático. Si usas PostgreSQL local sin Docker, carga la migración manualmente:

```bash
psql -U postgres -d quimbayaeval -f src/main/resources/db/migration/V1__initial_schema.sql
psql -U postgres -d quimbayaeval -f src/main/resources/db/migration/V2__add_foto_url_to_users.sql
```

Usuarios de prueba (todos con password `password`):

| Email | Rol |
|-------|-----|
| admin@quimbaya.edu.co | coordinador |
| profesor@quimbaya.edu.co | maestro |
| estudiante@quimbaya.edu.co | estudiante |

### Paso 4: Configurar Variables de Entorno

```bash
# Windows PowerShell
$env:JWT_SECRET="tu-secret-generado"
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/quimbayaeval"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="postgres"
$env:CORS_ALLOWED_ORIGINS="http://localhost:5173,http://localhost:3000"

# Linux/Mac
export JWT_SECRET="tu-secret-generado"
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/quimbayaeval"
export SPRING_DATASOURCE_USERNAME="postgres"
export SPRING_DATASOURCE_PASSWORD="postgres"
export CORS_ALLOWED_ORIGINS="http://localhost:5173,http://localhost:3000"
```

O crea un archivo `.env` en la raíz del proyecto (ver Opción 1, Paso 2).

### Paso 5: Compilar el Proyecto

```bash
# Limpiar y compilar
mvn clean package -DskipTests

# Con tests
mvn clean package
```

### Paso 6: Ejecutar la Aplicación

```bash
# Opción A: Con Maven
mvn spring-boot:run

# Opción B: Con JAR compilado
java -jar target/quimbayaeval-backend-1.0.0-SNAPSHOT.jar
```

### Paso 7: Verificar que Funciona

Deberías ver en la consola:

```
Started QuimbayaEvalBackendApplication in X.XXX seconds
Tomcat started on port(s): 8080 (http)
```

Probar:

```powershell
# Health check
curl http://localhost:8080/actuator/health

# Login
$body = '{"email":"estudiante@quimbaya.edu.co","password":"password","role":"estudiante"}'
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/auth/login" -Headers @{"Content-Type"="application/json"} -Body $body
```

## 🧪 Ejecutar Tests

Los tests usan **H2 en memoria** — no necesitan Docker ni PostgreSQL corriendo.

```powershell
# Si en tu sesión NO seteaste SPRING_DATASOURCE_URL (caso normal):
mvn test

# Si la seteaste antes para correr el backend, limpiarla primero:
Remove-Item Env:SPRING_DATASOURCE_URL -ErrorAction SilentlyContinue
mvn test
```

> Las variables de entorno tienen prioridad sobre `application.yml`. Si `SPRING_DATASOURCE_URL` está seteada, Spring Boot intenta conectarse a PostgreSQL en vez de H2 y los tests fallan.

Para verificar si está seteada:
```powershell
echo $env:SPRING_DATASOURCE_URL
# Si no imprime nada, puedes correr mvn test directamente
```

## 🧪 Verificación Completa

### 1. Health Check

```bash
curl http://localhost:8080/actuator/health
```

Respuesta esperada:
```json
{
  "status": "UP"
}
```

### 2. Login de Usuario

```powershell
$body = '{"email":"estudiante@quimbaya.edu.co","password":"password","role":"estudiante"}'
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/auth/login" -Headers @{"Content-Type"="application/json"} -Body $body
```

### 3. Endpoint Protegido

```bash
# Guardar token de la respuesta anterior
TOKEN="pega-aqui-el-token"

# Probar endpoint protegido
curl -X GET http://localhost:8080/api/cursos \
  -H "Authorization: Bearer $TOKEN"
```

### 4. Swagger UI

Abrir en navegador:
```
http://localhost:8080/swagger-ui.html
```

## ⚙️ Configuración Avanzada

### Cambiar Puerto del Servidor

Editar `src/main/resources/application.yml`:

```yaml
server:
  port: 8081  # Cambiar de 8080 a 8081
```

### Configurar Pool de Conexiones

Editar `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
```

### Habilitar Logs de SQL

Editar `src/main/resources/application.yml`:

```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true

logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### Configurar CORS para Múltiples Orígenes

Editar `src/main/java/com/quimbayaeval/config/SecurityConfig.java`:

```java
configuration.setAllowedOrigins(Arrays.asList(
    "http://localhost:5173",
    "http://localhost:3000",
    "https://tu-dominio.com"
));
```

## 🐛 Solución de Problemas

### Error: "JWT_SECRET not found"

**Causa**: Variable de entorno no configurada

**Solución**:
```bash
# Verificar
echo $JWT_SECRET  # Linux/Mac
echo $env:JWT_SECRET  # Windows PowerShell

# Configurar
export JWT_SECRET="tu-secret"  # Linux/Mac
$env:JWT_SECRET="tu-secret"  # Windows PowerShell
```

### Error: "Connection refused to PostgreSQL"

**Causa**: PostgreSQL no está corriendo o puerto incorrecto

**Solución**:
```bash
# Verificar servicio (Windows)
Get-Service postgresql*

# Iniciar servicio
Start-Service postgresql-x64-15

# Verificar puerto
psql -U postgres -c "SHOW port;"
```

### Error: "Port 8080 already in use"

**Causa**: Otro proceso está usando el puerto 8080

**Solución**:
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8080
kill -9 <PID>

# O cambiar puerto en application.yml
```

### Error: "Table 'users' doesn't exist"

**Causa**: Esquema no se cargó correctamente

**Solución**:
```bash
# Verificar configuración JPA
# En application.yml debe estar:
spring:
  jpa:
    hibernate:
      ddl-auto: update  # o create-drop para desarrollo
```

### Error: "Cannot resolve dependencies"

**Causa**: Caché de Maven corrupto

**Solución**:
```bash
# Limpiar caché
mvn clean install -U

# O eliminar carpeta .m2
rm -rf ~/.m2/repository  # Linux/Mac
Remove-Item -Recurse -Force ~/.m2/repository  # Windows PowerShell
```

### Error: "Lombok annotations not working"

**Causa**: Lombok no está configurado en el IDE

**Solución**:
1. Instalar plugin de Lombok en tu IDE
2. Habilitar annotation processing:
   - IntelliJ: Settings → Build → Compiler → Annotation Processors → Enable
   - Eclipse: Project Properties → Java Compiler → Annotation Processing → Enable

## 📝 Scripts de Utilidad

### Windows PowerShell

```powershell
# Script de prueba rápida
.\scripts\test-api-quick.ps1

# Script de diagnóstico
.\scripts\diagnostico.ps1

# Limpiar y reconstruir Docker
.\scripts\rebuild-docker.ps1
```

### Linux/Mac

```bash
# Crear script de prueba
cat > test-api.sh << 'EOF'
#!/bin/bash
echo "Testing health..."
curl http://localhost:8080/actuator/health
echo "\nTesting login..."
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"estudiante@quimbaya.edu.co","password":"password","role":"estudiante"}'
EOF

chmod +x test-api.sh
./test-api.sh
```

## ✅ Checklist de Instalación

- [ ] Java 17+ instalado y verificado
- [ ] Maven 3.8+ instalado y verificado
- [ ] PostgreSQL instalado y corriendo
- [ ] Base de datos `quimbayaeval` creada
- [ ] Usuarios de prueba insertados
- [ ] JWT_SECRET generado y configurado
- [ ] Variables de entorno configuradas
- [ ] Proyecto compila sin errores (`mvn clean package`)
- [ ] Aplicación inicia correctamente
- [ ] Health check responde OK
- [ ] Login funciona y retorna token
- [ ] Endpoints protegidos requieren token
- [ ] Swagger UI accesible

## 🎯 Próximos Pasos

Una vez que el backend esté funcionando:

1. Revisar [API.md](API.md) para conocer todos los endpoints
2. Explorar [ARCHITECTURE.md](ARCHITECTURE.md) para entender la arquitectura
3. Consultar [CREDENCIALES.md](CREDENCIALES.md) para usuarios de prueba
4. Integrar con frontend React (si aplica)

---

**¿Necesitas ayuda?** Revisa la sección de Solución de Problemas o consulta los logs de la aplicación.
