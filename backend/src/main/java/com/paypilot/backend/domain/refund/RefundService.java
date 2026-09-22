package com.paypilot.backend.domain.refund;

import com.paypilot.backend.domain.refund.dto.RefundResponse;

public interface RefundService {

    RefundResponse getRefund(Long merchantId, Long refundId);
}