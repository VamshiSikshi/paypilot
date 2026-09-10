package com.paypilot.backend.controller;

import com.paypilot.backend.dto.ai.AiHealthResponse;
import com.paypilot.backend.exception.AiServiceUnavailableException;
import com.paypilot.backend.service.AiIntegrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiController.class)
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AiIntegrationService aiIntegrationService;

    @Test
    void shouldReturnAiHealth() throws Exception {

        given(aiIntegrationService.getAiHealth())
                .willReturn(new AiHealthResponse(
                        "paypilot-ai",
                        "UP",
                        "v1"
                ));

        mockMvc.perform(get("/api/v1/ai/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("paypilot-ai"))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.version").value("v1"));
    }

    @Test
    void shouldReturn503WhenAiServiceUnavailable() throws Exception {

        given(aiIntegrationService.getAiHealth())
                .willThrow(new AiServiceUnavailableException(
                        new RuntimeException("Connection refused")
                ));

        mockMvc.perform(get("/api/v1/ai/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("AI_SERVICE_UNAVAILABLE"))
                .andExpect(jsonPath("$.message")
                        .value("AI service is currently unavailable"));
    }
}