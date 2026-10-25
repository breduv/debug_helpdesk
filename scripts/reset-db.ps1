$ErrorActionPreference = "Stop"
docker compose down -v
docker compose up -d db
Write-Host "База пересоздана из db/init. Дождитесь статуса healthy."
