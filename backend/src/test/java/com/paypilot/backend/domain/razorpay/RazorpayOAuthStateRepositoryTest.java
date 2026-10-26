
package com.paypilot.backend.domain.razorpay;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.flyway.enabled=false")
class RazorpayOAuthStateRepositoryTest {

    @Autowired
    private RazorpayOAuthStateRepository stateRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldConsumeValidStateOnce() {
        Merchant merchant = createMerchant("oauth_consume_1");
        RazorpayOAuthState state = createState(
                merchant,
                "a".repeat(64),
                Instant.now().plusSeconds(300)
        );

        RazorpayOAuthState saved = stateRepository.saveAndFlush(state);

        int firstAttempt = stateRepository.consumeIfValid(
                saved.getStateHash(),
                merchant.getId()
        );

        assertThat(firstAttempt).isEqualTo(1);

        RazorpayOAuthState consumed = stateRepository.findById(
                saved.getId()
        ).orElseThrow();

        assertThat(consumed.getConsumedAt()).isNotNull();

        int secondAttempt = stateRepository.consumeIfValid(
                saved.getStateHash(),
                merchant.getId()
        );

        assertThat(secondAttempt).isZero();
    }

    @Test
    void shouldNotConsumeExpiredState() {
        Merchant merchant = createMerchant("oauth_expired_1");
        RazorpayOAuthState state = createState(
                merchant,
                "b".repeat(64),
                Instant.now().plusSeconds(300)
        );

        RazorpayOAuthState saved = stateRepository.saveAndFlush(state);

        jdbcTemplate.update(
                """
                UPDATE razorpay_oauth_states
                SET expires_at = ?
                WHERE id = ?
                """,
                Timestamp.from(Instant.now().minusSeconds(60)),
                saved.getId()
        );

        int result = stateRepository.consumeIfValid(
                saved.getStateHash(),
                merchant.getId()
        );

        assertThat(result).isZero();
    }

    @Test
    void shouldNotConsumeStateForDifferentMerchant() {
        Merchant owner = createMerchant("oauth_owner_1");
        Merchant otherMerchant = createMerchant("oauth_other_1");

        RazorpayOAuthState state = createState(
                owner,
                "c".repeat(64),
                Instant.now().plusSeconds(300)
        );

        RazorpayOAuthState saved = stateRepository.saveAndFlush(state);

        int result = stateRepository.consumeIfValid(
                saved.getStateHash(),
                otherMerchant.getId()
        );

        assertThat(result).isZero();

        RazorpayOAuthState unchanged = stateRepository.findById(
                saved.getId()
        ).orElseThrow();

        assertThat(unchanged.getConsumedAt()).isNull();
    }

    @Test
    void shouldNotConsumeUnknownStateHash() {
        Merchant merchant = createMerchant("oauth_unknown_1");

        int result = stateRepository.consumeIfValid(
                "d".repeat(64),
                merchant.getId()
        );

        assertThat(result).isZero();
    }

    private Merchant createMerchant(String suffix) {
        return merchantRepository.saveAndFlush(
                new Merchant(
                        "acc_" + suffix,
                        "OAuth Test " + suffix,
                        "IN",
                        "INR",
                        "Asia/Kolkata"
                )
        );
    }

    private RazorpayOAuthState createState(
            Merchant merchant,
            String stateHash,
            Instant expiresAt
    ) {
        return new RazorpayOAuthState(
                merchant,
                stateHash,
                expiresAt
        );
    }
}
