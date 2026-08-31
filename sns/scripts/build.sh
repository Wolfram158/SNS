#!/bin/bash

./gradlew build -x :build -x test
./gradlew :gateway-spring:bootJar