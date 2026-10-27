
package com.paypilot.backend.domain.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.paypilot.backend.domain.audit.dto.AuditLogResponse;
import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import com.paypilot.backend.domain.user.User;
import com.paypilot.backend.domain.user.UserRepository;
import com.paypilot.backend.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Transactional
public class AuditLogServiceImpl implements AuditLogService {

    private static final String REDACTED = "[REDACTED]";

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password",
            "passwordhash",
            "secret",
            "clientsecret",
            "accesstoken",
            "refreshtoken",
            "authorization",
            "token",
            "cvv",
            "cardnumber",
            "apikey"
    );

    private final AuditLogRepository auditLogRepository;
    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

        String sanitizedMetadata = sanitizeMetadata(metadata);

        AuditLog auditLog = new AuditLog(
                merchant,
                user,
                eventType,
                resourceType,
                resourceId,
                description,
                sanitizedMetadata,
                ipAddress,
                userAgent
        );

        return AuditLogResponse.from(auditLogRepository.save(auditLog));
    }

    private String sanitizeMetadata(String metadata) {
        if (metadata == null) {
            return null;
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(metadata);

            if (root == null) {
                throw new IllegalArgumentException(
                        "Audit metadata must contain a JSON value"
                );
            }

            redactSensitiveFields(root);

            return OBJECT_MAPPER.writeValueAsString(root);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException(
                    "Audit metadata must be valid JSON",
                    exception
            );
        }
    }

    private void redactSensitiveFields(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<String> fieldNames = objectNode.fieldNames();

            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();

                if (isSensitiveKey(fieldName)) {
                    objectNode.put(fieldName, REDACTED);
                } else {
                    redactSensitiveFields(objectNode.get(fieldName));
                }
            }
        } else if (node.isArray()) {
            node.forEach(this::redactSensitiveFields);
        }
    }

    private boolean isSensitiveKey(String key) {
        String normalizedKey = key
                .replaceAll("[^a-zA-Z0-9]", "")
                .toLowerCase(Locale.ROOT);

        return SENSITIVE_KEYS.contains(normalizedKey);
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