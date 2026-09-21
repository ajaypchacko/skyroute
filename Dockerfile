# ---------- Stage 1: build ----------
# This stage has Maven and the JDK, and produces one .jar file. It is thrown away
# afterwards, so none of its tools end up in the image that actually runs.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy only the dependency list first. Docker caches each step: if pom.xml hasn't
# changed, this slow download step is skipped on the next build, even if the code has.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Now copy the actual code and build the jar. Tests are skipped here because they
# need Testcontainers, which needs Docker access this build stage doesn't have;
# CI (Phase 9) runs them separately, on a machine that does have Docker.
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---------- Stage 2: run ----------
# A small image with just a JRE (no compiler, no Maven, no source code).
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as a non-root user. If anything inside the container is ever compromised,
# it doesn't get to act as an administrator on the container's file system.
RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build /app/target/skyroute-*.jar app.jar
RUN chown spring:spring app.jar
USER spring

EXPOSE 8080

# Docker checks this URL periodically and marks the container "healthy" or
# "unhealthy" accordingly. docker-compose.yml uses this status to know when the
# app is really ready, not just started.
HEALTHCHECK --interval=10s --timeout=3s --start-period=40s --retries=5 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
