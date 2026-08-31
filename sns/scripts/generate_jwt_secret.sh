#!/bin/bash

echo "JWT_SECRET=$(openssl rand -base64 32)" > .env