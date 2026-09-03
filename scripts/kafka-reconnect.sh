#!/bin/bash

docker exec post-creator-container sh -c \
  "curl -s -X POST http://kafka-connect:8083/connectors/posts-outbox-connector/restart"