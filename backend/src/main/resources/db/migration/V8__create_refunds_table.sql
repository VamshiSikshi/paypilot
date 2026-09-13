CREATE TABLE refunds (
    id BIGSERIAL PRIMARY KEY,

    merchant_id BIGINT NOT NULL,

    payment_id BIGINT NOT NULL,

    razorpay_refund_id VARCHAR(100) NOT NULL,

    razorpay_payment_id VARCHAR(100) NOT NULL,

    amount BIGINT NOT NULL,

    currency VARCHAR(3) NOT NULL,

    status VARCHAR(30) NOT NULL,

    speed_requested VARCHAR(30),

    speed_processed VARCHAR(30),

    notes TEXT,

    failure_reason TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    last_synced_at TIMESTAMPTZ,

    CONSTRAINT fk_refunds_merchant
        FOREIGN KEY (merchant_id)
        REFERENCES merchants(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_refunds_payment
        FOREIGN KEY (payment_id)
        REFERENCES payments(id),

    CONSTRAINT uq_refunds_razorpay_refund_id
        UNIQUE (razorpay_refund_id),

    CONSTRAINT chk_refunds_amount
        CHECK (amount > 0),

    CONSTRAINT chk_refunds_status
        CHECK (
            status IN (
                'CREATED',
                'PROCESSED',
                'FAILED'
            )
        )
);

CREATE INDEX idx_refunds_merchant_id
    ON refunds(merchant_id);

CREATE INDEX idx_refunds_payment_id
    ON refunds(payment_id);

CREATE INDEX idx_refunds_merchant_created_at
    ON refunds(merchant_id, created_at);

CREATE INDEX idx_refunds_merchant_status
    ON refunds(merchant_id, status);