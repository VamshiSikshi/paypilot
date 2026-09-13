CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,

    merchant_id BIGINT NOT NULL,

    razorpay_order_id VARCHAR(100) NOT NULL,

    amount BIGINT NOT NULL,

    currency VARCHAR(3) NOT NULL,

    status VARCHAR(30) NOT NULL,

    receipt VARCHAR(100),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    last_synced_at TIMESTAMPTZ,

    CONSTRAINT fk_orders_merchant
        FOREIGN KEY (merchant_id)
        REFERENCES merchants(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_orders_razorpay_order_id
        UNIQUE (razorpay_order_id),

    CONSTRAINT chk_orders_amount
        CHECK (amount > 0),

    CONSTRAINT chk_orders_status
        CHECK (
            status IN (
                'CREATED',
                'ATTEMPTED',
                'PAID'
            )
        )
);

CREATE INDEX idx_orders_merchant_id
    ON orders(merchant_id);

CREATE INDEX idx_orders_merchant_created_at
    ON orders(merchant_id, created_at);

CREATE INDEX idx_orders_merchant_status
    ON orders(merchant_id, status);