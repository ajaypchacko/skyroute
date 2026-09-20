# SkyRoute Interview Notes

## Phase 0: Setup
Spring Initializr generates a Maven project. `pom.xml` lists the dependencies.
Spring Boot auto-configures whatever is on the classpath, which is why the app
tried to connect to a database as soon as it saw the PostgreSQL driver and Flyway.