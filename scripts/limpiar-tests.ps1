# Script para Limpiar Tests Antiguos (JDBC)
# QuimbayaEVAL Backend

Write-Host "=== Limpieza de Tests Antiguos ===" -ForegroundColor Cyan
Write-Host ""

$testsEliminados = 0
$errores = 0

# Función para eliminar archivo
function Remove-TestFile {
    param($path)
    if (Test-Path $path) {
        try {
            Remove-Item $path -Force
            Write-Host "  ✓ Eliminado: $path" -ForegroundColor Green
            return 1
        } catch {
            Write-Host "  ✗ Error eliminando: $path" -ForegroundColor Red
            return 0
        }
    } else {
        Write-Host "  ⚠ No existe: $path" -ForegroundColor Yellow
        return 0
    }
}

Write-Host "1. Eliminando carpeta dao/ completa..." -ForegroundColor Yellow
$daoPath = "src/test/java/com/quimbayaeval/dao"
if (Test-Path $daoPath) {
    try {
        Remove-Item -Recurse -Force $daoPath
        Write-Host "  ✓ Carpeta dao/ eliminada" -ForegroundColor Green
        $testsEliminados += 2
    } catch {
        Write-Host "  ✗ Error eliminando carpeta dao/" -ForegroundColor Red
        $errores++
    }
} else {
    Write-Host "  ⚠ Carpeta dao/ no existe" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "2. Eliminando tests de servicio antiguos..." -ForegroundColor Yellow
$testsEliminados += Remove-TestFile "src/test/java/com/quimbayaeval/service/AuthServiceTest.java"
$testsEliminados += Remove-TestFile "src/test/java/com/quimbayaeval/service/CursoServiceTest.java"
$testsEliminados += Remove-TestFile "src/test/java/com/quimbayaeval/service/CursoServiceValidationTest.java"
$testsEliminados += Remove-TestFile "src/test/java/com/quimbayaeval/service/EvaluacionServiceTest.java"
$testsEliminados += Remove-TestFile "src/test/java/com/quimbayaeval/service/EvaluacionServiceValidationTest.java"
$testsEliminados += Remove-TestFile "src/test/java/com/quimbayaeval/service/PQRSServiceTest.java"
$testsEliminados += Remove-TestFile "src/test/java/com/quimbayaeval/service/PreguntaServiceTest.java"

Write-Host ""
Write-Host "3. Eliminando test de controlador antiguo..." -ForegroundColor Yellow
$testsEliminados += Remove-TestFile "src/test/java/com/quimbayaeval/controller/AuthControllerIntegrationTest.java"

Write-Host ""
Write-Host "=== Resumen ===" -ForegroundColor Cyan
Write-Host "Tests eliminados: $testsEliminados" -ForegroundColor Green
Write-Host "Errores: $errores" -ForegroundColor $(if ($errores -gt 0) { "Red" } else { "Green" })
Write-Host ""

Write-Host "Tests que permanecen:" -ForegroundColor Yellow
Write-Host "  ✓ QuimbayaEvalApplicationContextTest.java" -ForegroundColor White
Write-Host "  ✓ repository/UserRepositoryTest.java" -ForegroundColor White
Write-Host "  ✓ security/JwtTokenProviderTest.java" -ForegroundColor White
Write-Host "  ✓ service/AuthServiceJpaTest.java" -ForegroundColor White
Write-Host "  ✓ controller/AuthControllerJpaTest.java" -ForegroundColor White
Write-Host ""

Write-Host "Para ejecutar los tests restantes:" -ForegroundColor Cyan
Write-Host "  mvn test" -ForegroundColor White
Write-Host ""
