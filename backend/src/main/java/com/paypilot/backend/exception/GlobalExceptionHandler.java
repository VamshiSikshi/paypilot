package com.paypilot.backend.exception;

import com.paypilot.backend.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AiServiceUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleAiServiceUnavailable(
            AiServiceUnavailableException exception
    ) {
        return new ErrorResponse(
                "AI_SERVICE_UNAVAILABLE",
                exception.getMessage()
        );
    }
}