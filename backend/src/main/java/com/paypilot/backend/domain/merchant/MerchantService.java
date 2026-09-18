package com.paypilot.backend.domain.merchant;

import com.paypilot.backend.domain.merchant.dto.MerchantResponse;

public interface MerchantService {

    MerchantResponse getMerchant(Long merchantId);

    MerchantResponse updateStatus(Long merchantId, MerchantStatus status);
}