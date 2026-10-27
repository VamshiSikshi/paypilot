package com.paypilot.backend.domain.merchant;

import com.paypilot.backend.domain.audit.AuditLogService;
import com.paypilot.backend.domain.merchant.dto.MerchantResponse;
import com.paypilot.backend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MerchantServiceImpl implements MerchantService {

    private final MerchantRepository merchantRepository;
    private final AuditLogService auditLogService;

    public MerchantServiceImpl(
            MerchantRepository merchantRepository,
            AuditLogService auditLogService
    ) {
        this.merchantRepository = merchantRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    public MerchantResponse getMerchant(Long merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Merchant not found: " + merchantId
                ));

        return MerchantResponse.from(merchant);
    }

    @Override
    @Transactional
    public MerchantResponse updateStatus(
            Long merchantId,
            Long actorUserId,
            MerchantStatus status
    ) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Merchant not found: " + merchantId
                ));

        MerchantStatus previousStatus = merchant.getStatus();

        if (previousStatus != status) {
            merchant.updateStatus(status);

            String metadata = """
                    {
                      "previousStatus": "%s",
                      "newStatus": "%s"
                    }
                    """.formatted(previousStatus, status);

            auditLogService.createAuditLog(
                    merchantId,
                    actorUserId,
                    "MERCHANT_STATUS_CHANGED",
                    "MERCHANT",
                    merchantId,
                    "Merchant status changed from "
                            + previousStatus + " to " + status,
                    metadata,
                    null,
                    null
            );
        }

        return MerchantResponse.from(merchant);
    }
}