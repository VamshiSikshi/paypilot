package com.paypilot.backend.domain.razorpay;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RazorpayConnectionServiceTest {

    @Mock
    private RazorpayConnectionRepository connectionRepository;

    @Mock
    private MerchantRepository merchantRepository;

    private RazorpayConnectionService connectionService;

    private Merchant merchant;
    private RazorpayConnection connection;

    @BeforeEach
    void setUp() {
        connectionService = new RazorpayConnectionServiceImpl(
                connectionRepository,
                merchantRepository
        );

        merchant = new Merchant(
                "acc_merchant_test",
                "Test Business",
                "IN",
                "INR",
                "Asia/Kolkata"
        );

        connection = new RazorpayConnection(
                merchant,
                "acct_external_test"
        );
    }

    @Test
    void getConnection_shouldReturnExistingConnection() {
        when(connectionRepository.findByMerchantId(1L))
                .thenReturn(Optional.of(connection));

        RazorpayConnection result =
                connectionService.getConnection(1L);

        assertSame(connection, result);
        assertEquals(
                RazorpayConnectionStatus.CONNECTED,
                result.getStatus()
        );

        verify(connectionRepository).findByMerchantId(1L);
    }

    @Test
    void getConnection_shouldThrowWhenConnectionDoesNotExist() {
        when(connectionRepository.findByMerchantId(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> connectionService.getConnection(999L)
        );
    }

    @Test
    void disconnect_shouldMarkConnectionDisconnected() {
        when(connectionRepository.findByMerchantId(1L))
                .thenReturn(Optional.of(connection));

        RazorpayConnection result =
                connectionService.disconnect(1L);

        assertSame(connection, result);
        assertEquals(
                RazorpayConnectionStatus.DISCONNECTED,
                result.getStatus()
        );
        verify(connectionRepository).findByMerchantId(1L);
    }

    @Test
    void disconnect_shouldThrowWhenConnectionDoesNotExist() {
        when(connectionRepository.findByMerchantId(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> connectionService.disconnect(999L)
        );
    }

    @Test
    void recordSuccessfulSync_shouldUpdateSyncTimestamp() {
        when(connectionRepository.findByMerchantId(1L))
                .thenReturn(Optional.of(connection));

        Instant syncedAt = Instant.parse("2026-10-21T12:00:00Z");

        RazorpayConnection result =
                connectionService.recordSuccessfulSync(1L, syncedAt);

        assertSame(connection, result);
        assertEquals(syncedAt, result.getLastSyncedAt());
        assertEquals(syncedAt, result.getUpdatedAt());
    }

    @Test
    void recordSuccessfulSync_shouldThrowWhenConnectionDoesNotExist() {
        when(connectionRepository.findByMerchantId(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> connectionService.recordSuccessfulSync(
                        999L,
                        Instant.parse("2026-10-21T12:00:00Z")
                )
        );
    }

    @Test
    void recordSuccessfulSync_shouldRejectNullTimestamp() {
        assertThrows(
                NullPointerException.class,
                () -> connectionService.recordSuccessfulSync(1L, null)
        );

        verifyNoInteractions(connectionRepository);
    }

    @Test
    void connectAuthorizedAccount_shouldCreateAndSaveNewConnection() {
        when(merchantRepository.findById(1L))
                .thenReturn(Optional.of(merchant));
        when(connectionRepository.findByMerchantId(1L))
                .thenReturn(Optional.empty());
        when(connectionRepository.save(any(RazorpayConnection.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Instant expiresAt = Instant.parse("2027-01-01T00:00:00Z");

        RazorpayConnection result = connectionService.connectAuthorizedAccount(
                1L,
                "acct_external_test",
                "razorpay-oauth-token:41",
                "razorpay-oauth-token:41",
                expiresAt,
                "payments:read"
        );

        assertEquals("acct_external_test", result.getExternalAccountId());
        assertEquals(RazorpayConnectionStatus.CONNECTED, result.getStatus());
        assertEquals(
                "razorpay-oauth-token:41",
                result.getAccessTokenReference()
        );
        assertEquals(
                "razorpay-oauth-token:41",
                result.getRefreshTokenReference()
        );
        assertEquals(expiresAt, result.getTokenExpiresAt());
        assertEquals("payments:read", result.getGrantedScopes());

        verify(connectionRepository).save(result);
    }

    @Test
    void connectAuthorizedAccount_shouldUpdateExistingConnection() {
        when(merchantRepository.findById(1L))
                .thenReturn(Optional.of(merchant));
        when(connectionRepository.findByMerchantId(1L))
                .thenReturn(Optional.of(connection));
        when(connectionRepository.save(connection)).thenReturn(connection);

        connection.markDisconnected();

        RazorpayConnection result = connectionService.connectAuthorizedAccount(
                1L,
                "acct_external_test",
                "razorpay-oauth-token:42",
                "razorpay-oauth-token:42",
                Instant.parse("2027-02-01T00:00:00Z"),
                "payments:read payments:write"
        );

        assertSame(connection, result);
        assertEquals(RazorpayConnectionStatus.CONNECTED, result.getStatus());
        assertEquals(
                "razorpay-oauth-token:42",
                result.getAccessTokenReference()
        );
        assertEquals(
                "payments:read payments:write",
                result.getGrantedScopes()
        );

        verify(connectionRepository).save(connection);
    }

    @Test
    void connectAuthorizedAccount_shouldRejectDifferentExternalAccount() {
        when(merchantRepository.findById(1L))
                .thenReturn(Optional.of(merchant));
        when(connectionRepository.findByMerchantId(1L))
                .thenReturn(Optional.of(connection));

        assertThrows(
                IllegalStateException.class,
                () -> connectionService.connectAuthorizedAccount(
                        1L,
                        "different_external_account",
                        "razorpay-oauth-token:43",
                        "razorpay-oauth-token:43",
                        Instant.parse("2027-03-01T00:00:00Z"),
                        "payments:read"
                )
        );

        verify(connectionRepository, never())
                .save(any(RazorpayConnection.class));
    }
}
