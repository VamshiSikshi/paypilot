package com.paypilot.backend.domain.reconciliation;

import com.paypilot.backend.domain.reconciliation.dto.ReconciliationResponse;
import com.paypilot.backend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReconciliationServiceImpl implements ReconciliationService {

    private final SettlementReconciliationRepository reconciliationRepository;

    public ReconciliationServiceImpl(
            SettlementReconciliationRepository reconciliationRepository
    ) {
        this.reconciliationRepository = reconciliationRepository;
    }

    @Override
    public ReconciliationResponse getReconciliation(
            Long merchantId,
            Long reconciliationId
    ) {
        SettlementReconciliation reconciliation = reconciliationRepository
                .findByMerchantIdAndId(merchantId, reconciliationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Reconciliation not found: " + reconciliationId
                        )
                );

        return ReconciliationResponse.from(reconciliation);
    }

    @Override
    public List<ReconciliationResponse> getReconciliationsBySettlement(
            Long merchantId,
            Long settlementId
    ) {
        return reconciliationRepository
                .findAllByMerchantIdAndSettlementId(merchantId, settlementId)
                .stream()
                .map(ReconciliationResponse::from)
                .toList();
    }

    @Override
    public ReconciliationResponse getReconciliationByRazorpayEntity(
            Long merchantId,
            String razorpayEntityId
    ) {
        SettlementReconciliation reconciliation = reconciliationRepository
                .findByMerchantIdAndRazorpayEntityId(
                        merchantId,
                        razorpayEntityId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Reconciliation not found: " + razorpayEntityId
                        )
                );

        return ReconciliationResponse.from(reconciliation);
    }
}