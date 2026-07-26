#!/usr/bin/env bash
# Restore the committed database snapshot into the local PostgreSQL container.
#
# Usage:
#   ./db/restore.sh                 # restores db/backup/interviewkata.sql
#   ./db/restore.sh path/to.sql     # restores a specific dump
#
# Safe to run on an empty OR existing DB: the dump uses --clean --if-exists,
# so it drops and recreates objects before loading data.

set -euo pipefail

DUMP="${1:-db/backup/interviewkata.sql}"
CONTAINER="interviewkata-db"
DB_USER="interviewkata"
DB_NAME="interviewkata"
export DOCKER_HOST="${DOCKER_HOST:-unix://$HOME/.colima/default/docker.sock}"

if [ ! -f "$DUMP" ]; then
  echo "Dump not found: $DUMP" >&2
  exit 1
fi

if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER}$"; then
  echo "Container '${CONTAINER}' is not running. Start it first: make db" >&2
  exit 1
fi

echo "Restoring '$DUMP' into ${DB_NAME}…"
docker exec -i "$CONTAINER" psql -v ON_ERROR_STOP=0 -U "$DB_USER" -d "$DB_NAME" < "$DUMP"
echo "✓ Restore complete."
