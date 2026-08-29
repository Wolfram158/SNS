#!/bin/bash

curl -X POST http://localhost:8081/v1/subscribe \
  -H "Content-Type: application/json" \
  -d '{
    "follower_id": 1,
    "following_id": 2
  }'

curl -X POST http://localhost:8082/v1/posts \
  -H "X-User-Id: 2" \
  -F "text=Hello, Clojure!" \
  -F "images=@./imgs/black.png"

sleep 10

curl "http://localhost:8086/v1/feed?user_id=1"