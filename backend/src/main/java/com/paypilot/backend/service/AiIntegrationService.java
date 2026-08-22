package com.paypilot.backend.service;

import com.paypilot.backend.client.AiServiceClient;
import com.paypilot.backend.dto.ai.AiHealthResponse;
import org.springframework.stereotype.Service;

@Service
public class AiIntegrationService {

    private final AiServiceClient aiServiceClient;

    public AiIntegrationService(AiServiceClient aiServiceClient) {
        this.aiServiceClient = aiServiceClient;
    }

    public AiHealthResponse getAiHealth() {
        return aiServiceClient.getHealth();
    }
}