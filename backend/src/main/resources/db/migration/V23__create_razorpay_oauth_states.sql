CREATE TABLE razorpay_oauth_states (
    id BIGSERIAL PRIMARY KEY,

    merchant_id BIGINT NOT NULL,

    state_hash VARCHAR(64) NOT NULL,

    expires_at TIMESTAMPTZ NOT NULL,

    consumed_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_razorpay_oauth_states_merchant
        FOREIGN KEY (merchant_id)
        REFERENCES merchants(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_razorpay_oauth_states_state_hash
        UNIQUE (state_hash),

    CONSTRAINT chk_razorpay_oauth_states_state_hash
        CHECK (state_hash ~ '^[0-9a-f]{64}$'),

    CONSTRAINT chk_razorpay_oauth_states_expiry
        CHECK (expires_at > created_at),

    CONSTRAINT chk_razorpay_oauth_states_consumed_at
        CHECK (consumed_at IS NULL OR consumed_at >= created_at)
);

CREATE INDEX idx_razorpay_oauth_states_merchant
    ON razorpay_oauth_states(merchant_id);

CREATE INDEX idx_razorpay_oauth_states_expiry
    ON razorpay_oauth_states(expires_at);
