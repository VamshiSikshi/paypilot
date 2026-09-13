CREATE TABLE settlement_reconciliation_entries (
    id BIGSERIAL PRIMARY KEY,

    merchant_id BIGINT NOT NULL,

    settlement_id BIGINT NOT NULL,

    razorpay_entity_id VARCHAR(100) NOT NULL,

    transaction_type VARCHAR(30) NOT NULL,

    debit BIGINT NOT NULL DEFAULT 0,

    credit BIGINT NOT NULL DEFAULT 0,

    amount BIGINT NOT NULL,

    currency VARCHAR(3) NOT NULL,

    fee BIGINT NOT NULL DEFAULT 0,

    tax BIGINT NOT NULL DEFAULT 0,

    on_hold BOOLEAN NOT NULL DEFAULT FALSE,

    settled BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    settled_at TIMESTAMPTZ,

    description TEXT,

    notes JSONB,

    razorpay_payment_id VARCHAR(100),

    razorpay_order_id VARCHAR(100),

    order_receipt VARCHAR(100),

    method VARCHAR(30),

    card_network VARCHAR(50),

    card_issuer VARCHAR(100),

    card_type VARCHAR(30),

    dispute_id VARCHAR(100),

    settlement_utr VARCHAR(100),

    last_synced_at TIMESTAMPTZ,

    CONSTRAINT fk_recon_entries_merchant
        FOREIGN KEY (merchant_id)
        REFERENCES merchants(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_recon_entries_settlement
        FOREIGN KEY (settlement_id)
        REFERENCES settlements(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_recon_entries_entity
        UNIQUE (
            settlement_id,
            razorpay_entity_id,
            transaction_type
        ),

    CONSTRAINT chk_recon_entries_transaction_type
        CHECK (
            transaction_type IN (
                'PAYMENT',
                'REFUND',
                'TRANSFER',
                'ADJUSTMENT'
            )
        ),

    CONSTRAINT chk_recon_entries_amount
        CHECK (amount > 0),

    CONSTRAINT chk_recon_entries_debit
        CHECK (debit >= 0),

    CONSTRAINT chk_recon_entries_credit
        CHECK (credit >= 0),

    CONSTRAINT chk_recon_entries_fee
        CHECK (fee >= 0),

    CONSTRAINT chk_recon_entries_tax
        CHECK (tax >= 0)
);

CREATE INDEX idx_recon_entries_merchant_id
    ON settlement_reconciliation_entries(merchant_id);

CREATE INDEX idx_recon_entries_settlement_id
    ON settlement_reconciliation_entries(settlement_id);

CREATE INDEX idx_recon_entries_merchant_created_at
    ON settlement_reconciliation_entries(merchant_id, created_at);

CREATE INDEX idx_recon_entries_merchant_transaction_type
    ON settlement_reconciliation_entries(merchant_id, transaction_type);

CREATE INDEX idx_recon_entries_payment_id
    ON settlement_reconciliation_entries(razorpay_payment_id);

CREATE INDEX idx_recon_entries_order_id
    ON settlement_reconciliation_entries(razorpay_order_id);