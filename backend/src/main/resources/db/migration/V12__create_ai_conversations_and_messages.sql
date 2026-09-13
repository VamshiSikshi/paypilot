CREATE TABLE ai_conversations (
    id BIGSERIAL PRIMARY KEY,

    merchant_id BIGINT NOT NULL,

    user_id BIGINT NOT NULL,

    title VARCHAR(200),

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    last_message_at TIMESTAMPTZ,

    CONSTRAINT fk_ai_conversations_merchant
        FOREIGN KEY (merchant_id)
        REFERENCES merchants(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_ai_conversations_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_ai_conversations_status
        CHECK (
            status IN (
                'ACTIVE',
                'ARCHIVED'
            )
        )
);

CREATE INDEX idx_ai_conversations_merchant_id
    ON ai_conversations(merchant_id);

CREATE INDEX idx_ai_conversations_user_id
    ON ai_conversations(user_id);

CREATE INDEX idx_ai_conversations_merchant_updated_at
    ON ai_conversations(merchant_id, updated_at);


CREATE TABLE ai_messages (
    id BIGSERIAL PRIMARY KEY,

    conversation_id BIGINT NOT NULL,

    merchant_id BIGINT NOT NULL,

    role VARCHAR(30) NOT NULL,

    content TEXT NOT NULL,

    message_type VARCHAR(30) NOT NULL DEFAULT 'TEXT',

    metadata JSONB,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ai_messages_conversation
        FOREIGN KEY (conversation_id)
        REFERENCES ai_conversations(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_ai_messages_merchant
        FOREIGN KEY (merchant_id)
        REFERENCES merchants(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_ai_messages_role
        CHECK (
            role IN (
                'USER',
                'ASSISTANT',
                'SYSTEM',
                'TOOL'
            )
        ),

    CONSTRAINT chk_ai_messages_message_type
        CHECK (
            message_type IN (
                'TEXT',
                'TOOL_CALL',
                'TOOL_RESULT',
                'ERROR'
            )
        )
);

CREATE INDEX idx_ai_messages_conversation_id
    ON ai_messages(conversation_id);

CREATE INDEX idx_ai_messages_merchant_id
    ON ai_messages(merchant_id);

CREATE INDEX idx_ai_messages_conversation_created_at
    ON ai_messages(conversation_id, created_at);