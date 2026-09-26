package com.paypilot.backend.domain.reconciliation.dto;

import com.paypilot.backend.domain.reconciliation.ReconciliationTransactionType;
import com.paypilot.backend.domain.reconciliation.SettlementReconciliation;

import java.time.Instant;

public record ReconciliationResponse(
        Long id,
        Long merchantId,
        Long settlementId,
        String razorpayEntityId,
        ReconciliationTransactionType transactionType,
        Long debit,
        Long credit,
        Long amount,
        String currency,
        Long fee,
        Long tax,
        Boolean onHold,
        Boolean settled,
        Instant createdAt,
        Instant settledAt,
        String description,
        String notes,
        String razorpayPaymentId,
        String razorpayOrderId,
        String orderReceipt,
        String method,
        String cardNetwork,
        String cardIssuer,
        String cardType,
        String disputeId,
        String settlementUtr,
        Instant lastSyncedAt
) {

    public static ReconciliationResponse from(
            SettlementReconciliation reconciliation
    ) {
        return new ReconciliationResponse(
                reconciliation.getId(),
                reconciliation.getMerchant().getId(),
                reconciliation.getSettlement().getId(),
                reconciliation.getRazorpayEntityId(),
                reconciliation.getTransactionType(),
                reconciliation.getDebit(),
                reconciliation.getCredit(),
                reconciliation.getAmount(),
                reconciliation.getCurrency(),
                reconciliation.getFee(),
                reconciliation.getTax(),
                reconciliation.getOnHold(),
                reconciliation.getSettled(),
                reconciliation.getCreatedAt(),
                reconciliation.getSettledAt(),
                reconciliation.getDescription(),
                reconciliation.getNotes(),
                reconciliation.getRazorpayPaymentId(),
                reconciliation.getRazorpayOrderId(),
                reconciliation.getOrderReceipt(),
                reconciliation.getMethod(),
                reconciliation.getCardNetwork(),
                reconciliation.getCardIssuer(),
                reconciliation.getCardType(),
                reconciliation.getDisputeId(),
                reconciliation.getSettlementUtr(),
                reconciliation.getLastSyncedAt()
        );
    }
}