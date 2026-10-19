Absolutely. I’ll align the design with the technology decisions we’ve made and remove the Python-based setup/stack entirely.

The updated technology direction will be:

* **Java 21**
* **Spring Boot 3.x**
* **Maven**
* **Thymeleaf**
* **Bootstrap 5**
* **PostgreSQL**
* **Spring Data JPA / Hibernate**
* **Flyway**
* **Jackson**
* **JUnit 5 + Mockito + Spring Boot Test**
* **Docker / Docker Compose**
* No React
* No Python
* No Python/FastAPI/Flask-based services

Here is the updated design document content:

# Integration Health Platform

## 1. Overview

### 1.1 Problem Statement

Brightline HR operates an HR platform that integrates with multiple external systems, including:

* Payroll
* E-Signature
* Applicant Tracking Systems (ATS)
* Directory Synchronization
* Notification Systems

Today, the Operations team typically discovers that an integration is broken only after a customer raises a support ticket. This can result in integration failures remaining undetected for hours or days.

The objective of this project is to provide an operational health view that allows the Ops team to quickly determine:

1. Which integrations are healthy?
2. Which integrations require attention?
3. How severe is the issue?
4. Why has an integration been classified as unhealthy?
5. When was the integration last evaluated?

The solution will consume request-level integration logs, calculate meaningful health metrics over defined evaluation windows, classify each integration, and present the results through an operational dashboard.

---

# 2. Goals

## 2.1 Primary Goals

The system should:

* Evaluate the health of each integration automatically.
* Detect elevated error rates.
* Detect latency degradation.
* Avoid reacting excessively to isolated/transient failures.
* Provide an understandable health classification.
* Explain why an integration is considered degraded or unhealthy.
* Provide an overall health picture across all integrations.
* Refresh health information periodically.
* Give Operations a dashboard that can be understood at a glance.
* Keep the health evaluation logic deterministic and testable.

## 2.2 Non-Goals

The initial version will not attempt to:

* Replace a full observability platform.
* Perform distributed tracing.
* Automatically remediate failed integrations.
* Restart external services.
* Implement real-time streaming infrastructure.
* Build a separate frontend SPA.
* Introduce unnecessary infrastructure such as Kafka or Redis.

The design intentionally favors simplicity and maintainability for the assessment scope.

---

# 3. Technology Stack

## 3.1 Backend

| Component          | Technology              |
| ------------------ | ----------------------- |
| Language           | Java 21                 |
| Framework          | Spring Boot 3.x         |
| Build Tool         | Apache Maven            |
| Web Framework      | Spring MVC              |
| API                | REST                    |
| Persistence        | Spring Data JPA         |
| ORM                | Hibernate               |
| JSON Serialization | Jackson                 |
| Validation         | Jakarta Bean Validation |
| Database Migration | Flyway                  |
| Logging            | SLF4J + Logback         |

## 3.2 Frontend

| Component              | Technology                                            |
| ---------------------- | ----------------------------------------------------- |
| Server-side rendering  | Thymeleaf                                             |
| UI Framework           | Bootstrap 5                                           |
| Client-side JavaScript | Lightweight JavaScript where required                 |
| Charts                 | Lightweight browser-side charting library if required |

A separate React application is deliberately not used.

The dashboard will be rendered by the Spring Boot application using Thymeleaf. This reduces architectural complexity and keeps the focus on the core integration-health problem.

## 3.3 Database

**PostgreSQL**

PostgreSQL will persist:

* Integration definitions
* Request records / normalized request metrics
* Health evaluations
* Configuration/thresholds where appropriate

## 3.4 Testing

| Test Type                | Technology                                          |
| ------------------------ | --------------------------------------------------- |
| Unit Tests               | JUnit 5                                             |
| Mocking                  | Mockito                                             |
| Spring Integration Tests | Spring Boot Test                                    |
| Repository Tests         | Spring Boot Test / Testcontainers where appropriate |
| API Tests                | MockMvc                                             |

The health evaluation engine will be designed so that the majority of business rules can be tested without starting the Spring application.

## 3.5 Infrastructure

* Docker
* Docker Compose
* PostgreSQL container
* Spring Boot application container

No Python runtime or Python-based service is required.

---

# 4. High-Level Architecture

```text
                         ┌──────────────────────────┐
                         │      Operations User     │
                         └────────────┬─────────────┘
                                      │
                                      ▼
                         ┌──────────────────────────┐
                         │     Thymeleaf +          │
                         │       Bootstrap UI       │
                         └────────────┬─────────────┘
                                      │
                                      ▼
┌──────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                   │
│                                                              │
│  ┌────────────────┐      ┌───────────────────────────────┐  │
│  │ Web / REST     │─────▶│ Integration Health Service    │  │
│  │ Controllers    │      └───────────────┬───────────────┘  │
│  └────────────────┘                      │                  │
│                                          ▼                  │
│                         ┌───────────────────────────────┐   │
│                         │       Health Evaluator        │   │
│                         │                               │   │
│                         │ Error Rate                    │   │
│                         │ Success Rate                  │   │
│                         │ Latency                       │   │
│                         │ Evaluation Window             │   │
│                         │ Health Classification         │   │
│                         └───────────────┬───────────────┘   │
│                                         │                   │
│                                         ▼                   │
│                         ┌───────────────────────────────┐   │
│                         │ Spring Data JPA / Hibernate    │   │
│                         └───────────────┬───────────────┘   │
└─────────────────────────────────────────┼────────────────────┘
                                          │
                                          ▼
                              ┌──────────────────────┐
                              │      PostgreSQL      │
                              └──────────────────────┘
```

---

# 5. Application Architecture

The application will follow a layered architecture with clear separation of responsibilities.

```text
com.example.integrationhealth
│
├── IntegrationHealthApplication
│
├── integration
│   ├── Integration
│   ├── IntegrationRepository
│   └── IntegrationService
│
├── request
│   ├── ApiRequest
│   ├── ApiRequestRepository
│   └── RequestMetricsService
│
├── health
│   ├── HealthEvaluator
│   ├── HealthEvaluationService
│   ├── HealthEvaluation
│   ├── HealthMetrics
│   ├── HealthScore
│   └── HealthStatus
│
├── dashboard
│   └── DashboardController
│
├── api
│   └── IntegrationHealthController
│
└── config
    └── ApplicationConfiguration
```

The core business logic will remain independent of the presentation layer.

---

# 6. Core Design Principle

The most important architectural decision is to separate:

```text
Raw Requests
     ↓
Metrics
     ↓
Health Evaluation
     ↓
Health Classification
     ↓
Presentation
```

The UI should not calculate health.

The controller should not calculate health.

The database should not contain business logic.

Instead, the health evaluation should be implemented as a dedicated domain/service component.

For example:

```text
HealthMetrics
      │
      ▼
HealthEvaluator
      │
      ├── Health Status
      ├── Health Score
      └── Reasons
```

This makes the health model easy to test, change, and defend during the assessment.

---

# 7. Health Evaluation

The exact thresholds will be derived from the supplied synthetic request logs rather than arbitrarily selected before examining the data.

The evaluation should consider multiple signals.

## 7.1 Error Rate

Error rate represents the proportion of requests resulting in failures during the evaluation window.

```text
errorRate = failedRequests / totalRequests
```

A small number of isolated failures should not automatically make an integration unhealthy.

The evaluation should therefore consider both:

* Error percentage
* Minimum request volume

This prevents a small sample size from producing misleading classifications.

---

# 8. Latency

Latency should be evaluated using percentile-based metrics rather than average latency alone.

For example:

* P50
* P95
* P99, if the available data supports it

P95 is particularly useful for operational health because an average can hide a degraded tail.

The selected latency threshold will be derived from the observed request data and documented as a design decision.

---

# 9. Evaluation Window

The health evaluator should operate on a rolling time window.

For example:

```text
Current Time
     │
     ├──────────────────────────────┤
     │       Evaluation Window     │
     │                             │
     ▼                             ▼
   T-N                            Now
```

The exact window will be selected after examining the supplied request logs.

The design should balance:

* Fast detection
* Stability
* Resistance to transient failures
* Availability of enough observations

A window that is too short may create noisy alerts.

A window that is too long may delay detection.

---

# 10. Health Classification

The initial model will use three states:

```text
HEALTHY
DEGRADED
UNHEALTHY
```

### HEALTHY

The integration is operating within expected error and latency boundaries.

### DEGRADED

The integration is showing meaningful deterioration but may still be functional.

Examples:

* Elevated error rate
* Elevated P95 latency
* One metric approaching a critical boundary

### UNHEALTHY

The integration has a significant and sustained failure condition.

Examples:

* Error rate above the critical threshold
* Severe latency degradation
* Sustained failure with sufficient request volume

The exact boundaries will be documented based on the provided data.

---

# 11. Explainability

The system should not only return:

```text
UNHEALTHY
```

It should also provide the reasons.

For example:

```text
Status: DEGRADED

Reasons:
- Error rate of 4.2% exceeds the degraded threshold of 2%
- P95 latency of 1.8s exceeds the expected latency threshold
```

This is particularly important for an Operations dashboard because users should not have to inspect raw logs to understand why an integration needs attention.

---

# 12. Health Score

A normalized health score may be used to provide additional signal.

For example:

```text
100 = Excellent
 75 = Healthy
 50 = Degraded
  0 = Critical
```

The score should not replace the categorical status.

The categorical status remains the primary operational signal, while the score provides additional context.

The scoring model should be documented explicitly so that its behavior is understandable and defensible.

---

# 13. Overall Health

The dashboard should provide both:

1. Per-integration health
2. Overall platform health

Example:

```text
Overall Health

5 Integrations
────────────────────────────
Healthy       3
Degraded      1
Unhealthy     1
```

The overall status should not simply be the arithmetic average of individual scores.

For example, one critical integration should not necessarily be hidden by four healthy integrations.

A sensible approach is to use the worst/severity-aware status as a primary signal while providing the detailed breakdown.

---

# 14. Refresh Strategy

The dashboard should periodically refresh health information.

For the initial implementation, a scheduled evaluation can be used rather than introducing streaming infrastructure.

Example:

```text
Scheduled Job
     │
     ▼
Evaluate integrations
     │
     ▼
Persist health snapshot
     │
     ▼
Dashboard displays latest snapshot
```

The refresh interval should be configurable.

This keeps the implementation simple while allowing the system to be adjusted without code changes.

---

# 15. Database Model

A conceptual schema could contain:

## Integration

```text
integration
------------
id
name
type
enabled
created_at
updated_at
```

## API Request

```text
api_request
------------
id
integration_id
timestamp
status_code
latency_ms
success
```

## Health Evaluation

```text
health_evaluation
-----------------
id
integration_id
evaluated_at
window_start
window_end
request_count
success_rate
error_rate
p50_latency_ms
p95_latency_ms
health_score
status
reasons
```

The final schema will be adjusted after inspecting the supplied starter repository and request-log format.

---

# 16. Data Ingestion

The supplied synthetic request logs will be imported into the application/database.

A dedicated ingestion component should transform source records into the application's normalized request model.

Conceptually:

```text
Synthetic Request Logs
        │
        ▼
     Jackson
        │
        ▼
Request DTO
        │
        ▼
Validation / Mapping
        │
        ▼
ApiRequest
        │
        ▼
PostgreSQL
```

Jackson will be used for JSON parsing and serialization.

---

# 17. REST API

Although the primary UI will use Thymeleaf, REST endpoints can expose the health information for future integrations.

Potential endpoints:

```text
GET /api/integrations
GET /api/integrations/{id}
GET /api/integrations/{id}/health
GET /api/health/summary
```

Example:

```text
GET /api/integrations/{id}/health
```

Response:

```json
{
  "integration": "ATS",
  "status": "DEGRADED",
  "score": 72,
  "errorRate": 0.028,
  "p95LatencyMs": 1840,
  "evaluatedAt": "2026-10-07T09:42:18Z",
  "reasons": [
    "Error rate above degraded threshold",
    "P95 latency above expected threshold"
  ]
}
```

---

# 18. User Interface

The primary page will be an Operations Health Dashboard.

## Dashboard

The dashboard should provide:

* Overall health
* Number of healthy integrations
* Number of degraded integrations
* Number of unhealthy integrations
* Per-integration health
* Error rate
* Success rate
* P95 latency
* Last evaluation time
* Reason for degraded/unhealthy status

Bootstrap will provide the responsive UI components.

Thymeleaf will render server-side HTML.

---

# 19. Integration Detail

Selecting an integration should provide more detailed information:

```text
ATS Integration
──────────────────────────────

Status: DEGRADED
Score: 72 / 100

Success Rate: 97.2%
Error Rate:    2.8%
P95 Latency:   1.84 sec

Evaluation Window:
Last 60 minutes

Requests:
4,821

Reasons:
⚠ Elevated error rate
⚠ Elevated latency
```

The detail page can optionally include a historical trend if the stored health snapshots provide enough information.

---

# 20. Testing Strategy

The health evaluation engine is the most important area for automated tests.

## Unit Tests

Examples:

```text
shouldReturnHealthyWhenMetricsAreWithinThresholds()

shouldReturnDegradedWhenErrorRateExceedsWarningThreshold()

shouldReturnUnhealthyWhenErrorRateExceedsCriticalThreshold()

shouldReturnDegradedWhenLatencyExceedsThreshold()

shouldNotFlagSingleFailureWithInsufficientSampleSize()

shouldReturnUnhealthyWhenMultipleCriticalSignalsArePresent()

shouldExplainWhyIntegrationIsDegraded()
```

## Repository Tests

Verify:

* Request retrieval
* Time-window filtering
* Health snapshot persistence
* Integration lookup

## Controller Tests

Verify:

* Dashboard renders successfully
* Integration detail page renders
* REST endpoints return expected responses
* Invalid integration IDs are handled correctly

---

# 21. Configuration

Health thresholds should not be hardcoded throughout the application.

They should be centralized and configurable.

For example:

```yaml
integration:
  health:
    evaluation-window-minutes: 60
    minimum-request-count: 20
    error-rate:
      degraded: 0.02
      unhealthy: 0.05
    latency:
      p95-degraded-ms: 1000
      p95-unhealthy-ms: 3000
    refresh-interval-seconds: 300
```

The actual values will be determined from the supplied dataset and justified in the design decision document.

---

# 22. Error Handling

The application should provide consistent error handling for:

* Invalid integration IDs
* Database failures
* Invalid request data
* Missing/insufficient metrics
* Unexpected application errors

Spring's centralized exception handling can be used for REST APIs.

For the UI, user-friendly error pages should be provided rather than exposing stack traces.

---

# 23. Observability of the Application

The health platform itself should also have basic operational visibility.

The application should log:

* Health evaluation execution
* Number of integrations evaluated
* Evaluation failures
* Data ingestion failures
* Unexpected exceptions

Example:

```text
Health evaluation completed:
5 integrations evaluated
4 healthy
1 degraded
0 unhealthy
```

No sensitive request payloads should be unnecessarily logged.

---

# 24. Deployment

For local development and assessment demonstration:

```text
Docker Compose
│
├── integration-health-app
│       └── Spring Boot
│
└── postgres
        └── PostgreSQL
```

The application will be packaged as a standard Spring Boot executable JAR.

---

# 25. Project Structure

```text
integration-health-platform/
│
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── com/example/integrationhealth/
│       │   └── resources/
│       │       ├── templates/
│       │       ├── static/
│       │       ├── db/migration/
│       │       └── application.yml
│       │
│       └── test/
│           └── java/
│
├── docker-compose.yml
├── Dockerfile
├── README.md
└── docs/
    ├── architecture.md
    ├── health-model.md
    └── decisions.md
```

---

# 26. Key Design Decisions

### Decision 1 — Spring Boot Monolith

A modular Spring Boot application is preferred over microservices.

**Reason:** The assessment problem does not require distributed deployment, and a modular monolith provides sufficient separation while minimizing operational complexity.

### Decision 2 — Thymeleaf Instead of React

The frontend will use Thymeleaf and Bootstrap.

**Reason:** The primary requirement is an operational dashboard rather than a complex client-side application. Server-side rendering reduces implementation overhead and keeps the solution focused on health evaluation.

### Decision 3 — PostgreSQL

PostgreSQL is used as the primary persistence layer.

**Reason:** The data is relational, time-window queries are important, and PostgreSQL provides strong querying and aggregation capabilities.

### Decision 4 — Dedicated Health Evaluator

Health classification will be isolated from controllers and persistence.

**Reason:** This is the most important business logic and should be deterministic, testable, explainable, and easy to evolve.

### Decision 5 — Scheduled Evaluation

The initial implementation uses scheduled health evaluation.

**Reason:** The supplied problem does not require real-time streaming. Scheduled evaluation provides an appropriate balance between freshness and implementation complexity.

### Decision 6 — No Unnecessary Infrastructure

Kafka, Redis, Kubernetes, and similar infrastructure are intentionally excluded.

**Reason:** They do not materially improve the solution for the stated problem and would increase operational complexity without providing sufficient value at this stage.

---

# 27. Assessment Focus

The implementation should optimize for the following:

1. **Sound health-model design**
2. **Evidence-based thresholds**
3. **Explainability**
4. **Clear handling of ambiguity**
5. **Good separation of concerns**
6. **Strong automated testing**
7. **Operational usability**
8. **Reasonable implementation complexity**
9. **Ability to explain trade-offs during the live defense**

The implementation should demonstrate that engineering decisions were made based on the customer's problem rather than technology preference.

---

# 28. Technology Stack Summary

```text
┌─────────────────────────────────────────┐
│              Presentation               │
│         Thymeleaf + Bootstrap 5         │
├─────────────────────────────────────────┤
│              Application                │
│       Java 21 + Spring Boot 3.x         │
├─────────────────────────────────────────┤
│              Persistence                │
│ Spring Data JPA + Hibernate + Flyway    │
├─────────────────────────────────────────┤
│                Database                 │
│                PostgreSQL               │
├─────────────────────────────────────────┤
│              Serialization              │
│                 Jackson                 │
├─────────────────────────────────────────┤
│                Testing                  │
│ JUnit 5 + Mockito + Spring Boot Test    │
├─────────────────────────────────────────┤
│             Infrastructure              │
│         Docker + Docker Compose         │
└─────────────────────────────────────────┘
```

**Python, FastAPI, Flask, React, and a separate Python/frontend application are not part of the proposed solution.**

The next useful step is to apply this design **against the actual starter repo and synthetic logs**, because the health thresholds, schema, and ingestion model should be evidence-driven rather than assumed.
