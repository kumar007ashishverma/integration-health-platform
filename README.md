# Integration Health Platform

A Java 21 and Spring Boot modular monolith for evidence-based monitoring of API integrations. The operations UI uses Thymeleaf and Bootstrap 5; PostgreSQL stores integration data and health snapshots, with schema changes managed by Flyway.

## Structure

- `backend/src/main/java` — Spring Boot application and API, dashboard, and health modules
- `backend/src/main/resources/templates` — Thymeleaf views
- `backend/src/main/resources/static` — browser assets
- `backend/src/main/resources/db/migration` — Flyway migrations
- `backend/src/test/java` — JUnit 5, Mockito, and Spring Boot tests
- `spec` — product requirements, contracts, data model, and quality gates
- `docs/technical-design.md` — current technology and architecture direction

## Run locally

Requires JDK 21, Maven 3.9+, and Docker Compose.

```powershell
docker compose up -d postgres
mvn -f backend/pom.xml spring-boot:run
```

Open <http://localhost:8080/> for the dashboard and <http://localhost:8080/actuator/health> for service health. The dashboard, API, ingestion, schema, and evaluation are scaffolding only.

## Run with containers

```powershell
docker compose up --build
```

Health thresholds and the evaluation window must be calibrated against the supplied synthetic request logs before they are treated as policy.
