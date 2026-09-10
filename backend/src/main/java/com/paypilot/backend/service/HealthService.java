package com.paypilot.backend.service;

import com.paypilot.backend.dto.HealthResponse;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

    public HealthResponse getHealth() {
        return new HealthResponse(
                "paypilot-backend",
                "UP",
                "v1"
        );
    }
}