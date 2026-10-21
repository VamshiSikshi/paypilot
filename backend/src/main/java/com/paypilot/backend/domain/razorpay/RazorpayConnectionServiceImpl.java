package com.paypilot.backend.domain.razorpay;

import com.paypilot.backend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class RazorpayConnectionServiceImpl
        implements RazorpayConnectionService {

    private final RazorpayConnectionRepository connectionRepository;

    public RazorpayConnectionServiceImpl(
            RazorpayConnectionRepository connectionRepository
    ) {
        this.connectionRepository = connectionRepository;
    }

    @Override
    public RazorpayConnection getConnection(Long merchantId) {
        return findConnection(merchantId);
    }

    @Override
    @Transactional
    public RazorpayConnection disconnect(Long merchantId) {
        RazorpayConnection connection = findConnection(merchantId);
        connection.markDisconnected();
        return connection;
    }

    @Override
    @Transactional
    public RazorpayConnection recordSuccessfulSync(
            Long merchantId,
            Instant syncedAt
    ) {
        Objects.requireNonNull(syncedAt, "Sync timestamp is required");

        RazorpayConnection connection = findConnection(merchantId);
        connection.recordSuccessfulSync(syncedAt);
        return connection;
    }

    private RazorpayConnection findConnection(Long merchantId) {
        Objects.requireNonNull(merchantId, "Merchant ID is required");

        return connectionRepository.findByMerchantId(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Razorpay connection not found for merchant: "
                                + merchantId
                ));
    }
}
