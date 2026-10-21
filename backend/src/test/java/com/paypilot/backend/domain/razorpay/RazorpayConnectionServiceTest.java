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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RazorpayConnectionServiceTest {

    @Mock
    private RazorpayConnectionRepository connectionRepository;

    private RazorpayConnectionService connectionService;

    private Merchant merchant;
    private RazorpayConnection connection;

    @BeforeEach
    void setUp() {
        connectionService =
                new RazorpayConnectionServiceImpl(connectionRepository);

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
}
