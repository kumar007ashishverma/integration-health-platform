# API Integration Health — Requirement Gathering Brainstorm

## 1. Context

### Customer

Brightline HR is an HR platform integrating with:

- Payroll
- E-signature
- ATS
- Directory synchronization
- Notification systems

The operations team has request logs for each integration.

### Current pain

Ops learns an integration is broken only when a customer submits a support ticket, often days after the failure began.

The desired experience is a morning-glance view that answers:

1. Which integrations are healthy?
2. Which need attention?
3. Why are they considered unhealthy?
4. How confident are we in that judgment?
5. Is the integration failing now, or did it fail briefly earlier?
6. When was the last successful/failed activity?
7. How frequently is the health picture refreshed?

---

## 2. Questions to Resolve

### Health semantics

- **What entity should receive a health status?**
  - **Integration-level:** One health status for the entire integration (e.g., Payroll, ATS, E-signature). This is the proposed primary operational view.
  - **Customer–integration level:** One health status for each customer using an integration. This can identify customer-specific failures that may be hidden by aggregate integration health.
  - **Endpoint-level:** One health status for each endpoint within an integration. This can identify which operation is failing within an otherwise healthy integration.
- **Proposed MVP:** Use **integration-level health as the primary unit of health**. Retain customer and endpoint information as diagnostic dimensions where those fields are present and reliable in the supplied logs.
- The health state should answer **“Is this integration healthy?”** while drill-down diagnostics should answer **“Why is it unhealthy?”**
- What counts as an error?
  - HTTP 5xx?
  - Timeouts?
  - Connection failures?
  - Selected 4xx?
  - Application-level failure fields?
- What does latency mean?
  - Mean?
  - p50?
  - p95?
  - p99?
- What time window is appropriate?
  - 5 min?
  - 15 min?
  - 1 hour?
  - 24 hours?
- How much traffic is enough evidence?
- What should happen when traffic is sparse?
- Should a single failure turn an integration red?
- Should a historical failure remain visible after recovery?

### Operational behavior

- How often should the dashboard refresh?
- Should health be push-based or polled?
- Do we need alerts now or only health visibility?
- Who owns an integration?
- Should there be maintenance windows?
- How do we prevent noisy state flapping?
- How do we explain state changes?

### Data

- What fields are present in the supplied logs?
- Are logs JSON, CSV, line-oriented text, or mixed?
- Are timestamps UTC?
- Is there a correlation/request ID?
- Is integration identity stable?
- Are customer identifiers present?
- Are request duration units consistent?
- Are retries represented separately?

### UX

Morning view should likely show:

| Integration | State | Error rate | p95 | Last success | Last failure | Evidence volume | Reason |
|---|---|---:|---:|---|---|---:|---|
| Payroll | Healthy | 0.2% | 310 ms | 08:58 | 07:42 | 1,240 | Within thresholds |
| ATS | Degraded | 3.4% | 1.2 s | 08:59 | 08:57 | 420 | Error rate elevated |
| E-signature | Unknown | — | — | — | — | 4 | Insufficient evidence |

---

## 3. Candidate Health Model

### State model

Four states are preferable to a binary green/red model:

- **Healthy** — enough evidence and all critical signals are within thresholds.
- **Degraded** — enough evidence exists, but one or more warning thresholds are exceeded.
- **Unhealthy** — strong evidence indicates the integration is failing materially.
- **Unknown** — there is not enough trustworthy evidence to make a health judgment.

Unknown is important. It prevents low-volume integrations from being falsely reported as healthy.

### Proposed initial policy

Use configurable defaults rather than hard-coding business policy:

- Rolling evaluation window: **15 minutes**
- Minimum observations: **20 requests**
- Error classification:
  - transport/connection failure = error
  - timeout = error
  - HTTP 5xx = error
  - HTTP 429 = error/warning depending on policy
  - HTTP 4xx = not automatically an integration failure
  - application-level failure = error when the source log explicitly marks it
- Latency signal: p95
- Healthy error rate: `< 1%`
- Degraded error rate: `1%–5%`
- Unhealthy error rate: `> 5%`
- Healthy p95: `< 500 ms`
- Degraded p95: `500 ms–1.5 s`
- Unhealthy p95: `> 1.5 s`
- State changes should use hysteresis to avoid flapping.

These are starting defaults. The supplied synthetic logs should be profiled before final threshold calibration.

---

## 4. Refresh Strategy

Candidate:

- Backend continuously calculates current aggregates from ingested events.
- Dashboard polls the health endpoint every **60 seconds**.
- A manual refresh is available.
- The API exposes `evaluated_at` and `data_freshness` so operators know whether they are looking at current evidence.

Why polling first:

- Simple to implement.
- Reliable across deployments.
- No websocket infrastructure.
- Meets the “often it refreshes” requirement without over-engineering.

---

## 5. Evidence & Explainability

Every health result should expose:

- state
- evaluated timestamp
- evaluation window
- sample count
- error count
- error rate
- p95 latency
- last success
- last failure
- reason codes
- human-readable explanation

Example:

> **Degraded** — 3.4% errors over the last 15 minutes (14/412 requests), above the 1% warning threshold. p95 latency remains healthy at 420 ms.

This makes the morning view actionable rather than merely decorative.

---

## 6. Edge Cases to Investigate

- No requests in the window.
- Fewer than minimum observations.
- Clock skew / future timestamps.
- Duplicate request records.
- Missing integration identifier.
- Missing duration.
- Malformed log record.
- Extremely large duration.
- Mixed timestamp timezones.
- Retry storms.
- A burst of failures followed by recovery.
- A permanently low-volume integration.
- One endpoint failing while another succeeds.
- Customer-specific failure hidden by aggregate health.
- Maintenance windows.
- Partial ingestion outage.

---

## 7. Non-Functional Requirements

- Deterministic health evaluation.
- Idempotent ingestion where request IDs exist.
- Secure handling of log payloads.
- No raw customer secrets in API responses.
- Useful structured logs and metrics.
- Testable policy engine.
- Configuration-driven thresholds.
- Graceful handling of malformed records.
- API response latency suitable for a morning dashboard.
- Clear operational audit trail for state transitions.

---

## 8. Suggested MVP

### Must have

1. Ingest supplied request logs.
2. Normalize them into a common event model.
3. Calculate rolling health per integration.
4. Return Healthy/Degraded/Unhealthy/Unknown.
5. Expose evidence behind each state.
6. Provide a simple health API.
7. Refresh every 60 seconds.
8. Persist normalized events and aggregates.
9. Add tests around the health policy.
10. Provide Docker-based local setup.

### Later

- Alerting.
- Slack/email notifications.
- Customer-level drilldown.
- Maintenance windows.
- Anomaly detection.
- Endpoint-level health.
- Historical charts.
- Multi-region ingestion.
- Streaming/event-bus ingestion.
