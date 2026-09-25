package com.paypilot.backend.controller;

import com.paypilot.backend.dto.HealthResponse;
import com.paypilot.backend.service.HealthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.paypilot.backend.security.JwtAuthenticationFilter;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@WebMvcTest(HealthController.class)
@AutoConfigureMockMvc(addFilters = false)
class HealthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HealthService healthService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldReturnHealthStatus() throws Exception {

        given(healthService.getHealth())
                .willReturn(new HealthResponse(
                        "paypilot-backend",
                        "UP",
                        "v1"
                ));

        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("paypilot-backend"))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.version").value("v1"));
    }
}