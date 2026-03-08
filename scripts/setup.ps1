# Script de Setup para QuimbayaEVAL Backend
# Windows PowerShell

Write-Host "=== QuimbayaEVAL Backend Setup ===" -ForegroundColor Cyan
Write-Host ""

# Paso 1: Verificar Java
Write-Host "1. Verificando Java..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "   ✓ Java encontrado: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Java no encontrado. Por favor instala Java 17+" -ForegroundColor Red
    exit 1
}

# Paso 2: Verificar Maven
Write-Host "2. Verificando Maven..." -ForegroundColor Yellow
try {
    $mavenVersion = mvn -version 2>&1 | Select-String "Apache Maven"
    Write-Host "   ✓ Maven encontrado: $mavenVersion" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Maven no encontrado. Por favor instala Maven 3.8+" -ForegroundColor Red
    exit 1
}

# Paso 3: Verificar PostgreSQL
Write-Host "3. Verificando PostgreSQL..." -ForegroundColor Yellow
try {
    $pgVersion = psql --version 2>&1
    Write-Host "   ✓ PostgreSQL encontrado: $pgVersion" -ForegroundColor Green
} catch {
    Write-Host "   ✗ PostgreSQL no encontrado. Por favor instala PostgreSQL 12+" -ForegroundColor Red
    exit 1
}

# Paso 4: Generar JWT Secret
Write-Host "4. Generando JWT Secret..." -ForegroundColor Yellow
$bytes = New-Object byte[] 64
(New-Object Security.Cryptography.RNGCryptoServiceProvider).GetBytes($bytes)
$jwtSecret = [Convert]::ToBase64String($bytes)
Write-Host "   ✓ JWT Secret generado" -ForegroundColor Green

# Paso 5: Crear archivo .env
Write-Host "5. Creando archivo .env..." -ForegroundColor Yellow
$envContent = @"
# JWT Configuration
JWT_SECRET=$jwtSecret
JWT_EXPIRATION=86400000

# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/quimbayaeval
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
"@

$envContent | Out-File -FilePath ".env" -Encoding UTF8
Write-Host "   ✓ Archivo .env creado" -ForegroundColor Green

# Paso 6: Configurar variables de entorno para esta sesión
Write-Host "6. Configurando variables de entorno..." -ForegroundColor Yellow
$env:JWT_SECRET = $jwtSecret
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/quimbayaeval"
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$env:SPRING_DATASOURCE_PASSWORD = "postgres"
$env:CORS_ALLOWED_ORIGINS = "http://localhost:5173,http://localhost:3000"
Write-Host "   ✓ Variables configuradas" -ForegroundColor Green

# Paso 7: Crear base de datos
Write-Host "7. Configurando base de datos..." -ForegroundColor Yellow
Write-Host "   Ingresa la contraseña de PostgreSQL cuando se solicite" -ForegroundColor Cyan

# Crear base de datos
$createDbSql = "CREATE DATABASE quimbayaeval;"
$checkDb = "SELECT 1 FROM pg_database WHERE datname='quimbayaeval';"

try {
    $dbExists = psql -U postgres -t -c $checkDb 2>&1
    if ($dbExists -match "1") {
        Write-Host "   ✓ Base de datos 'quimbayaeval' ya existe" -ForegroundColor Green
    } else {
        psql -U postgres -c $createDbSql
        Write-Host "   ✓ Base de datos 'quimbayaeval' creada" -ForegroundColor Green
    }
} catch {
    Write-Host "   ⚠ Error al crear base de datos. Puede que ya exista." -ForegroundColor Yellow
}

# Cargar schema
Write-Host "   Cargando schema..." -ForegroundColor Cyan
try {
    psql -U postgres -d quimbayaeval -f "src/main/resources/db/schema.sql" 2>&1 | Out-Null
    Write-Host "   ✓ Schema cargado" -ForegroundColor Green
} catch {
    Write-Host "   ⚠ Error al cargar schema" -ForegroundColor Yellow
}

# Insertar usuarios de prueba
Write-Host "   Insertando usuarios de prueba..." -ForegroundColor Cyan
$insertUsersSql = @"
INSERT INTO users (name, email, password, role, active) VALUES
('Juan Estudiante', 'estudiante@test.com', '\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36P4/tvO', 'estudiante', true),
('María Maestra', 'maestro@test.com', '\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36P4/tvO', 'maestro', true),
('Carlos Coordinador', 'coordinador@test.com', '\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36P4/tvO', 'coordinador', true)
ON CONFLICT (email) DO NOTHING;
"@

try {
    psql -U postgres -d quimbayaeval -c $insertUsersSql 2>&1 | Out-Null
    Write-Host "   ✓ Usuarios de prueba insertados" -ForegroundColor Green
} catch {
    Write-Host "   ⚠ Error al insertar usuarios (pueden ya existir)" -ForegroundColor Yellow
}

# Paso 8: Compilar proyecto
Write-Host "8. Compilando proyecto..." -ForegroundColor Yellow
Write-Host "   Esto puede tomar unos minutos..." -ForegroundColor Cyan
try {
    mvn clean package -DskipTests 2>&1 | Out-Null
    Write-Host "   ✓ Proyecto compilado exitosamente" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Error al compilar proyecto" -ForegroundColor Red
    Write-Host "   Ejecuta manualmente: mvn clean package -DskipTests" -ForegroundColor Yellow
}

# Resumen
Write-Host ""
Write-Host "=== Setup Completado ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "Credenciales de prueba:" -ForegroundColor Yellow
Write-Host "  Estudiante: estudiante@test.com / password" -ForegroundColor White
Write-Host "  Maestro:    maestro@test.com / password" -ForegroundColor White
Write-Host "  Coordinador: coordinador@test.com / password" -ForegroundColor White
Write-Host ""
Write-Host "Para iniciar el servidor:" -ForegroundColor Yellow
Write-Host "  mvn spring-boot:run" -ForegroundColor White
Write-Host ""
Write-Host "Endpoints útiles:" -ForegroundColor Yellow
Write-Host "  API:     http://localhost:8080/api" -ForegroundColor White
Write-Host "  Swagger: http://localhost:8080/swagger-ui.html" -ForegroundColor White
Write-Host "  Health:  http://localhost:8080/actuator/health" -ForegroundColor White
Write-Host ""
Write-Host "Variables de entorno configuradas para esta sesión." -ForegroundColor Green
Write-Host "Para sesiones futuras, ejecuta:" -ForegroundColor Yellow
Write-Host "  . .\.env.ps1" -ForegroundColor White
Write-Host ""

# Crear script para cargar variables en futuras sesiones
$envLoaderContent = @"
# Cargar variables de entorno desde .env
Get-Content .env | ForEach-Object {
    if (`$_ -match '^([^#][^=]+)=(.*)$') {
        [System.Environment]::SetEnvironmentVariable(`$matches[1], `$matches[2])
    }
}
Write-Host "Variables de entorno cargadas desde .env" -ForegroundColor Green
"@

$envLoaderContent | Out-File -FilePath ".env.ps1" -Encoding UTF8

Write-Host "Presiona Enter para continuar..." -ForegroundColor Cyan
Read-Host
