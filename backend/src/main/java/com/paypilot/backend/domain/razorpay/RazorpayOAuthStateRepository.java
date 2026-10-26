package com.paypilot.backend.domain.razorpay;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RazorpayOAuthStateRepository
        extends JpaRepository<RazorpayOAuthState, Long> {

    @Query(value = """
            SELECT merchant_id
            FROM razorpay_oauth_states
            WHERE state_hash = :stateHash
            """, nativeQuery = true)
    Optional<Long> findMerchantIdByStateHash(
            @Param("stateHash") String stateHash
    );

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            UPDATE razorpay_oauth_states
            SET consumed_at = CURRENT_TIMESTAMP
            WHERE state_hash = :stateHash
              AND merchant_id = :merchantId
              AND consumed_at IS NULL
              AND expires_at > CURRENT_TIMESTAMP
            """, nativeQuery = true)
    int consumeIfValid(
            @Param("stateHash") String stateHash,
            @Param("merchantId") Long merchantId
    );
}