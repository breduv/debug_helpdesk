#!/usr/bin/env sh
set -eu
docker compose up -d db
echo "PostgreSQL запускается. Проверьте состояние: docker compose ps"
