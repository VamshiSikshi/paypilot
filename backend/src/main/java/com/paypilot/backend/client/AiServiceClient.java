package com.paypilot.backend.client;

import com.paypilot.backend.dto.ai.AiHealthResponse;
import com.paypilot.backend.exception.AiServiceUnavailableException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AiServiceClient {

    private final RestClient restClient;

    public AiServiceClient(RestClient aiServiceRestClient) {
        this.restClient = aiServiceRestClient;
    }

    public AiHealthResponse getHealth() {
        try {
            return restClient
                    .get()
                    .uri("/api/v1/health")
                    .retrieve()
                    .body(AiHealthResponse.class);
        } catch (Exception exception) {
            throw new AiServiceUnavailableException(exception);
        }
    }
}