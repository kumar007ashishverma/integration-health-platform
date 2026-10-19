package com.brightline.integrationhealth.health;

import java.time.Instant;
import java.util.List;

public record HealthResult(HealthStatus status, HealthMetrics metrics, Integer score,
                           List<String> reasons, Instant evaluatedAt) {
}
