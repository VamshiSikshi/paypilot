package com.paypilot.backend.domain.reconciliation;

import com.paypilot.backend.domain.reconciliation.dto.ReconciliationResponse;

import java.util.List;

public interface ReconciliationService {

    ReconciliationResponse getReconciliation(
            Long merchantId,
            Long reconciliationId
    );

    List<ReconciliationResponse> getReconciliationsBySettlement(
            Long merchantId,
            Long settlementId
    );

    ReconciliationResponse getReconciliationByRazorpayEntity(
            Long merchantId,
            String razorpayEntityId
    );
}