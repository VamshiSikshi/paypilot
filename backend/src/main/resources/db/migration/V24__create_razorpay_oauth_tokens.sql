CREATE TABLE razorpay_oauth_tokens (
    id BIGSERIAL PRIMARY KEY,

    merchant_id BIGINT NOT NULL,

    access_token_ciphertext TEXT NOT NULL,

    refresh_token_ciphertext TEXT,

    encryption_key_version VARCHAR(50) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_razorpay_oauth_tokens_merchant
        FOREIGN KEY (merchant_id)
        REFERENCES merchants(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_razorpay_oauth_tokens_merchant
        UNIQUE (merchant_id)
);

CREATE INDEX idx_razorpay_oauth_tokens_updated_at
    ON razorpay_oauth_tokens(updated_at);