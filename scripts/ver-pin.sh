#!/usr/bin/env bash
# Muestra los PIN de recuperacion de contrasena de la ultima hora.
#   bash scripts/ver-pin.sh
set -euo pipefail
cd "$(dirname "$0")/.."
docker compose -f docker-compose.prod.yml logs --since 1h app | grep -A4 "RECUPERACIÓN" || echo "No hay PIN generados en la última hora."
