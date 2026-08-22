package com.paypilot.backend.dto;

public record HealthResponse(
        String service,
        String status,
        String version
) {
}