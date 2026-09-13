CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,

    merchant_id BIGINT NOT NULL,

    razorpay_payment_id VARCHAR(100) NOT NULL,

    razorpay_order_id VARCHAR(100),

    amount BIGINT NOT NULL,

    currency VARCHAR(3) NOT NULL,

    status VARCHAR(30) NOT NULL,

    method VARCHAR(30),

    captured_at TIMESTAMPTZ,

    customer_email VARCHAR(255),

    customer_contact VARCHAR(30),

    error_code VARCHAR(100),

    error_description TEXT,

    error_source VARCHAR(100),

    error_reason VARCHAR(100),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    last_synced_at TIMESTAMPTZ,

    CONSTRAINT fk_payments_merchant
        FOREIGN KEY (merchant_id)
        REFERENCES merchants(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_payments_razorpay_payment_id
        UNIQUE (razorpay_payment_id),

    CONSTRAINT chk_payments_amount
        CHECK (amount > 0),

    CONSTRAINT chk_payments_status
        CHECK (
            status IN (
                'CREATED',
                'AUTHORIZED',
                'CAPTURED',
                'FAILED'
            )
        )
);

CREATE INDEX idx_payments_merchant_id
    ON payments(merchant_id);

CREATE INDEX idx_payments_merchant_created_at
    ON payments(merchant_id, created_at);

CREATE INDEX idx_payments_merchant_status
    ON payments(merchant_id, status);