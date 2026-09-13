CREATE TABLE razorpay_connections (
    id BIGSERIAL PRIMARY KEY,

    merchant_id BIGINT NOT NULL,

    external_account_id VARCHAR(100) NOT NULL,

    status VARCHAR(30) NOT NULL,

    access_token_reference VARCHAR(255),

    refresh_token_reference VARCHAR(255),

    token_expires_at TIMESTAMPTZ,

    granted_scopes TEXT,

    last_connected_at TIMESTAMPTZ,

    last_synced_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_razorpay_connections_merchant
        FOREIGN KEY (merchant_id)
        REFERENCES merchants(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_razorpay_connections_merchant
        UNIQUE (merchant_id),

    CONSTRAINT uq_razorpay_connections_external_account
        UNIQUE (external_account_id),

    CONSTRAINT chk_razorpay_connections_status
        CHECK (
            status IN (
                'CONNECTED',
                'DISCONNECTED',
                'EXPIRED',
                'ERROR'
            )
        )
);

CREATE INDEX idx_razorpay_connections_external_account
    ON razorpay_connections(external_account_id);