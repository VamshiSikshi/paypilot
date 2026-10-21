package com.paypilot.backend.domain.razorpay;

import com.paypilot.backend.domain.merchant.Merchant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class RazorpayConnectionLifecycleTest {

    private Merchant merchant;

    @BeforeEach
    void setUp() {
        merchant = new Merchant(
                "acct_merchant_test",
                "Test Business",
                "IN",
                "INR",
                "Asia/Kolkata"
        );
    }

    @Test
    void shouldStartConnectedAfterVerifiedAuthorization() {
        RazorpayConnection connection =
                new RazorpayConnection(merchant, "acct_test_123");

        assertThat(connection.getStatus())
                .isEqualTo(RazorpayConnectionStatus.CONNECTED);
        assertThat(connection.getExternalAccountId())
                .isEqualTo("acct_test_123");
        assertThat(connection.getLastConnectedAt()).isNotNull();
        assertThat(connection.getCreatedAt()).isNotNull();
        assertThat(connection.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldRejectBlankExternalAccountId() {
        assertThatIllegalArgumentException()
                .isThrownBy(() ->
                        new RazorpayConnection(merchant, " "));
    }

    @Test
    void shouldRejectNullMerchant() {
        assertThatNullPointerException()
                .isThrownBy(() ->
                        new RazorpayConnection(null, "acct_test_123"));
    }

    @Test
    void shouldMarkConnectionDisconnected() {
        RazorpayConnection connection =
                new RazorpayConnection(merchant, "acct_test_123");

        connection.markDisconnected();

        assertThat(connection.getStatus())
                .isEqualTo(RazorpayConnectionStatus.DISCONNECTED);
        assertThat(connection.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldMarkConnectionExpired() {
        RazorpayConnection connection =
                new RazorpayConnection(merchant, "acct_test_123");

        connection.markExpired();

        assertThat(connection.getStatus())
                .isEqualTo(RazorpayConnectionStatus.EXPIRED);
    }

    @Test
    void shouldMarkConnectionError() {
        RazorpayConnection connection =
                new RazorpayConnection(merchant, "acct_test_123");

        connection.markError();

        assertThat(connection.getStatus())
                .isEqualTo(RazorpayConnectionStatus.ERROR);
    }

    @Test
    void shouldUpdateTokenReferencesAndReconnectTimestamp() {
        RazorpayConnection connection =
                new RazorpayConnection(merchant, "acct_test_123");

        connection.markDisconnected();

        connection.updateTokenReferences(
                "vault/access/123",
                "vault/refresh/123",
                Instant.parse("2027-01-01T00:00:00Z"),
                "payments:read"
        );

        assertThat(connection.getStatus())
                .isEqualTo(RazorpayConnectionStatus.CONNECTED);
        assertThat(connection.getAccessTokenReference())
                .isEqualTo("vault/access/123");
        assertThat(connection.getRefreshTokenReference())
                .isEqualTo("vault/refresh/123");
        assertThat(connection.getTokenExpiresAt())
                .isEqualTo(Instant.parse("2027-01-01T00:00:00Z"));
        assertThat(connection.getGrantedScopes()).isEqualTo("payments:read");
        assertThat(connection.getLastConnectedAt()).isNotNull();
    }

    @Test
    void shouldRecordSuccessfulSyncTimestamp() {
        RazorpayConnection connection =
                new RazorpayConnection(merchant, "acct_test_123");
        Instant syncedAt = Instant.parse("2026-10-21T12:00:00Z");

        connection.recordSuccessfulSync(syncedAt);

        assertThat(connection.getLastSyncedAt()).isEqualTo(syncedAt);
        assertThat(connection.getUpdatedAt()).isEqualTo(syncedAt);
    }

    @Test
    void shouldRejectNullSyncTimestamp() {
        RazorpayConnection connection =
                new RazorpayConnection(merchant, "acct_test_123");

        assertThatNullPointerException()
                .isThrownBy(() -> connection.recordSuccessfulSync(null));
    }

    @Test
    void shouldRejectBlankAccessTokenReference() {
        RazorpayConnection connection =
                new RazorpayConnection(merchant, "acct_test_123");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> connection.updateTokenReferences(
                        " ",
                        "vault/refresh/123",
                        Instant.parse("2027-01-01T00:00:00Z"),
                        "payments:read"
                ));
    }
}