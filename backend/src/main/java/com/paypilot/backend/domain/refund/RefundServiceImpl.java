package com.paypilot.backend.domain.refund;

import com.paypilot.backend.domain.refund.dto.RefundResponse;
import com.paypilot.backend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RefundServiceImpl implements RefundService {

    private final RefundRepository refundRepository;

    public RefundServiceImpl(RefundRepository refundRepository) {
        this.refundRepository = refundRepository;
    }

    @Override
    public RefundResponse getRefund(Long merchantId, Long refundId) {
        Refund refund = refundRepository
                .findByMerchantIdAndId(merchantId, refundId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Refund not found: " + refundId
                ));

        return RefundResponse.from(refund);
    }
}