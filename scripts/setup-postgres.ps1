# Script para configurar PostgreSQL para QuimbayaEVAL
# Ejecutar con: .\scripts\setup-postgres.ps1

Write-Host "=== Configuración de PostgreSQL para QuimbayaEVAL ===" -ForegroundColor Cyan
Write-Host ""

# Verificar si PostgreSQL está instalado
Write-Host "Verificando instalación de PostgreSQL..." -ForegroundColor Yellow
$psqlPath = Get-Command psql -ErrorAction SilentlyContinue

if (-not $psqlPath) {
    Write-Host "ERROR: PostgreSQL no está instalado o no está en el PATH" -ForegroundColor Red
    Write-Host "Instala PostgreSQL desde: https://www.postgresql.org/download/windows/" -ForegroundColor Yellow
    Write-Host "O usa Chocolatey: choco install postgresql" -ForegroundColor Yellow
    exit 1
}

Write-Host "PostgreSQL encontrado: $($psqlPath.Source)" -ForegroundColor Green
Write-Host ""

# Solicitar credenciales de administrador
Write-Host "Ingresa las credenciales del usuario administrador de PostgreSQL:" -ForegroundColor Yellow
$adminUser = Read-Host "Usuario administrador (default: postgres)"
if ([string]::IsNullOrWhiteSpace($adminUser)) {
    $adminUser = "postgres"
}

# Crear base de datos y usuario
Write-Host ""
Write-Host "Creando base de datos y usuario..." -ForegroundColor Yellow
Write-Host "Se te pedirá la contraseña del usuario administrador" -ForegroundColor Cyan
Write-Host ""

$sqlCommands = @"
-- Crear base de datos
CREATE DATABASE quimbayaeval;

-- Crear usuario
CREATE USER quimbaya_user WITH PASSWORD 'quimbayaEval123';

-- Dar permisos
GRANT ALL PRIVILEGES ON DATABASE quimbayaeval TO quimbaya_user;

-- Conectar a la base de datos
\c quimbayaeval

-- Dar permisos en el schema public
GRANT ALL ON SCHEMA public TO quimbaya_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO quimbaya_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO quimbaya_user;

-- Mensaje de éxito
\echo 'Base de datos configurada exitosamente'
"@

# Guardar comandos en archivo temporal
$tempFile = [System.IO.Path]::GetTempFileName()
$sqlCommands | Out-File -FilePath $tempFile -Encoding UTF8

# Ejecutar comandos SQL
try {
    psql -U $adminUser -f $tempFile
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "=== Configuración completada exitosamente ===" -ForegroundColor Green
        Write-Host ""
        Write-Host "Base de datos: quimbayaeval" -ForegroundColor Cyan
        Write-Host "Usuario: quimbaya_user" -ForegroundColor Cyan
        Write-Host "Contraseña: quimbayaEval123" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Actualiza tu archivo .env con estas credenciales" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "Para ejecutar la aplicación con PostgreSQL:" -ForegroundColor Yellow
        Write-Host "  mvn spring-boot:run -Dspring-boot.run.profiles=prod" -ForegroundColor White
        Write-Host ""
        Write-Host "O configura SPRING_PROFILES_ACTIVE=prod en tu .env" -ForegroundColor White
    } else {
        Write-Host ""
        Write-Host "ERROR: Falló la configuración de la base de datos" -ForegroundColor Red
        Write-Host "Verifica que PostgreSQL esté corriendo y las credenciales sean correctas" -ForegroundColor Yellow
    }
} catch {
    Write-Host ""
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
} finally {
    # Limpiar archivo temporal
    Remove-Item -Path $tempFile -ErrorAction SilentlyContinue
}

Write-Host ""
Write-Host "Presiona cualquier tecla para continuar..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
