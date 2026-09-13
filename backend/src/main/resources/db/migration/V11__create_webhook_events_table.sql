
CREATE TABLE webhook_events (
    id BIGSERIAL PRIMARY KEY,

    merchant_id BIGINT NOT NULL,

    event_id VARCHAR(150) NOT NULL,

    event_type VARCHAR(100) NOT NULL,

    payload JSONB NOT NULL,

    signature VARCHAR(255),

    status VARCHAR(30) NOT NULL DEFAULT 'RECEIVED',

    attempt_count INTEGER NOT NULL DEFAULT 0,

    received_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    processed_at TIMESTAMPTZ,

    last_attempt_at TIMESTAMPTZ,

    error_message TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_webhook_events_merchant
        FOREIGN KEY (merchant_id)
        REFERENCES merchants(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_webhook_events_event_id
        UNIQUE (event_id),

    CONSTRAINT chk_webhook_events_status
        CHECK (
            status IN (
                'RECEIVED',
                'PROCESSING',
                'PROCESSED',
                'FAILED'
            )
        ),

    CONSTRAINT chk_webhook_events_attempt_count
        CHECK (attempt_count >= 0)
);

CREATE INDEX idx_webhook_events_merchant_id
    ON webhook_events(merchant_id);

CREATE INDEX idx_webhook_events_event_type
    ON webhook_events(event_type);

CREATE INDEX idx_webhook_events_merchant_status
    ON webhook_events(merchant_id, status);

CREATE INDEX idx_webhook_events_merchant_received_at
    ON webhook_events(merchant_id, received_at);