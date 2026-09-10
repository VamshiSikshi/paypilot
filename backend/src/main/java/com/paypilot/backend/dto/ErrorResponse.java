package com.paypilot.backend.dto;

public record ErrorResponse(
        String error,
        String message
) {
}