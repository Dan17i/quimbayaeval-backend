# Script de Diagnóstico Rápido

Write-Host "=== DIAGNÓSTICO QUIMBAYAEVAL ===" -ForegroundColor Cyan
Write-Host ""

# 1. Ver últimos logs con errores
Write-Host "1. Buscando errores en logs..." -ForegroundColor Yellow
docker compose logs backend --tail 100 | Select-String "ERROR|Exception|Failed" | Select-Object -Last 20

Write-Host "`n2. Probando login con curl..." -ForegroundColor Yellow
$body = @{
    email = "estudiante@quimbaya.edu.co"
    password = "password123"
    role = "estudiante"
} | ConvertTo-Json

Write-Host "Body: $body" -ForegroundColor Gray

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
        -Method Post `
        -ContentType "application/json" `
        -Body $body `
        -ErrorAction Stop
    
    Write-Host "✓ Login exitoso!" -ForegroundColor Green
    Write-Host "Token: $($response.data.token.Substring(0, 50))..." -ForegroundColor Gray
} catch {
    Write-Host "✗ Login falló" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Respuesta del servidor: $responseBody" -ForegroundColor Yellow
    }
}

Write-Host "`n3. Verificando base de datos..." -ForegroundColor Yellow
docker exec quimbayaeval-db psql -U quimbayaeval -d quimbayaeval -c "SELECT COUNT(*) as total_users FROM users;"

Write-Host "`n4. Verificando usuarios en BD..." -ForegroundColor Yellow
docker exec quimbayaeval-db psql -U quimbayaeval -d quimbayaeval -c "SELECT id, nombre, email, rol FROM users LIMIT 5;"
