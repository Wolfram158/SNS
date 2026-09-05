#!/bin/bash
# ============================================================
# E2E Test: Delete Post
# Проверяет полный жизненный цикл удаления поста:
#   1. Регистрация двух пользователей
#   2. Подписка пользователя B на пользователя A
#   3. Пользователь A создаёт пост
#   4. Пользователь B видит пост в ленте
#   5. Пользователь A удаляет пост
#   6. Пользователь B больше не видит пост в ленте
#   7. Повторное удаление возвращает 404
# ============================================================

BASE_URL="http://localhost:8080"

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

PASS=0
FAIL=0
STEP=0

next_step() {
    ((STEP++))
    echo ""
    echo -e "${CYAN}── Step $STEP: $1 ──${NC}"
}

assert_status() {
    local desc="$1"
    local expected="$2"
    local actual="$3"

    if [ "$actual" -eq "$expected" ]; then
        echo -e "  ${GREEN}✓ PASS${NC}: $desc (status=$actual)"
        ((PASS++))
    else
        echo -e "  ${RED}✗ FAIL${NC}: $desc (expected=$expected, got=$actual)"
        ((FAIL++))
    fi
}

assert_contains() {
    local desc="$1"
    local needle="$2"
    local haystack="$3"

    if echo "$haystack" | grep -q "$needle"; then
        echo -e "  ${GREEN}✓ PASS${NC}: $desc"
        ((PASS++))
    else
        echo -e "  ${RED}✗ FAIL${NC}: $desc (expected to contain '$needle')"
        echo "  Response: ${haystack:0:200}"
        ((FAIL++))
    fi
}

assert_not_contains() {
    local desc="$1"
    local needle="$2"
    local haystack="$3"

    if echo "$haystack" | grep -q "$needle"; then
        echo -e "  ${RED}✗ FAIL${NC}: $desc (should NOT contain '$needle')"
        echo "  Response: ${haystack:0:200}"
        ((FAIL++))
    else
        echo -e "  ${GREEN}✓ PASS${NC}: $desc"
        ((PASS++))
    fi
}

extract_json_field() {
    local json="$1"
    local field="$2"
    echo "$json" | grep -o "\"$field\":\"[^\"]*\"" | head -1 | sed "s/\"$field\":\"//;s/\"//"
}

echo "=========================================="
echo " E2E Test: Delete Post"
echo "=========================================="

TIMESTAMP=$(date +%s)
NICKNAME_A="author_$TIMESTAMP"
NICKNAME_B="reader_$TIMESTAMP"
PASSWORD="testPassword123"

# ──────────────────────────────────────────────────────────────
next_step "Register User A (author)"
# ──────────────────────────────────────────────────────────────

RESP_A=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/v1/auth/register" \
    -H "Content-Type: application/json" \
    -d "{\"nickname\": \"$NICKNAME_A\", \"password\": \"$PASSWORD\"}")

BODY_A=$(echo "$RESP_A" | head -n -1)
STATUS_A=$(echo "$RESP_A" | tail -n 1)
assert_status "Register User A ($NICKNAME_A)" 200 "$STATUS_A"

TOKEN_A=$(extract_json_field "$BODY_A" "accessToken")
USER_A_ID=$(echo "$BODY_A" | grep -o '"id":[0-9]*' | head -1 | sed 's/"id"://')

if [ -z "$TOKEN_A" ]; then
    echo -e "  ${RED}✗ FATAL${NC}: Could not extract accessToken for User A"
    exit 1
fi
echo "  User A: id=$USER_A_ID"

# ──────────────────────────────────────────────────────────────
next_step "Register User B (reader)"
# ──────────────────────────────────────────────────────────────

RESP_B=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/v1/auth/register" \
    -H "Content-Type: application/json" \
    -d "{\"nickname\": \"$NICKNAME_B\", \"password\": \"$PASSWORD\"}")

BODY_B=$(echo "$RESP_B" | head -n -1)
STATUS_B=$(echo "$RESP_B" | tail -n 1)
assert_status "Register User B ($NICKNAME_B)" 200 "$STATUS_B"

TOKEN_B=$(extract_json_field "$BODY_B" "accessToken")
USER_B_ID=$(echo "$BODY_B" | grep -o '"id":[0-9]*' | head -1 | sed 's/"id"://')

if [ -z "$TOKEN_B" ]; then
    echo -e "  ${RED}✗ FATAL${NC}: Could not extract accessToken for User B"
    exit 1
fi
echo "  User B: id=$USER_B_ID"

# ──────────────────────────────────────────────────────────────
next_step "User B subscribes to User A"
# ──────────────────────────────────────────────────────────────

SUB_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/v1/subscribe" \
    -H "Authorization: Bearer $TOKEN_B" \
    -H "Content-Type: application/json" \
    -d "{\"follower_id\": $USER_B_ID, \"following_id\": $USER_A_ID}")

SUB_STATUS=$(echo "$SUB_RESP" | tail -n 1)
assert_status "User B subscribes to User A" 200 "$SUB_STATUS"

# ──────────────────────────────────────────────────────────────
next_step "User A creates a post"
# ──────────────────────────────────────────────────────────────

POST_TEXT="This post will be deleted. Timestamp: $TIMESTAMP"
CREATE_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/v1/posts" \
    -H "Authorization: Bearer $TOKEN_A" \
    -F "text=$POST_TEXT")

CREATE_BODY=$(echo "$CREATE_RESP" | head -n -1)
CREATE_STATUS=$(echo "$CREATE_RESP" | tail -n 1)
assert_status "Create post" 200 "$CREATE_STATUS"

POST_ID=$(extract_json_field "$CREATE_BODY" "id")
if [ -z "$POST_ID" ]; then
    POST_ID=$(extract_json_field "$CREATE_BODY" "postId")
fi

if [ -z "$POST_ID" ]; then
    echo -e "  ${RED}✗ FATAL${NC}: Could not extract postId"
    echo "  Response: $CREATE_BODY"
    exit 1
fi
echo "  PostId: $POST_ID"

# ──────────────────────────────────────────────────────────────
next_step "Wait for async event processing (PostCreated)"
# ──────────────────────────────────────────────────────────────

echo "  Waiting 5 seconds for Kafka event propagation..."
sleep 5

# ──────────────────────────────────────────────────────────────
next_step "User B sees the post in feed"
# ──────────────────────────────────────────────────────────────

FEED_RESP1=$(curl -s -w "\n%{http_code}" -X GET \
    "$BASE_URL/v1/feed?page=0&size=10&user_id=$USER_B_ID" \
    -H "Authorization: Bearer $TOKEN_B")

FEED_BODY1=$(echo "$FEED_RESP1" | head -n -1)
FEED_STATUS1=$(echo "$FEED_RESP1" | tail -n 1)
assert_status "Get feed (before delete)" 200 "$FEED_STATUS1"
assert_contains "Post exists in User B's feed" "$POST_ID" "$FEED_BODY1"

# ──────────────────────────────────────────────────────────────
next_step "User A deletes the post"
# ──────────────────────────────────────────────────────────────

DELETE_RESP=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/v1/posts/$POST_ID" \
    -H "Authorization: Bearer $TOKEN_A")

DELETE_STATUS=$(echo "$DELETE_RESP" | tail -n 1)
assert_status "Delete post returns 204" 204 "$DELETE_STATUS"

# ──────────────────────────────────────────────────────────────
next_step "Wait for async event processing (PostDeleted)"
# ──────────────────────────────────────────────────────────────

echo "  Waiting 5 seconds for Kafka event propagation..."
sleep 5

# ──────────────────────────────────────────────────────────────
next_step "User B no longer sees the post in feed"
# ──────────────────────────────────────────────────────────────

FEED_RESP2=$(curl -s -w "\n%{http_code}" -X GET \
    "$BASE_URL/v1/feed?page=0&size=10&user_id=$USER_B_ID" \
    -H "Authorization: Bearer $TOKEN_B")

FEED_BODY2=$(echo "$FEED_RESP2" | head -n -1)
FEED_STATUS2=$(echo "$FEED_RESP2" | tail -n 1)
assert_status "Get feed (after delete)" 200 "$FEED_STATUS2"
assert_not_contains "Post removed from User B's feed" "$POST_ID" "$FEED_BODY2"

# ──────────────────────────────────────────────────────────────
next_step "Deleting the same post again returns 404"
# ──────────────────────────────────────────────────────────────

DELETE_RESP2=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/v1/posts/$POST_ID" \
    -H "Authorization: Bearer $TOKEN_A")

DELETE_STATUS2=$(echo "$DELETE_RESP2" | tail -n 1)
assert_status "Delete non-existent post returns 404" 404 "$DELETE_STATUS2"

# ──────────────────────────────────────────────────────────────
next_step "User B cannot delete User A's post (403)"
# ──────────────────────────────────────────────────────────────

CREATE_RESP2=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/v1/posts" \
    -H "Authorization: Bearer $TOKEN_A" \
    -F "text=Another post for permission test")

CREATE_BODY2=$(echo "$CREATE_RESP2" | head -n -1)
POST_ID2=$(extract_json_field "$CREATE_BODY2" "id")

if [ -n "$POST_ID2" ]; then
    DELETE_RESP3=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/v1/posts/$POST_ID2" \
        -H "Authorization: Bearer $TOKEN_B")

    DELETE_STATUS3=$(echo "$DELETE_RESP3" | tail -n 1)
    assert_status "User B cannot delete User A's post (403)" 403 "$DELETE_STATUS3"
else
    echo -e "  ${YELLOW}⚠ SKIP${NC}: Could not create second post for permission test"
fi

# ──────────────────────────────────────────────────────────────
# Итоги
# ──────────────────────────────────────────────────────────────

echo ""
echo "=========================================="
echo -e " Results: ${GREEN}$PASS passed${NC}, ${RED}$FAIL failed${NC}"
echo "=========================================="

if [ $FAIL -gt 0 ]; then
    echo -e "${RED}TEST FAILED${NC}"
    exit 1
else
    echo -e "${GREEN}ALL TESTS PASSED${NC}"
    exit 0
fi