#!/usr/bin/env bash
# Respaldo comprimido de la base de datos de Eden. Conserva los ultimos 14 dias.
#   bash scripts/respaldo.sh
set -euo pipefail
cd "$(dirname "$0")/.."

mkdir -p respaldos
archivo="respaldos/eden-$(date +%F-%H%M).sql.gz"
docker compose -f docker-compose.prod.yml exec -T postgres pg_dump -U eden --no-owner eden | gzip > "$archivo"
find respaldos -name 'eden-*.sql.gz' -mtime +14 -delete
echo "Respaldo creado: $archivo ($(du -h "$archivo" | cut -f1))"
