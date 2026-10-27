package com.paypilot.backend.domain.audit;

import com.paypilot.backend.domain.audit.dto.AuditLogResponse;
import com.paypilot.backend.domain.user.UserRole;
import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import com.paypilot.backend.domain.user.User;
import com.paypilot.backend.domain.user.UserRepository;
import com.paypilot.backend.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import com.paypilot.backend.domain.audit.AuditLogRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@Transactional
class AuditLogServiceTest {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldCreateAuditLog() {
        Merchant merchant = merchantRepository.save(
                new Merchant(
                        "razorpay_audit",
                        "Audit Merchant",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        User user = userRepository.save(
                new User(
                        merchant,
                        "Audit User",
                        "user@example.com",
                        UserRole.OWNER
                )
        );

        AuditLogResponse response = auditLogService.createAuditLog(
                merchant.getId(),
                user.getId(),
                "REFUND_REQUESTED",
                "PAYMENT",
                100L,
                "Refund requested",
                "{\"amount\":500}",
                "127.0.0.1",
                "Mozilla/5.0"
        );

        assertThat(response.id()).isNotNull();
        assertThat(response.merchantId()).isEqualTo(merchant.getId());
        assertThat(response.userId()).isEqualTo(user.getId());
        assertThat(response.eventType()).isEqualTo("REFUND_REQUESTED");
        assertThat(response.resourceType()).isEqualTo("PAYMENT");
        assertThat(response.resourceId()).isEqualTo(100L);
        assertThat(response.description()).isEqualTo("Refund requested");
        assertThat(response.metadata()).isEqualTo("{\"amount\":500}");
        assertThat(response.ipAddress()).isEqualTo("127.0.0.1");
        assertThat(response.userAgent()).isEqualTo("Mozilla/5.0");
        assertThat(response.createdAt()).isNotNull();
    }

    @Test
    void shouldCreateAuditLogWithoutUser() {
        Merchant merchant = merchantRepository.save(
                new Merchant(
                        "razorpay_audit",
                        "Audit Merchant",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        AuditLogResponse response = auditLogService.createAuditLog(
                merchant.getId(),
                null,
                "PAYMENT_SYNCED",
                "PAYMENT",
                101L,
                "Payment synchronized",
                null,
                null,
                null
        );

        assertThat(response.id()).isNotNull();
        assertThat(response.merchantId()).isEqualTo(merchant.getId());
        assertThat(response.userId()).isNull();
        assertThat(response.eventType()).isEqualTo("PAYMENT_SYNCED");
    }

    @Test
    void shouldRejectAuditLogWhenMerchantDoesNotExist() {
        assertThatThrownBy(() ->
                auditLogService.createAuditLog(
                        999L,
                        null,
                        "PAYMENT_SYNCED",
                        "PAYMENT",
                        101L,
                        "Payment synchronized",
                        null,
                        null,
                        null
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Merchant not found");
    }

    @Test
    void shouldRejectAuditLogWhenUserDoesNotBelongToMerchant() {
        Merchant firstMerchant = merchantRepository.save(
                new Merchant(
                        "razorpay_first",
                        "First Merchant",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        Merchant secondMerchant = merchantRepository.save(
                new Merchant(
                        "razorpay_other",
                        "Other Merchant",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        User user = userRepository.save(
                new User(
                        secondMerchant,
                        "Second User",
                        "second-user@example.com",
                        UserRole.OWNER
                )
        );

        assertThatThrownBy(() ->
                auditLogService.createAuditLog(
                        firstMerchant.getId(),
                        user.getId(),
                        "REFUND_REQUESTED",
                        "PAYMENT",
                        100L,
                        "Refund requested",
                        null,
                        null,
                        null
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void shouldGetAuditLogsForMerchant() {
        Merchant merchant = merchantRepository.save(
                new Merchant(
                        "razorpay_audit",
                        "Audit Merchant",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        auditLogService.createAuditLog(
                merchant.getId(),
                null,
                "PAYMENT_SYNCED",
                "PAYMENT",
                101L,
                "Payment synchronized",
                null,
                null,
                null
        );

        auditLogService.createAuditLog(
                merchant.getId(),
                null,
                "REFUND_REQUESTED",
                "PAYMENT",
                100L,
                "Refund requested",
                null,
                null,
                null
        );

        List<AuditLogResponse> logs =
                auditLogService.getAuditLogs(merchant.getId());

        assertThat(logs).hasSize(2);
        assertThat(logs)
                .extracting(AuditLogResponse::eventType)
                .containsExactlyInAnyOrder(
                        "PAYMENT_SYNCED",
                        "REFUND_REQUESTED"
                );
    }

    @Test
    void shouldNotReturnLogsFromAnotherMerchant() {
        Merchant firstMerchant = merchantRepository.save(
                new Merchant(
                        "razorpay_first",
                        "First Merchant",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        Merchant secondMerchant = merchantRepository.save(
                new Merchant(
                        "razorpay_other",
                        "Other Merchant",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        auditLogService.createAuditLog(
                firstMerchant.getId(),
                null,
                "PAYMENT_SYNCED",
                "PAYMENT",
                101L,
                "Payment synchronized",
                null,
                null,
                null
        );

        auditLogService.createAuditLog(
                secondMerchant.getId(),
                null,
                "REFUND_REQUESTED",
                "PAYMENT",
                102L,
                "Refund requested",
                null,
                null,
                null
        );

        List<AuditLogResponse> logs =
                auditLogService.getAuditLogs(firstMerchant.getId());

        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).eventType())
                .isEqualTo("PAYMENT_SYNCED");
    }

    @Test
    void shouldRejectGetAuditLogsWhenMerchantDoesNotExist() {
        assertThatThrownBy(() ->
                auditLogService.getAuditLogs(999L)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Merchant not found");
    }

    @Test
    void shouldRedactSensitiveFieldsFromAuditMetadata() {
        Merchant merchant = merchantRepository.save(
                new Merchant(
                        "razorpay_redact",
                        "Redaction Merchant",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        String metadata = """
            {
              "amount": 500,
              "currency": "INR",
              "password": "secret-password",
              "accessToken": "access-123",
              "clientSecret": "client-secret-456"
            }
            """;

        AuditLogResponse response = auditLogService.createAuditLog(
                merchant.getId(),
                null,
                "PAYMENT_CREATED",
                "PAYMENT",
                200L,
                "Payment created",
                metadata,
                null,
                null
        );

        assertThat(response.metadata())
                .contains("\"amount\":500")
                .contains("\"currency\":\"INR\"")
                .contains("\"password\":\"[REDACTED]\"")
                .contains("\"accessToken\":\"[REDACTED]\"")
                .contains("\"clientSecret\":\"[REDACTED]\"")
                .doesNotContain("secret-password")
                .doesNotContain("access-123")
                .doesNotContain("client-secret-456");
    }

    @Test
    void shouldRedactSensitiveFieldsRecursivelyInNestedMetadata() {
        Merchant merchant = merchantRepository.save(
                new Merchant(
                        "razorpay_nested",
                        "Nested Metadata Merchant",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        String metadata = """
            {
              "payment": {
                "amount": 500,
                "credentials": {
                  "apiKey": "api-key-value"
                }
              },
              "items": [
                {
                  "refreshToken": "refresh-token-value"
                }
              ]
            }
            """;

        AuditLogResponse response = auditLogService.createAuditLog(
                merchant.getId(),
                null,
                "PAYMENT_CREATED",
                "PAYMENT",
                201L,
                "Payment created",
                metadata,
                null,
                null
        );

        assertThat(response.metadata())
                .contains("\"amount\":500")
                .contains("\"apiKey\":\"[REDACTED]\"")
                .contains("\"refreshToken\":\"[REDACTED]\"")
                .doesNotContain("api-key-value")
                .doesNotContain("refresh-token-value");
    }

    @Test
    void shouldRejectInvalidJsonAuditMetadata() {
        Merchant merchant = merchantRepository.save(
                new Merchant(
                        "razorpay_invalid_json",
                        "Invalid JSON Merchant",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        assertThatThrownBy(() ->
                auditLogService.createAuditLog(
                        merchant.getId(),
                        null,
                        "PAYMENT_CREATED",
                        "PAYMENT",
                        202L,
                        "Payment created",
                        "{invalid-json",
                        null,
                        null
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Audit metadata must be valid JSON");
    }

    @Test
    void shouldPreserveNullAuditMetadata() {
        Merchant merchant = merchantRepository.save(
                new Merchant(
                        "razorpay_null_metadata",
                        "Null Metadata Merchant",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        AuditLogResponse response = auditLogService.createAuditLog(
                merchant.getId(),
                null,
                "PAYMENT_SYNCED",
                "PAYMENT",
                203L,
                "Payment synchronized",
                null,
                null,
                null
        );

        assertThat(response.metadata()).isNull();
    }
}