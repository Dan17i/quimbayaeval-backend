# Script para reconstruir Docker limpiando la caché
Write-Host "🧹 Limpiando contenedores y caché de Docker..." -ForegroundColor Yellow

# Detener y eliminar contenedores
docker compose down

# Eliminar imágenes del proyecto
docker rmi quimbayaeval-backend-app -f 2>$null

# Limpiar caché de build
docker builder prune -f

Write-Host "🔨 Reconstruyendo con caché limpia..." -ForegroundColor Cyan
docker compose build --no-cache

Write-Host "✅ Reconstrucción completa. Ahora puedes ejecutar: docker compose up -d" -ForegroundColor Green
