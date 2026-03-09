# Script de Pruebas Completo - QuimbayaEVAL Backend
# Este script ejecuta todas las pruebas paso a paso

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  PRUEBAS COMPLETAS - QuimbayaEVAL" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8080"
$testsPassed = 0
$testsFailed = 0

# Función para mostrar resultado
function Show-Result {
    param($testName, $success, $message = "")
    if ($success) {
        Write-Host "[✓] $testName" -ForegroundColor Green
        $script:testsPassed++
    } else {
        Write-Host "[✗] $testName" -ForegroundColor Red
        if ($message) {
            Write-Host "    Error: $message" -ForegroundColor Yellow
        }
        $script:testsFailed++
    }
}

# Test 1: Verificar contenedores
Write-Host "`n1. Verificando contenedores..." -ForegroundColor Yellow
try {
    $containers = docker compose ps --format json | ConvertFrom-Json
    $backendRunning = $containers | Where-Object { $_.Service -eq "backend" -and $_.State -eq "running" }
    $dbRunning = $containers | Where-Object { $_.Service -eq "postgres" -and $_.State -eq "running" }
    
    Show-Result "Backend container running" ($null -ne $backendRunning)
    Show-Result "Database container running" ($null -ne $dbRunning)
} catch {
    Show-Result "Verificar contenedores" $false $_.Exception.Message
}

# Esperar a que el backend esté listo
Write-Host "`n2. Esperando a que el backend esté listo..." -ForegroundColor Yellow
$maxRetries = 30
$retryCount = 0
$backendReady = $false

while ($retryCount -lt $maxRetries -and -not $backendReady) {
    try {
        $response = Invoke-WebRequest -Uri "$baseUrl/actuator/health" -Method Get -TimeoutSec 2 -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            $backendReady = $true
        }
    } catch {
        $retryCount++
        Write-Host "." -NoNewline
        Start-Sleep -Seconds 1
    }
}

Write-Host ""
Show-Result "Backend está listo" $backendReady

if (-not $backendReady) {
    Write-Host "`nEl backend no está respondiendo. Verifica los logs:" -ForegroundColor Red
    Write-Host "docker compose logs backend" -ForegroundColor Yellow
    exit 1
}

# Test 3: Health Check
Write-Host "`n3. Probando Health Check..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method Get
    Show-Result "Health check" ($response.status -eq "UP")
} catch {
    Show-Result "Health check" $false $_.Exception.Message
}

# Test 4: Login Estudiante
Write-Host "`n4. Probando Login de Estudiante..." -ForegroundColor Yellow
try {
    $loginBody = @{
        email = "estudiante@quimbaya.edu.co"
        password = "password123"
        role = "estudiante"
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post `
        -ContentType "application/json" -Body $loginBody
    
    $tokenEstudiante = $response.data.token
    Show-Result "Login estudiante" ($response.success -eq $true)
    
    if ($response.success) {
        Write-Host "    Usuario: $($response.data.name)" -ForegroundColor Gray
        Write-Host "    Rol: $($response.data.role)" -ForegroundColor Gray
    }
} catch {
    Show-Result "Login estudiante" $false $_.Exception.Message
    $tokenEstudiante = $null
}

# Test 5: Login Profesor
Write-Host "`n5. Probando Login de Profesor..." -ForegroundColor Yellow
try {
    $loginBody = @{
        email = "profesor@quimbaya.edu.co"
        password = "password123"
        role = "maestro"
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post `
        -ContentType "application/json" -Body $loginBody
    
    $tokenProfesor = $response.data.token
    Show-Result "Login profesor" ($response.success -eq $true)
    
    if ($response.success) {
        Write-Host "    Usuario: $($response.data.name)" -ForegroundColor Gray
        Write-Host "    Rol: $($response.data.role)" -ForegroundColor Gray
    }
} catch {
    Show-Result "Login profesor" $false $_.Exception.Message
    $tokenProfesor = $null
}

# Test 6: Login Coordinador
Write-Host "`n6. Probando Login de Coordinador..." -ForegroundColor Yellow
try {
    $loginBody = @{
        email = "admin@quimbaya.edu.co"
        password = "password123"
        role = "coordinador"
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post `
        -ContentType "application/json" -Body $loginBody
    
    $tokenCoordinador = $response.data.token
    Show-Result "Login coordinador" ($response.success -eq $true)
} catch {
    Show-Result "Login coordinador" $false $_.Exception.Message
    $tokenCoordinador = $null
}

# Test 7: Listar Cursos (con token)
Write-Host "`n7. Probando Listar Cursos (autenticado)..." -ForegroundColor Yellow
if ($tokenEstudiante) {
    try {
        $headers = @{
            Authorization = "Bearer $tokenEstudiante"
        }
        $response = Invoke-RestMethod -Uri "$baseUrl/api/cursos" -Method Get -Headers $headers
        Show-Result "Listar cursos" ($response.success -eq $true)
        
        if ($response.success -and $response.data) {
            Write-Host "    Cursos encontrados: $($response.data.Count)" -ForegroundColor Gray
        }
    } catch {
        Show-Result "Listar cursos" $false $_.Exception.Message
    }
} else {
    Show-Result "Listar cursos" $false "No hay token de estudiante"
}

# Test 8: Intentar acceder sin token (debe fallar)
Write-Host "`n8. Probando acceso sin token (debe fallar)..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/cursos" -Method Get -ErrorAction Stop
    Show-Result "Acceso sin token bloqueado" $false "Debería haber fallado"
} catch {
    if ($_.Exception.Response.StatusCode -eq 401 -or $_.Exception.Response.StatusCode -eq 403) {
        Show-Result "Acceso sin token bloqueado" $true
    } else {
        Show-Result "Acceso sin token bloqueado" $false $_.Exception.Message
    }
}

# Test 9: Crear Evaluación como Profesor
Write-Host "`n9. Probando Crear Evaluación (como profesor)..." -ForegroundColor Yellow
if ($tokenProfesor) {
    try {
        $headers = @{
            Authorization = "Bearer $tokenProfesor"
        }
        $evaluacionBody = @{
            nombre = "Parcial de Prueba $(Get-Date -Format 'HHmmss')"
            descripcion = "Evaluación creada por script de prueba"
            cursoId = 1
            tipo = "EXAMEN"
            duracionMinutos = 120
            intentosPermitidos = 1
        } | ConvertTo-Json

        $response = Invoke-RestMethod -Uri "$baseUrl/api/evaluaciones" -Method Post `
            -Headers $headers -ContentType "application/json" -Body $evaluacionBody
        
        Show-Result "Crear evaluación (profesor)" ($response.success -eq $true)
        
        if ($response.success) {
            $evaluacionId = $response.data.id
            Write-Host "    Evaluación ID: $evaluacionId" -ForegroundColor Gray
        }
    } catch {
        Show-Result "Crear evaluación (profesor)" $false $_.Exception.Message
    }
} else {
    Show-Result "Crear evaluación (profesor)" $false "No hay token de profesor"
}

# Test 10: Intentar crear evaluación como estudiante (debe fallar)
Write-Host "`n10. Probando Crear Evaluación como Estudiante (debe fallar)..." -ForegroundColor Yellow
if ($tokenEstudiante) {
    try {
        $headers = @{
            Authorization = "Bearer $tokenEstudiante"
        }
        $evaluacionBody = @{
            nombre = "Test"
            cursoId = 1
            tipo = "QUIZ"
        } | ConvertTo-Json

        $response = Invoke-RestMethod -Uri "$baseUrl/api/evaluaciones" -Method Post `
            -Headers $headers -ContentType "application/json" -Body $evaluacionBody -ErrorAction Stop
        
        Show-Result "Autorización por rol" $false "Debería haber fallado"
    } catch {
        if ($_.Exception.Response.StatusCode -eq 403) {
            Show-Result "Autorización por rol" $true
        } else {
            Show-Result "Autorización por rol" $false $_.Exception.Message
        }
    }
} else {
    Show-Result "Autorización por rol" $false "No hay token de estudiante"
}

# Test 11: Listar Evaluaciones
Write-Host "`n11. Probando Listar Evaluaciones..." -ForegroundColor Yellow
if ($tokenEstudiante) {
    try {
        $headers = @{
            Authorization = "Bearer $tokenEstudiante"
        }
        $response = Invoke-RestMethod -Uri "$baseUrl/api/evaluaciones" -Method Get -Headers $headers
        Show-Result "Listar evaluaciones" ($response.success -eq $true)
        
        if ($response.success -and $response.data) {
            Write-Host "    Evaluaciones encontradas: $($response.data.Count)" -ForegroundColor Gray
        }
    } catch {
        Show-Result "Listar evaluaciones" $false $_.Exception.Message
    }
} else {
    Show-Result "Listar evaluaciones" $false "No hay token"
}

# Test 12: Crear PQRS
Write-Host "`n12. Probando Crear PQRS..." -ForegroundColor Yellow
if ($tokenEstudiante) {
    try {
        $headers = @{
            Authorization = "Bearer $tokenEstudiante"
        }
        $pqrsBody = @{
            tipo = "PETICION"
            asunto = "Prueba de PQRS $(Get-Date -Format 'HHmmss')"
            descripcion = "PQRS creado por script de prueba"
            cursoId = 1
        } | ConvertTo-Json

        $response = Invoke-RestMethod -Uri "$baseUrl/api/pqrs" -Method Post `
            -Headers $headers -ContentType "application/json" -Body $pqrsBody
        
        Show-Result "Crear PQRS" ($response.success -eq $true)
    } catch {
        Show-Result "Crear PQRS" $false $_.Exception.Message
    }
} else {
    Show-Result "Crear PQRS" $false "No hay token"
}

# Test 13: Verificar Métricas
Write-Host "`n13. Probando Métricas..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/actuator/metrics" -Method Get
    Show-Result "Endpoint de métricas" ($response.names.Count -gt 0)
    
    # Verificar métricas personalizadas
    $customMetrics = @("auth.login.success", "auth.login.failure", "evaluacion.created", "pqrs.created")
    foreach ($metric in $customMetrics) {
        if ($response.names -contains $metric) {
            Show-Result "Métrica '$metric'" $true
        } else {
            Show-Result "Métrica '$metric'" $false "No encontrada"
        }
    }
} catch {
    Show-Result "Métricas" $false $_.Exception.Message
}

# Test 14: Verificar Prometheus
Write-Host "`n14. Probando Endpoint Prometheus..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/actuator/prometheus" -Method Get
    Show-Result "Endpoint Prometheus" ($response.StatusCode -eq 200)
} catch {
    Show-Result "Endpoint Prometheus" $false $_.Exception.Message
}

# Resumen
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  RESUMEN DE PRUEBAS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Tests Exitosos: $testsPassed" -ForegroundColor Green
Write-Host "Tests Fallidos: $testsFailed" -ForegroundColor Red
Write-Host "Total: $($testsPassed + $testsFailed)" -ForegroundColor White

if ($testsFailed -eq 0) {
    Write-Host "`n¡Todas las pruebas pasaron! ✓" -ForegroundColor Green
    Write-Host "`nPróximos pasos:" -ForegroundColor Yellow
    Write-Host "1. Abre Swagger UI: http://localhost:8080/swagger-ui.html" -ForegroundColor White
    Write-Host "2. Revisa las métricas: http://localhost:8080/actuator/metrics" -ForegroundColor White
    Write-Host "3. Integra con el frontend (ver docs/CHECKLIST_INTEGRACION_FRONTEND.md)" -ForegroundColor White
} else {
    Write-Host "`nAlgunas pruebas fallaron. Revisa los logs:" -ForegroundColor Red
    Write-Host "docker compose logs backend" -ForegroundColor Yellow
}

Write-Host ""
