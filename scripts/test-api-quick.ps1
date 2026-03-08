# Script de Prueba Rápida - QuimbayaEVAL Backend
# Windows PowerShell

$baseUrl = "http://localhost:8080"

Write-Host "=== Prueba Rápida de API ===" -ForegroundColor Cyan
Write-Host ""

# Test 1: Health Check
Write-Host "1. Health Check..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method Get
    if ($health.status -eq "UP") {
        Write-Host "   ✓ Servidor funcionando correctamente" -ForegroundColor Green
    } else {
        Write-Host "   ✗ Servidor con problemas: $($health.status)" -ForegroundColor Red
    }
} catch {
    Write-Host "   ✗ No se puede conectar al servidor" -ForegroundColor Red
    Write-Host "   Asegúrate de que el servidor esté corriendo: mvn spring-boot:run" -ForegroundColor Yellow
    exit 1
}

# Test 2: Login como Estudiante
Write-Host "2. Login como Estudiante..." -ForegroundColor Yellow
try {
    $loginBody = @{
        email = "estudiante@test.com"
        password = "password"
        role = "estudiante"
    } | ConvertTo-Json

    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" `
        -Method Post `
        -ContentType "application/json" `
        -Body $loginBody

    if ($loginResponse.success) {
        $tokenEstudiante = $loginResponse.data.token
        Write-Host "   ✓ Login exitoso" -ForegroundColor Green
        Write-Host "   Usuario: $($loginResponse.data.name)" -ForegroundColor White
        Write-Host "   Rol: $($loginResponse.data.role)" -ForegroundColor White
    } else {
        Write-Host "   ✗ Login fallido" -ForegroundColor Red
    }
} catch {
    Write-Host "   ✗ Error en login: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 3: Acceder a endpoint protegido
Write-Host "3. Acceder a cursos (endpoint protegido)..." -ForegroundColor Yellow
try {
    $headers = @{
        Authorization = "Bearer $tokenEstudiante"
    }
    
    $cursos = Invoke-RestMethod -Uri "$baseUrl/api/cursos" `
        -Method Get `
        -Headers $headers

    Write-Host "   ✓ Acceso autorizado" -ForegroundColor Green
    Write-Host "   Cursos obtenidos: $($cursos.data.Count)" -ForegroundColor White
} catch {
    Write-Host "   ⚠ Error al obtener cursos (puede ser normal si no hay datos)" -ForegroundColor Yellow
}

# Test 4: Login como Maestro
Write-Host "4. Login como Maestro..." -ForegroundColor Yellow
try {
    $loginBody = @{
        email = "maestro@test.com"
        password = "password"
        role = "maestro"
    } | ConvertTo-Json

    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" `
        -Method Post `
        -ContentType "application/json" `
        -Body $loginBody

    if ($loginResponse.success) {
        $tokenMaestro = $loginResponse.data.token
        Write-Host "   ✓ Login exitoso" -ForegroundColor Green
        Write-Host "   Usuario: $($loginResponse.data.name)" -ForegroundColor White
    }
} catch {
    Write-Host "   ✗ Error en login: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 5: Intentar crear evaluación como estudiante (debe fallar)
Write-Host "5. Test de autorización (estudiante intenta crear evaluación)..." -ForegroundColor Yellow
try {
    $headers = @{
        Authorization = "Bearer $tokenEstudiante"
        "Content-Type" = "application/json"
    }
    
    $evalBody = @{
        nombre = "Test Evaluacion"
        cursoId = 1
        profesorId = 2
        tipo = "Quiz"
        duracionMinutos = 60
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "$baseUrl/api/evaluaciones" `
        -Method Post `
        -Headers $headers `
        -Body $evalBody

    Write-Host "   ✗ Estudiante pudo crear evaluación (ERROR DE SEGURIDAD)" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 403) {
        Write-Host "   ✓ Acceso denegado correctamente (403 Forbidden)" -ForegroundColor Green
    } else {
        Write-Host "   ⚠ Error inesperado: $($_.Exception.Message)" -ForegroundColor Yellow
    }
}

# Test 6: Crear evaluación como maestro (debe funcionar)
Write-Host "6. Test de autorización (maestro crea evaluación)..." -ForegroundColor Yellow
try {
    $headers = @{
        Authorization = "Bearer $tokenMaestro"
        "Content-Type" = "application/json"
    }
    
    $evalBody = @{
        nombre = "Parcial 1 - Matemáticas"
        descripcion = "Evaluación de prueba"
        cursoId = 1
        profesorId = 2
        tipo = "Examen"
        duracionMinutos = 120
        intentosPermitidos = 1
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "$baseUrl/api/evaluaciones" `
        -Method Post `
        -Headers $headers `
        -Body $evalBody

    if ($response.success) {
        Write-Host "   ✓ Evaluación creada exitosamente" -ForegroundColor Green
        Write-Host "   ID: $($response.data.id)" -ForegroundColor White
    }
} catch {
    if ($_.Exception.Response.StatusCode -eq 400) {
        Write-Host "   ⚠ Error de validación (puede ser normal si faltan datos)" -ForegroundColor Yellow
        Write-Host "   Mensaje: $($_.ErrorDetails.Message)" -ForegroundColor White
    } else {
        Write-Host "   ✗ Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test 7: Verificar métricas
Write-Host "7. Verificar métricas..." -ForegroundColor Yellow
try {
    $metrics = Invoke-RestMethod -Uri "$baseUrl/actuator/metrics" -Method Get
    Write-Host "   ✓ Métricas disponibles: $($metrics.names.Count)" -ForegroundColor Green
} catch {
    Write-Host "   ⚠ No se pudieron obtener métricas" -ForegroundColor Yellow
}

# Test 8: Verificar Swagger
Write-Host "8. Verificar Swagger UI..." -ForegroundColor Yellow
try {
    $swagger = Invoke-WebRequest -Uri "$baseUrl/swagger-ui.html" -Method Get
    if ($swagger.StatusCode -eq 200) {
        Write-Host "   ✓ Swagger UI disponible" -ForegroundColor Green
        Write-Host "   URL: $baseUrl/swagger-ui.html" -ForegroundColor White
    }
} catch {
    Write-Host "   ⚠ Swagger UI no disponible" -ForegroundColor Yellow
}

# Resumen
Write-Host ""
Write-Host "=== Resumen de Pruebas ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "✓ Tests completados" -ForegroundColor Green
Write-Host ""
Write-Host "Tokens generados:" -ForegroundColor Yellow
Write-Host "  Estudiante: $tokenEstudiante" -ForegroundColor White
Write-Host "  Maestro: $tokenMaestro" -ForegroundColor White
Write-Host ""
Write-Host "Puedes usar estos tokens para probar en Postman o curl" -ForegroundColor Cyan
Write-Host ""
Write-Host "Ejemplo de uso:" -ForegroundColor Yellow
Write-Host '  curl -H "Authorization: Bearer TOKEN" http://localhost:8080/api/cursos' -ForegroundColor White
Write-Host ""
