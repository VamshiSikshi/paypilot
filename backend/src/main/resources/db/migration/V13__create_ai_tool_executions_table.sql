CREATE TABLE ai_tool_executions (
    id BIGSERIAL PRIMARY KEY,

    merchant_id BIGINT NOT NULL,

    conversation_id BIGINT NOT NULL,

    message_id BIGINT NOT NULL,

    tool_name VARCHAR(150) NOT NULL,

    tool_call_id VARCHAR(150),

    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    input JSONB,

    output JSONB,

    error_message TEXT,

    started_at TIMESTAMPTZ,

    completed_at TIMESTAMPTZ,

    duration_ms BIGINT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ai_tool_executions_merchant
        FOREIGN KEY (merchant_id)
        REFERENCES merchants(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_ai_tool_executions_conversation
        FOREIGN KEY (conversation_id)
        REFERENCES ai_conversations(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_ai_tool_executions_message
        FOREIGN KEY (message_id)
        REFERENCES ai_messages(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_ai_tool_executions_tool_call_id
        UNIQUE (tool_call_id),

    CONSTRAINT chk_ai_tool_executions_status
        CHECK (
            status IN (
                'PENDING',
                'RUNNING',
                'COMPLETED',
                'FAILED'
            )
        ),

    CONSTRAINT chk_ai_tool_executions_duration
        CHECK (
            duration_ms IS NULL OR duration_ms >= 0
        )
);

CREATE INDEX idx_ai_tool_executions_merchant_id
    ON ai_tool_executions(merchant_id);

CREATE INDEX idx_ai_tool_executions_conversation_id
    ON ai_tool_executions(conversation_id);

CREATE INDEX idx_ai_tool_executions_message_id
    ON ai_tool_executions(message_id);

CREATE INDEX idx_ai_tool_executions_merchant_created_at
    ON ai_tool_executions(merchant_id, created_at);

CREATE INDEX idx_ai_tool_executions_tool_name
    ON ai_tool_executions(tool_name);

CREATE INDEX idx_ai_tool_executions_status
    ON ai_tool_executions(status);