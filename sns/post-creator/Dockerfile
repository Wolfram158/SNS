FROM gradle:9.7.0-jdk21 AS builder

WORKDIR /app

COPY buildSrc/build.gradle.kts ./buildSrc/
COPY buildSrc/src ./buildSrc/src
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle

COPY post-creator/build.gradle.kts ./post-creator/
COPY subscribers/build.gradle.kts ./subscribers/
COPY feed/build.gradle.kts ./feed/

RUN mkdir -p subscribers/src/main/kotlin \
    && mkdir -p feed/src/main/kotlin

RUN --mount=type=cache,target=/root/.gradle/caches \
    --mount=type=cache,target=/root/.gradle/wrapper \
    gradle :post-creator:dependencies --no-daemon || true

COPY post-creator/src ./post-creator/src
COPY buildSrc ./buildSrc

RUN --mount=type=cache,target=/root/.gradle/caches \
    --mount=type=cache,target=/root/.gradle/wrapper \
    gradle :post-creator:assemble -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=builder /app/post-creator/build/libs/*.jar app.jar
USER appuser
EXPOSE 8082 8085
ENTRYPOINT ["java", "-jar", "app.jar"]