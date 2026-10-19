package com.brightline.integrationhealth.health;

import java.time.Instant;

public record HealthMetrics(long requestCount, long failureCount, double errorRate,
                            Double p95LatencyMs, Instant windowStart, Instant windowEnd) {
}
