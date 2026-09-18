package com.paypilot.backend.domain.merchant.dto;

import com.paypilot.backend.domain.merchant.MerchantStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateMerchantStatusRequest(
        @NotNull MerchantStatus status
) {
}