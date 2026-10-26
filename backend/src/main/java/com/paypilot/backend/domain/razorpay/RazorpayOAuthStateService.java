package com.paypilot.backend.domain.razorpay;

import java.util.Optional;

public interface RazorpayOAuthStateService {

    /**
     * Generates and persists an OAuth state for the specified merchant.
     * Returns the raw state for the authorization URL.
     */
    String createState(Long merchantId);

    /**
     * Atomically consumes a state if it is valid, unexpired,
     * unused, and belongs to the specified merchant.
     *
     * @return true if consumed successfully; false otherwise
     */
    boolean consumeState(String rawState, Long merchantId);

    /**
     * Resolves the merchant from a stored state hash and consumes
     * that state exactly once.
     *
     * @return the merchant ID if the state is valid and consumed;
     *         otherwise Optional.empty()
     */
    Optional<Long> consumeStateAndGetMerchantId(String rawState);
}