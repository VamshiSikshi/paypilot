CREATE TABLE settlements (
    id BIGSERIAL PRIMARY KEY,

    merchant_id BIGINT NOT NULL,

    razorpay_settlement_id VARCHAR(100) NOT NULL,

    amount BIGINT NOT NULL,

    currency VARCHAR(3) NOT NULL,

    status VARCHAR(30) NOT NULL,

    settled_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    last_synced_at TIMESTAMPTZ,

    CONSTRAINT fk_settlements_merchant
        FOREIGN KEY (merchant_id)
        REFERENCES merchants(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_settlements_razorpay_settlement_id
        UNIQUE (razorpay_settlement_id),

    CONSTRAINT chk_settlements_amount
        CHECK (amount > 0),

    CONSTRAINT chk_settlements_status
        CHECK (
            status IN (
                'CREATED',
                'PROCESSED',
                'FAILED'
            )
        )
);

CREATE INDEX idx_settlements_merchant_id
    ON settlements(merchant_id);

CREATE INDEX idx_settlements_merchant_created_at
    ON settlements(merchant_id, created_at);

CREATE INDEX idx_settlements_merchant_status
    ON settlements(merchant_id, status);