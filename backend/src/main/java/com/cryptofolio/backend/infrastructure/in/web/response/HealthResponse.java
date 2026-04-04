package com.cryptofolio.backend.infrastructure.in.web.response;

import java.time.Instant;

public record HealthResponse(String status, String service, Instant timestamp) {
}
