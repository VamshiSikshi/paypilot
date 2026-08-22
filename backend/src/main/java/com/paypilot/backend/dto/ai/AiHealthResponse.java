package com.paypilot.backend.dto.ai;

public record AiHealthResponse(
        String service,
        String status,
        String version
) {
}