package com.paypilot.backend.domain.audit;

import com.paypilot.backend.domain.audit.dto.AuditLogResponse;
import com.paypilot.backend.exception.ResourceNotFoundException;
import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import com.paypilot.backend.domain.user.User;
import com.paypilot.backend.domain.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;

    public AuditLogServiceImpl(
            AuditLogRepository auditLogRepository,
            MerchantRepository merchantRepository,
            UserRepository userRepository
    ) {
        this.auditLogRepository = auditLogRepository;
        this.merchantRepository = merchantRepository;
        this.userRepository = userRepository;
    }

    @Override
    public AuditLogResponse createAuditLog(
            Long merchantId,
            Long userId,
            String eventType,
            String resourceType,
            Long resourceId,
            String description,
            String metadata,
            String ipAddress,
            String userAgent
    ) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Merchant not found: " + merchantId
                ));

        User user = null;

        if (userId != null) {
            user = userRepository.findByMerchantIdAndId(merchantId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found: " + userId
                    ));
        }

        AuditLog auditLog = new AuditLog(
                merchant,
                user,
                eventType,
                resourceType,
                resourceId,
                description,
                metadata,
                ipAddress,
                userAgent
        );

        return AuditLogResponse.from(auditLogRepository.save(auditLog));
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<AuditLogResponse> getAuditLogs(Long merchantId) {
        if (!merchantRepository.existsById(merchantId)) {
            throw new ResourceNotFoundException(
                    "Merchant not found: " + merchantId
            );
        }

        return auditLogRepository
                .findByMerchantIdOrderByCreatedAtDesc(merchantId)
                .stream()
                .map(AuditLogResponse::from)
                .toList();
    }
}