package com.paypilot.backend.domain.razorpay;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.merchant.MerchantRepository;
import com.paypilot.backend.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RazorpayOAuthStateServiceImplTest {

    private static final Long MERCHANT_ID = 42L;

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private RazorpayOAuthStateRepository stateRepository;

    private RazorpayOAuthStateServiceImpl service;

    private Merchant merchant;

    @BeforeEach
    void setUp() {
        service = new RazorpayOAuthStateServiceImpl(
                merchantRepository,
                stateRepository
        );

        merchant = new Merchant(
                "acc_oauth_test",
                "OAuth Test Merchant",
                "IN",
                "INR",
                "Asia/Kolkata"
        );
    }

    @Test
    void shouldCreateRandomStateAndPersistOnlyItsHash() throws Exception {
        when(merchantRepository.findById(MERCHANT_ID))
                .thenReturn(Optional.of(merchant));

        Instant beforeCreation = Instant.now();

        String rawState = service.createState(MERCHANT_ID);

        Instant afterCreation = Instant.now();

        assertThat(rawState)
                .matches("[A-Za-z0-9_-]{43}");

        ArgumentCaptor<RazorpayOAuthState> captor =
                ArgumentCaptor.forClass(RazorpayOAuthState.class);

        verify(stateRepository).save(captor.capture());

        RazorpayOAuthState savedState = captor.getValue();

        assertThat(savedState.getMerchant()).isSameAs(merchant);
        assertThat(savedState.getStateHash())
                .isEqualTo(sha256Hex(rawState));
        assertThat(savedState.getStateHash()).isNotEqualTo(rawState);
        assertThat(savedState.getStateHash())
                .matches("[0-9a-f]{64}");

        Duration remainingAtCreation = Duration.between(
                beforeCreation,
                savedState.getExpiresAt()
        );

        Duration remainingAfterCreation = Duration.between(
                afterCreation,
                savedState.getExpiresAt()
        );

        assertThat(remainingAtCreation)
                .isBetween(
                        Duration.ofMinutes(9),
                        Duration.ofMinutes(11)
                );

        assertThat(remainingAfterCreation).isPositive();
    }

    @Test
    void shouldGenerateDifferentStatesForSeparateRequests() {
        when(merchantRepository.findById(MERCHANT_ID))
                .thenReturn(Optional.of(merchant));

        String firstState = service.createState(MERCHANT_ID);
        String secondState = service.createState(MERCHANT_ID);

        assertThat(firstState).isNotEqualTo(secondState);
    }

    @Test
    void shouldRejectCreationForUnknownMerchant() {
        when(merchantRepository.findById(MERCHANT_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createState(MERCHANT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Merchant not found");

        verifyNoInteractions(stateRepository);
    }

    @Test
    void shouldConsumeStateUsingItsSha256Hash() throws Exception {
        String rawState = "example-raw-oauth-state";

        when(stateRepository.consumeIfValid(
                sha256Hex(rawState),
                MERCHANT_ID
        )).thenReturn(1);

        boolean consumed = service.consumeState(rawState, MERCHANT_ID);

        assertThat(consumed).isTrue();

        verify(stateRepository).consumeIfValid(
                sha256Hex(rawState),
                MERCHANT_ID
        );
    }

    @Test
    void shouldReturnFalseWhenStateCannotBeConsumed() throws Exception {
        String rawState = "expired-or-already-used-state";

        when(stateRepository.consumeIfValid(
                sha256Hex(rawState),
                MERCHANT_ID
        )).thenReturn(0);

        boolean consumed = service.consumeState(rawState, MERCHANT_ID);

        assertThat(consumed).isFalse();
    }

    @Test
    void shouldRejectBlankStateOrMissingMerchantWithoutRepositoryCall() {
        assertThat(service.consumeState(null, MERCHANT_ID)).isFalse();
        assertThat(service.consumeState(" ", MERCHANT_ID)).isFalse();
        assertThat(service.consumeState("some-state", null)).isFalse();

        verifyNoInteractions(stateRepository);
    }

    @Test
    void shouldRejectCreationWhenMerchantIdIsNull() {
        assertThatThrownBy(() -> service.createState(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Merchant ID is required");

        verifyNoInteractions(merchantRepository, stateRepository);
    }

    private String sha256Hex(String value) throws Exception {
        byte[] hash = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));

        return HexFormat.of().formatHex(hash);
    }
}
