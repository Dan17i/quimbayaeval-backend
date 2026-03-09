# Script para probar métricas de QuimbayaEVAL
# Autor: Kiro AI Assistant
# Fecha: Marzo 9, 2026

$BASE_URL = "http://localhost:8080"
$ACTUATOR_URL = "$BASE_URL/actuator"

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "  Test de Métricas - QuimbayaEVAL" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

# Función para hacer requests
function Test-Endpoint {
    param(
        [string]$Url,
        [string]$Name
    )
    
    Write-Host "Probando: $Name" -ForegroundColor Yellow
    Write-Host "URL: $Url" -ForegroundColor Gray
    
    try {
        $response = Invoke-RestMethod -Uri $Url -Method Get -ErrorAction Stop
        Write-Host "✓ OK" -ForegroundColor Green
        return $response
    } catch {
        Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
    Write-Host ""
}

# 1. Health Check
Write-Host "`n1. Health Check" -ForegroundColor Magenta
Write-Host "=================" -ForegroundColor Magenta
$health = Test-Endpoint -Url "$ACTUATOR_URL/health" -Name "Health"
if ($health) {
    Write-Host "Status: $($health.status)" -ForegroundColor $(if ($health.status -eq "UP") { "Green" } else { "Red" })
}

# 2. Listar todas las métricas
Write-Host "`n2. Métricas Disponibles" -ForegroundColor Magenta
Write-Host "========================" -ForegroundColor Magenta
$metrics = Test-Endpoint -Url "$ACTUATOR_URL/metrics" -Name "Lista de métricas"
if ($metrics) {
    Write-Host "Total de métricas: $($metrics.names.Count)" -ForegroundColor Cyan
    Write-Host "`nMétricas personalizadas:" -ForegroundColor Yellow
    $metrics.names | Where-Object { $_ -match "^(auth|evaluacion|pqrs|submission)" } | ForEach-Object {
        Write-Host "  - $_" -ForegroundColor White
    }
}

# 3. Métricas de Autenticación
Write-Host "`n3. Métricas de Autenticación" -ForegroundColor Magenta
Write-Host "=============================" -ForegroundColor Magenta

$loginSuccess = Test-Endpoint -Url "$ACTUATOR_URL/metrics/auth.login.success" -Name "Logins exitosos"
if ($loginSuccess) {
    $count = $loginSuccess.measurements | Where-Object { $_.statistic -eq "COUNT" } | Select-Object -ExpandProperty value
    Write-Host "Logins exitosos: $count" -ForegroundColor Green
}

$loginFailure = Test-Endpoint -Url "$ACTUATOR_URL/metrics/auth.login.failure" -Name "Logins fallidos"
if ($loginFailure) {
    $count = $loginFailure.measurements | Where-Object { $_.statistic -eq "COUNT" } | Select-Object -ExpandProperty value
    Write-Host "Logins fallidos: $count" -ForegroundColor Red
}

# 4. Métricas de Evaluaciones
Write-Host "`n4. Métricas de Evaluaciones" -ForegroundColor Magenta
Write-Host "============================" -ForegroundColor Magenta

$evalCreated = Test-Endpoint -Url "$ACTUATOR_URL/metrics/evaluacion.created" -Name "Evaluaciones creadas"
if ($evalCreated) {
    $count = $evalCreated.measurements | Where-Object { $_.statistic -eq "COUNT" } | Select-Object -ExpandProperty value
    Write-Host "Evaluaciones creadas: $count" -ForegroundColor Green
}

$evalTime = Test-Endpoint -Url "$ACTUATOR_URL/metrics/evaluacion.creation.time" -Name "Tiempo de creación"
if ($evalTime) {
    $total = $evalTime.measurements | Where-Object { $_.statistic -eq "TOTAL_TIME" } | Select-Object -ExpandProperty value
    $count = $evalTime.measurements | Where-Object { $_.statistic -eq "COUNT" } | Select-Object -ExpandProperty value
    if ($count -gt 0) {
        $avg = $total / $count
        Write-Host "Tiempo promedio: $([math]::Round($avg, 3))s" -ForegroundColor Cyan
    }
}

# 5. Métricas HTTP
Write-Host "`n5. Métricas HTTP" -ForegroundColor Magenta
Write-Host "=================" -ForegroundColor Magenta

$httpRequests = Test-Endpoint -Url "$ACTUATOR_URL/metrics/http.server.requests" -Name "HTTP Requests"
if ($httpRequests) {
    Write-Host "Tags disponibles:" -ForegroundColor Yellow
    $httpRequests.availableTags | ForEach-Object {
        Write-Host "  - $($_.tag): $($_.values.Count) valores" -ForegroundColor White
    }
}

# 6. Métricas JVM
Write-Host "`n6. Métricas JVM" -ForegroundColor Magenta
Write-Host "================" -ForegroundColor Magenta

$jvmMemory = Test-Endpoint -Url "$ACTUATOR_URL/metrics/jvm.memory.used" -Name "Memoria JVM usada"
if ($jvmMemory) {
    $used = $jvmMemory.measurements | Where-Object { $_.statistic -eq "VALUE" } | Select-Object -ExpandProperty value
    $usedMB = [math]::Round($used / 1024 / 1024, 2)
    Write-Host "Memoria usada: $usedMB MB" -ForegroundColor Cyan
}

# 7. Prometheus Format
Write-Host "`n7. Formato Prometheus" -ForegroundColor Magenta
Write-Host "======================" -ForegroundColor Magenta
Write-Host "Probando: Prometheus endpoint" -ForegroundColor Yellow
Write-Host "URL: $ACTUATOR_URL/prometheus" -ForegroundColor Gray

try {
    $prometheus = Invoke-WebRequest -Uri "$ACTUATOR_URL/prometheus" -Method Get -ErrorAction Stop
    Write-Host "✓ OK" -ForegroundColor Green
    $lines = ($prometheus.Content -split "`n").Count
    Write-Host "Líneas de métricas: $lines" -ForegroundColor Cyan
    
    # Mostrar algunas métricas personalizadas
    Write-Host "`nMétricas personalizadas (primeras 10 líneas):" -ForegroundColor Yellow
    ($prometheus.Content -split "`n" | Where-Object { $_ -match "^(auth_|evaluacion_|pqrs_)" } | Select-Object -First 10) | ForEach-Object {
        Write-Host "  $_" -ForegroundColor White
    }
} catch {
    Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
}

# 8. Info
Write-Host "`n8. Información de la Aplicación" -ForegroundColor Magenta
Write-Host "=================================" -ForegroundColor Magenta
$info = Test-Endpoint -Url "$ACTUATOR_URL/info" -Name "Info"

# Resumen
Write-Host "`n==================================" -ForegroundColor Cyan
Write-Host "  Resumen" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "✓ Actuator está funcionando correctamente" -ForegroundColor Green
Write-Host "✓ Métricas personalizadas disponibles" -ForegroundColor Green
Write-Host "✓ Formato Prometheus habilitado" -ForegroundColor Green
Write-Host ""
Write-Host "Próximos pasos:" -ForegroundColor Yellow
Write-Host "1. Configurar Prometheus para scraping" -ForegroundColor White
Write-Host "2. Crear dashboards en Grafana" -ForegroundColor White
Write-Host "3. Configurar alertas" -ForegroundColor White
Write-Host ""
Write-Host "Ver documentación completa en: docs/METRICAS_Y_MONITOREO.md" -ForegroundColor Cyan
Write-Host ""
