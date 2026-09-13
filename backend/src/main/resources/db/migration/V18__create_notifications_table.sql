CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,

    merchant_id BIGINT NOT NULL,

    user_id BIGINT NOT NULL,

    notification_type VARCHAR(50) NOT NULL,

    severity VARCHAR(20) NOT NULL,

    title VARCHAR(200) NOT NULL,

    message TEXT NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'UNREAD',

    resource_type VARCHAR(30),

    resource_id BIGINT,

    metadata JSONB,

    read_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notifications_merchant
        FOREIGN KEY (merchant_id)
        REFERENCES merchants(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_notifications_type
        CHECK (
            notification_type IN (
                'PAYMENT_FAILED',
                'PAYMENT_SUCCESS',
                'REFUND_COMPLETED',
                'REFUND_FAILED',
                'SETTLEMENT_PROCESSED',
                'SETTLEMENT_FAILED',
                'SYNC_COMPLETED',
                'SYNC_FAILED',
                'AI_INSIGHT',
                'SYSTEM'
            )
        ),

    CONSTRAINT chk_notifications_severity
        CHECK (
            severity IN (
                'INFO',
                'LOW',
                'MEDIUM',
                'HIGH',
                'CRITICAL'
            )
        ),

    CONSTRAINT chk_notifications_status
        CHECK (
            status IN (
                'UNREAD',
                'READ',
                'ARCHIVED'
            )
        ),

    CONSTRAINT chk_notifications_resource_type
        CHECK (
            resource_type IS NULL
            OR resource_type IN (
                'PAYMENT',
                'REFUND',
                'SETTLEMENT',
                'ORDER',
                'SYNC_JOB',
                'AI_INSIGHT',
                'ACTION',
                'SYSTEM'
            )
        ),

    CONSTRAINT chk_notifications_resource_id
        CHECK (
            resource_id IS NULL
            OR resource_id > 0
        ),

    CONSTRAINT chk_notifications_read_at
        CHECK (
            status = 'UNREAD'
            OR read_at IS NOT NULL
        )
);

CREATE INDEX idx_notifications_merchant_id
    ON notifications(merchant_id);

CREATE INDEX idx_notifications_user_id
    ON notifications(user_id);

CREATE INDEX idx_notifications_merchant_status
    ON notifications(merchant_id, status);

CREATE INDEX idx_notifications_user_created_at
    ON notifications(user_id, created_at);

CREATE INDEX idx_notifications_merchant_severity
    ON notifications(merchant_id, severity);

CREATE INDEX idx_notifications_resource
    ON notifications(resource_type, resource_id);