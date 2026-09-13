CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,

    merchant_id BIGINT NOT NULL,

    user_id BIGINT,

    event_type VARCHAR(100) NOT NULL,

    resource_type VARCHAR(50),

    resource_id BIGINT,

    description TEXT,

    metadata JSONB,

    ip_address INET,

    user_agent TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_audit_logs_merchant
        FOREIGN KEY (merchant_id)
        REFERENCES merchants(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_audit_logs_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_audit_logs_merchant_id
    ON audit_logs(merchant_id);

CREATE INDEX idx_audit_logs_user_id
    ON audit_logs(user_id);

CREATE INDEX idx_audit_logs_merchant_created_at
    ON audit_logs(merchant_id, created_at);

CREATE INDEX idx_audit_logs_event_type
    ON audit_logs(event_type);

CREATE INDEX idx_audit_logs_resource
    ON audit_logs(resource_type, resource_id);