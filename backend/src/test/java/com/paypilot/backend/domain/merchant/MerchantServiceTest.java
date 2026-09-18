package com.paypilot.backend.domain.merchant;

import com.paypilot.backend.domain.merchant.dto.MerchantResponse;
import com.paypilot.backend.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MerchantServiceTest {

    @Mock
    private MerchantRepository merchantRepository;

    private MerchantService merchantService;

    @BeforeEach
    void setUp() {
        merchantService = new MerchantServiceImpl(merchantRepository);
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
    void updateStatus_shouldUpdateMerchantStatus() {
        Merchant merchant = new Merchant(
                "acc_test123",
                "Test Business",
                "IN",
                "INR",
                "Asia/Kolkata"
        );

        when(merchantRepository.findById(1L))
                .thenReturn(Optional.of(merchant));

        MerchantResponse response =
                merchantService.updateStatus(1L, MerchantStatus.SUSPENDED);

        assertEquals(MerchantStatus.SUSPENDED, response.status());
        assertEquals(MerchantStatus.SUSPENDED, merchant.getStatus());

        verify(merchantRepository).findById(1L);
    }

    @Test
    void updateStatus_shouldThrowWhenMerchantDoesNotExist() {
        when(merchantRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> merchantService.updateStatus(
                        999L,
                        MerchantStatus.SUSPENDED
                )
        );

        verify(merchantRepository).findById(999L);
    }
}