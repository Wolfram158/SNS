#!/bin/sh
set -eu

KAFKA_CONNECT_URL="http://kafka-connect:8083"
CONNECTORS_DIR="/connectors"

log() {
  echo "[$(date '+%H:%M:%S')] $1"
}

log "=== Registering Debezium connectors ==="

log "Waiting for Kafka Connect at $KAFKA_CONNECT_URL..."
retries=0
max_retries=30

until curl -sf "$KAFKA_CONNECT_URL/connectors" > /dev/null 2>&1; do
  retries=$((retries + 1))
  if [ $retries -ge $max_retries ]; then
    log "✗ Kafka Connect not available after $max_retries attempts"
    exit 1
  fi
  log "  Attempt $retries/$max_retries..."
  sleep 3
done

log "✓ Kafka Connect is ready"

connector_count=0
for config in "$CONNECTORS_DIR"/*.json; do
  [ -e "$config" ] || continue

  name=$(jq -r '.name // empty' "$config")
  if [ -z "$name" ]; then
    log "⚠ Skipping $config: no 'name' field"
    continue
  fi

  log "→ Processing: $name"

  if curl -sf "$KAFKA_CONNECT_URL/connectors/$name" > /dev/null 2>&1; then
    log "  Exists — updating config..."
    curl -sf -X PUT \
      -H "Content-Type: application/json" \
      --data @"$config" \
      "$KAFKA_CONNECT_URL/connectors/$name/config"
  else
    log "  New — creating..."
    curl -sf -X POST \
      -H "Content-Type: application/json" \
      --data @"$config" \
      "$KAFKA_CONNECT_URL/connectors"
  fi

  log "  ✓ $name registered"
  connector_count=$((connector_count + 1))
done

if [ $connector_count -eq 0 ]; then
  log "⚠ No connectors found"
  exit 1
fi

log "=== Registered $connector_count connector(s) ==="
exit 0