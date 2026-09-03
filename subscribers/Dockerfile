from gradle:8.14.0-jdk21 as builder

workdir /app

copy . .

run ./gradlew :subscribers:build -x test

from alpine:3.22.2

run apk add --no-cache openjdk21-jre

run addgroup -S appgroup && adduser -S appuser -G appgroup
user appuser

workdir /app

copy --from=builder /app/subscribers/build/libs/*.jar app.jar

expose 8081

entrypoint ["java", "-jar", "app.jar"]