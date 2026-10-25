#!/usr/bin/env sh
set -eu
docker compose down -v
docker compose up -d db
echo "База пересоздана из db/init. Дождитесь статуса healthy."
