# Script de Prueba Rápida - QuimbayaEVAL Backend
# Prueba los endpoints principales

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  PRUEBA RAPIDA - QuimbayaEVAL" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080"

# Test 1: Health Check
Write-Host "`n1. Health Check..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method Get
    if ($health.status -eq "UP") {
        Write-Host "[OK] Backend esta funcionando" -ForegroundColor Green
    }
} catch {
    Write-Host "[ERROR] Backend no responde" -ForegroundColor Red
    exit 1
}

# Test 2: Login Estudiante
Write-Host "`n2. Login Estudiante..." -ForegroundColor Yellow
try {
    $loginBody = @{
        email = "estudiante@quimbaya.edu.co"
        password = "password123"
        role = "estudiante"
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post `
        -ContentType "application/json" -Body $loginBody
    
    if ($response.success) {
        Write-Host "[OK] Login exitoso" -ForegroundColor Green
        Write-Host "    Usuario: $($response.data.name)" -ForegroundColor Gray
        Write-Host "    Email: $($response.data.email)" -ForegroundColor Gray
        Write-Host "    Rol: $($response.data.role)" -ForegroundColor Gray
        $TOKEN = $response.data.token
        Write-Host "`n    Token guardado en variable `$TOKEN" -ForegroundColor Cyan
    }
} catch {
    Write-Host "[ERROR] Login fallo: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 3: Listar Cursos
Write-Host "`n3. Listar Cursos..." -ForegroundColor Yellow
try {
    $headers = @{
        Authorization = "Bearer $TOKEN"
    }
    $response = Invoke-RestMethod -Uri "$baseUrl/api/cursos" -Method Get -Headers $headers
    
    if ($response.success) {
        Write-Host "[OK] Cursos obtenidos" -ForegroundColor Green
        Write-Host "    Total de cursos: $($response.data.Count)" -ForegroundColor Gray
        foreach ($curso in $response.data) {
            Write-Host "    - $($curso.nombre)" -ForegroundColor Gray
        }
    }
} catch {
    Write-Host "[ERROR] No se pudieron obtener los cursos" -ForegroundColor Red
}

# Test 4: Login Profesor
Write-Host "`n4. Login Profesor..." -ForegroundColor Yellow
try {
    $loginBody = @{
        email = "profesor@quimbaya.edu.co"
        password = "password123"
        role = "maestro"
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post `
        -ContentType "application/json" -Body $loginBody
    
    if ($response.success) {
        Write-Host "[OK] Login profesor exitoso" -ForegroundColor Green
        Write-Host "    Usuario: $($response.data.name)" -ForegroundColor Gray
        $TOKEN_PROFESOR = $response.data.token
    }
} catch {
    Write-Host "[ERROR] Login profesor fallo" -ForegroundColor Red
}

# Test 5: Listar Evaluaciones
Write-Host "`n5. Listar Evaluaciones..." -ForegroundColor Yellow
try {
    $headers = @{
        Authorization = "Bearer $TOKEN"
    }
    $response = Invoke-RestMethod -Uri "$baseUrl/api/evaluaciones" -Method Get -Headers $headers
    
    if ($response.success) {
        Write-Host "[OK] Evaluaciones obtenidas" -ForegroundColor Green
        Write-Host "    Total de evaluaciones: $($response.data.Count)" -ForegroundColor Gray
        foreach ($eval in $response.data) {
            Write-Host "    - $($eval.nombre) [$($eval.tipo)]" -ForegroundColor Gray
        }
    }
} catch {
    Write-Host "[ERROR] No se pudieron obtener las evaluaciones" -ForegroundColor Red
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  PRUEBAS COMPLETADAS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`nPara usar el token en otros comandos:" -ForegroundColor Yellow
Write-Host "`$TOKEN = '$TOKEN'" -ForegroundColor White

Write-Host "`nEjemplo de uso:" -ForegroundColor Yellow
Write-Host "Invoke-RestMethod -Uri 'http://localhost:8080/api/cursos' -Headers @{ Authorization = 'Bearer `$TOKEN' }" -ForegroundColor White

Write-Host ""
