package com.paypilot.backend.domain.settlement;

import com.paypilot.backend.domain.settlement.dto.SettlementResponse;

public interface SettlementService {

    SettlementResponse getSettlement(Long merchantId, Long settlementId);
}