package com.paypilot.backend.domain.settlement;

import com.paypilot.backend.domain.settlement.dto.SettlementResponse;
import com.paypilot.backend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SettlementServiceImpl implements SettlementService {

    private final SettlementRepository settlementRepository;

    public SettlementServiceImpl(SettlementRepository settlementRepository) {
        this.settlementRepository = settlementRepository;
    }

    @Override
    public SettlementResponse getSettlement(Long merchantId, Long settlementId) {
        Settlement settlement = settlementRepository
                .findByMerchantIdAndId(merchantId, settlementId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Settlement not found: " + settlementId
                        )
                );

        return SettlementResponse.from(settlement);
    }
}