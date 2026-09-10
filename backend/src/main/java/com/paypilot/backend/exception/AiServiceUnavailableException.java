package com.paypilot.backend.exception;

public class AiServiceUnavailableException extends RuntimeException {

    public AiServiceUnavailableException(Throwable cause) {
        super("AI service is currently unavailable", cause);
    }
}