package com.paypilot.backend.domain.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAIConversationRequest(

        @NotBlank
        @Size(max = 200)
        String title

) {
}
