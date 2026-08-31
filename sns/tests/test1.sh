#!/bin/bash

set -x
GATEWAY="http://localhost:8080"
ALICE="alice_$(date +%s)"
BOB="bob_$(date +%s)"
TIMEOUT=15

echo "=== 1. Регистрация alice ==="
curl -v -m $TIMEOUT -X POST "$GATEWAY/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"nickname\": \"$ALICE\", \"password\": \"password123\"}" | tee /tmp/alice.json
echo ""

echo "=== 2. Регистрация bob ==="
curl -v -m $TIMEOUT -X POST "$GATEWAY/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"nickname\": \"$BOB\", \"password\": \"password123\"}" | tee /tmp/bob.json
echo ""

ALICE_TOKEN=$(grep -o '"accessToken":"[^"]*"' /tmp/alice.json | head -1 | cut -d'"' -f4)
ALICE_ID=$(grep -o '"id":[0-9]*' /tmp/alice.json | head -1 | cut -d: -f2)
BOB_TOKEN=$(grep -o '"accessToken":"[^"]*"' /tmp/bob.json | head -1 | cut -d'"' -f4)
BOB_ID=$(grep -o '"id":[0-9]*' /tmp/bob.json | head -1 | cut -d: -f2)

echo "ALICE: id=$ALICE_ID, token=${ALICE_TOKEN:0:30}..."
echo "BOB:   id=$BOB_ID, token=${BOB_TOKEN:0:30}..."

echo "=== 3. Подписка alice -> bob ==="
curl -v -m $TIMEOUT -X POST "$GATEWAY/v1/subscribe" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -d "{\"follower_id\": $ALICE_ID, \"following_id\": $BOB_ID}"
echo ""

echo "=== 4. Создание поста bob ==="
curl -v -m $TIMEOUT -X POST "$GATEWAY/v1/posts" \
  -H "Authorization: Bearer $BOB_TOKEN" \
  -F "text=Hello, Clojure!" \
  -F "images=@./imgs/black.png"
echo ""

sleep 10

echo "=== 6. Получение ленты alice ==="
curl -v -m $TIMEOUT "$GATEWAY/v1/feed?user_id=$ALICE_ID" \
  -H "Authorization: Bearer $ALICE_TOKEN"
echo ""