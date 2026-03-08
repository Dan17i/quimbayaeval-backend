# Script de prueba - Windows PowerShell
# Ejecutar en PowerShell: .\test-api.ps1

$API_BASE = "http://localhost:8080/api"
$TOKEN = ""

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "PRUEBAS DE API - QuimbayaEVAL Backend" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

# 1. TEST LOGIN
Write-Host "`n1. Probando LOGIN" -ForegroundColor Yellow
$loginPayload = @{
    email = "estudiante@example.com"
    password = "password123"
    role = "estudiante"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "$API_BASE/auth/login" `
    -Method POST `
    -Headers @{"Content-Type" = "application/json"} `
    -Body $loginPayload

$loginResponse | ConvertTo-Json | Write-Host

$TOKEN = $loginResponse.data.token
if ($TOKEN) {
    Write-Host "✓ Token obtenido: $($TOKEN.Substring(0, [Math]::Min(20, $TOKEN.Length)))..." -ForegroundColor Green
} else {
    Write-Host "ERROR: No se obtuvo token" -ForegroundColor Red
    $TOKEN = "test-token"
}

# 2. GET EVALUACIONES
Write-Host "`n2. Obteniendo Evaluaciones" -ForegroundColor Yellow
try {
    $evalsResponse = Invoke-RestMethod -Uri "$API_BASE/evaluaciones" `
        -Method GET `
        -Headers @{"Authorization" = "Bearer $TOKEN"}
    
    $evalsResponse | ConvertTo-Json | Write-Host
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
}

# 3. GET CURSOS
Write-Host "`n3. Obteniendo Cursos" -ForegroundColor Yellow
try {
    $cursosResponse = Invoke-RestMethod -Uri "$API_BASE/cursos" `
        -Method GET `
        -Headers @{"Authorization" = "Bearer $TOKEN"}
    
    $cursosResponse | ConvertTo-Json | Write-Host
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
}

# 4. GET PQRS
Write-Host "`n4. Obteniendo PQRS" -ForegroundColor Yellow
try {
    $pqrsResponse = Invoke-RestMethod -Uri "$API_BASE/pqrs" `
        -Method GET `
        -Headers @{"Authorization" = "Bearer $TOKEN"}
    
    $pqrsResponse | ConvertTo-Json | Write-Host
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
}

Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "Pruebas completadas" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Cyan

Write-Host "`nNOTAS:" -ForegroundColor Yellow
Write-Host "- El token expira en 24 horas"
Write-Host "- Accede a Swagger en: http://localhost:8080/swagger-ui.html"
