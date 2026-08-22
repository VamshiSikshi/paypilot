package com.paypilot.backend.controller;

import com.paypilot.backend.dto.ai.AiHealthResponse;
import com.paypilot.backend.service.AiIntegrationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private final AiIntegrationService aiIntegrationService;

    public AiController(AiIntegrationService aiIntegrationService) {
        this.aiIntegrationService = aiIntegrationService;
    }

    @GetMapping("/health")
    public AiHealthResponse health() {
        return aiIntegrationService.getAiHealth();
    }
}