# Script de arranque para desarrollo local
# Uso: .\scripts\start-dev.ps1

# Iniciar Docker Desktop si no está corriendo
$dockerRunning = docker info 2>&1 | Select-String "Server Version"
if (-not $dockerRunning) {
    Write-Host "Iniciando Docker Desktop..." -ForegroundColor Yellow
    Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"
    Write-Host "Esperando que Docker Desktop arranque..." -ForegroundColor Yellow
    do {
        Start-Sleep -Seconds 3
        $dockerRunning = docker info 2>&1 | Select-String "Server Version"
    } while (-not $dockerRunning)
    Write-Host "Docker Desktop listo." -ForegroundColor Green
}

Write-Host "Limpiando contenedores huerfanos..." -ForegroundColor Yellow
docker-compose down --remove-orphans

Write-Host "Levantando PostgreSQL en Docker..." -ForegroundColor Cyan
docker-compose up -d

Write-Host "Esperando que PostgreSQL este listo..." -ForegroundColor Cyan
Start-Sleep -Seconds 5

Write-Host "Iniciando Spring Boot..." -ForegroundColor Cyan
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5433/quimbayaeval"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="postgres"
mvn spring-boot:run
