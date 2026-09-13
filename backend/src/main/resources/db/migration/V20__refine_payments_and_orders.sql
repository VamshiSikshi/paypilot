ALTER TABLE payments
    ADD COLUMN fee BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN tax BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN amount_refunded BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN refund_status VARCHAR(30),
    ADD COLUMN captured BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN international BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN description TEXT,
    ADD COLUMN notes JSONB;

ALTER TABLE payments
    ADD CONSTRAINT chk_payments_fee
    CHECK (fee >= 0);

ALTER TABLE payments
    ADD CONSTRAINT chk_payments_tax
    CHECK (tax >= 0);

ALTER TABLE payments
    ADD CONSTRAINT chk_payments_amount_refunded
    CHECK (amount_refunded >= 0);

ALTER TABLE payments
    ADD CONSTRAINT chk_payments_refund_status
    CHECK (
        refund_status IS NULL
        OR refund_status IN (
            'NOT_REFUNDED',
            'PARTIALLY_REFUNDED',
            'FULLY_REFUNDED'
        )
    );


ALTER TABLE orders
    ADD COLUMN amount_paid BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN amount_due BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN attempts INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN notes JSONB;

ALTER TABLE orders
    ADD CONSTRAINT chk_orders_amount_paid
    CHECK (amount_paid >= 0);

ALTER TABLE orders
    ADD CONSTRAINT chk_orders_amount_due
    CHECK (amount_due >= 0);

ALTER TABLE orders
    ADD CONSTRAINT chk_orders_attempts
    CHECK (attempts >= 0);