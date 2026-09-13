CREATE TABLE action_executions (
    id BIGSERIAL PRIMARY KEY,

    merchant_id BIGINT NOT NULL,

    user_id BIGINT NOT NULL,

    conversation_id BIGINT,

    message_id BIGINT,

    action_type VARCHAR(50) NOT NULL,

    target_type VARCHAR(30) NOT NULL,

    target_id BIGINT NOT NULL,

    status VARCHAR(40) NOT NULL DEFAULT 'PENDING',

    confirmation_required BOOLEAN NOT NULL DEFAULT TRUE,

    confirmed_at TIMESTAMPTZ,

    requested_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    started_at TIMESTAMPTZ,

    completed_at TIMESTAMPTZ,

    request_data JSONB,

    result_data JSONB,

    error_message TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_action_executions_merchant
        FOREIGN KEY (merchant_id)
        REFERENCES merchants(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_action_executions_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_action_executions_conversation
        FOREIGN KEY (conversation_id)
        REFERENCES ai_conversations(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_action_executions_message
        FOREIGN KEY (message_id)
        REFERENCES ai_messages(id)
        ON DELETE SET NULL,

    CONSTRAINT chk_action_executions_action_type
        CHECK (
            action_type IN (
                'REFUND',
                'SYNC'
            )
        ),

    CONSTRAINT chk_action_executions_target_type
        CHECK (
            target_type IN (
                'PAYMENT',
                'REFUND',
                'SETTLEMENT',
                'ORDER'
            )
        ),

    CONSTRAINT chk_action_executions_status
        CHECK (
            status IN (
                'PENDING',
                'CONFIRMATION_REQUIRED',
                'CONFIRMED',
                'EXECUTING',
                'COMPLETED',
                'FAILED',
                'CANCELLED'
            )
        ),

    CONSTRAINT chk_action_executions_target_id
        CHECK (target_id > 0),

    CONSTRAINT chk_action_executions_confirmation
        CHECK (
            confirmation_required = FALSE
            OR confirmed_at IS NOT NULL
            OR status IN ('PENDING', 'CONFIRMATION_REQUIRED', 'CANCELLED')
        )
);

CREATE INDEX idx_action_executions_merchant_id
    ON action_executions(merchant_id);

CREATE INDEX idx_action_executions_user_id
    ON action_executions(user_id);

CREATE INDEX idx_action_executions_conversation_id
    ON action_executions(conversation_id);

CREATE INDEX idx_action_executions_message_id
    ON action_executions(message_id);

CREATE INDEX idx_action_executions_merchant_status
    ON action_executions(merchant_id, status);

CREATE INDEX idx_action_executions_merchant_created_at
    ON action_executions(merchant_id, created_at);

CREATE INDEX idx_action_executions_target
    ON action_executions(target_type, target_id);