$ErrorActionPreference = "Stop"
docker compose up -d db
Write-Host "PostgreSQL запускается. Проверьте состояние: docker compose ps"
