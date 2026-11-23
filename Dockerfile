# syntax=docker/dockerfile:1.7
FROM maven:3.9.11-eclipse-temurin-21-alpine AS build

WORKDIR /workspace
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests dependency:go-offline
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl \
    && addgroup -S -g 10001 app \
    && adduser -S -u 10001 -G app app
WORKDIR /app
COPY --from=build --chown=10001:10001 /workspace/target/*-SNAPSHOT.jar /app/app.jar
USER 10001:10001

EXPOSE 8081
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -fsS http://localhost:8081/actuator/health >/dev/null || exit 1
ENTRYPOINT ["sh", "-c", "if [ -n \"${DB_PASSWORD_FILE:-}\" ]; then export DB_PASSWORD=\"$(cat \"$DB_PASSWORD_FILE\")\"; fi; exec java -XX:MaxRAMPercentage=75.0 -jar /app/app.jar"]
