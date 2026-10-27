package com.paypilot.backend.domain.merchant;

import com.paypilot.backend.domain.merchant.dto.MerchantResponse;
import com.paypilot.backend.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.paypilot.backend.domain.audit.AuditLogService;
import com.paypilot.backend.domain.audit.dto.AuditLogResponse;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MerchantServiceTest {

    @Mock
    private MerchantRepository merchantRepository;

    private MerchantService merchantService;

    @Mock
    private AuditLogService auditLogService;

    @BeforeEach
    void setUp() {
        merchantService = new MerchantServiceImpl(
                merchantRepository,
                auditLogService
        );
    }

    @Test
    void getMerchant_shouldReturnMerchantResponse() {
        Merchant merchant = new Merchant(
                "acc_test123",
                "Test Business",
                "IN",
                "INR",
                "Asia/Kolkata"
        );

        when(merchantRepository.findById(1L))
                .thenReturn(Optional.of(merchant));

        MerchantResponse response = merchantService.getMerchant(1L);

        assertEquals("acc_test123", response.razorpayAccountId());
        assertEquals("Test Business", response.businessName());
        assertEquals("IN", response.country());
        assertEquals("INR", response.defaultCurrency());
        assertEquals("Asia/Kolkata", response.timezone());
        assertEquals(MerchantStatus.ACTIVE, response.status());

        verify(merchantRepository).findById(1L);
    }

    @Test
    void getMerchant_shouldThrowWhenMerchantDoesNotExist() {
        when(merchantRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> merchantService.getMerchant(999L)
        );

        verify(merchantRepository).findById(999L);
    }

    @Test
    void updateStatus_shouldUpdateMerchantStatusAndCreateAuditLog() {
        Merchant merchant = new Merchant(
                "acc_test123",
                "Test Business",
                "IN",
                "INR",
                "Asia/Kolkata"
        );

        when(merchantRepository.findById(1L))
                .thenReturn(Optional.of(merchant));

        MerchantResponse response = merchantService.updateStatus(
                1L,
                10L,
                MerchantStatus.SUSPENDED
        );

        assertEquals(MerchantStatus.SUSPENDED, response.status());
        assertEquals(MerchantStatus.SUSPENDED, merchant.getStatus());

        verify(auditLogService).createAuditLog(
                eq(1L),
                eq(10L),
                eq("MERCHANT_STATUS_CHANGED"),
                eq("MERCHANT"),
                eq(1L),
                contains("ACTIVE"),
                contains("previousStatus"),
                isNull(),
                isNull()
        );
    }

    @Test
    void updateStatus_shouldThrowWhenMerchantDoesNotExist() {
        when(merchantRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> merchantService.updateStatus(
                        999L,
                        10L,
                        MerchantStatus.SUSPENDED
                )
        );

        verify(merchantRepository).findById(999L);
    }

    @Test
    void updateStatus_shouldNotCreateAuditLogWhenStatusIsUnchanged() {
        Merchant merchant = new Merchant(
                "acc_test123",
                "Test Business",
                "IN",
                "INR",
                "Asia/Kolkata"
        );

        when(merchantRepository.findById(1L))
                .thenReturn(Optional.of(merchant));

        MerchantResponse response = merchantService.updateStatus(
                1L,
                10L,
                MerchantStatus.ACTIVE
        );

        assertEquals(MerchantStatus.ACTIVE, response.status());

        verifyNoInteractions(auditLogService);
    }
}