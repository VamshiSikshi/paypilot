package com.paypilot.backend.domain.action;

public enum ActionExecutionStatus {
    PENDING,
    CONFIRMATION_REQUIRED,
    CONFIRMED,
    EXECUTING,
    COMPLETED,
    FAILED,
    CANCELLED
}