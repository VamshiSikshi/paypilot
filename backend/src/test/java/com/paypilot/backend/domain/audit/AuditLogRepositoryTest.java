package com.paypilot.backend.domain.audit;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.user.User;
import com.paypilot.backend.domain.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.flyway.enabled=false")
class AuditLogRepositoryTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private com.paypilot.backend.domain.merchant.MerchantRepository merchantRepository;

    @Autowired
    private com.paypilot.backend.domain.user.UserRepository userRepository;

    @Test
    void shouldSaveAndFindAuditLogsByMerchant() {
        Merchant merchant = merchantRepository.save(
                new Merchant(
                        "acc_audit_001",
                        "Audit Business",
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );

        User user = userRepository.save(
                new User(
                        merchant,
                        "audit@example.com",
                        "Audit User",
                        UserRole.OWNER
                )
        );

        AuditLog firstLog = auditLogRepository.save(
                new AuditLog(
                        merchant,
                        user,
                        "REFUND_REQUESTED",
                        "PAYMENT",
                        100L,
                        "Refund requested",
                        "{\"amount\":500}",
                        "127.0.0.1",
                        "Mozilla/5.0"
                )
        );

        AuditLog secondLog = auditLogRepository.save(
                new AuditLog(
                        merchant,
                        null,
                        "PAYMENT_SYNCED",
                        "PAYMENT",
                        101L,
                        "Payment synchronized",
                        null,
                        null,
                        null
                )
        );

        List<AuditLog> logs =
                auditLogRepository.findByMerchantIdOrderByCreatedAtDesc(merchant.getId());

        assertThat(logs).hasSize(2);
        assertThat(logs).contains(firstLog, secondLog);

        assertThat(firstLog.getMerchant().getId()).isEqualTo(merchant.getId());
        assertThat(firstLog.getUser().getId()).isEqualTo(user.getId());
        assertThat(firstLog.getEventType()).isEqualTo("REFUND_REQUESTED");
        assertThat(firstLog.getResourceType()).isEqualTo("PAYMENT");
        assertThat(firstLog.getResourceId()).isEqualTo(100L);
        assertThat(firstLog.getDescription()).isEqualTo("Refund requested");
        assertThat(firstLog.getMetadata()).isEqualTo("{\"amount\":500}");
        assertThat(firstLog.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(firstLog.getUserAgent()).isEqualTo("Mozilla/5.0");
        assertThat(firstLog.getCreatedAt()).isNotNull();

        assertThat(secondLog.getUser()).isNull();
    }
}
