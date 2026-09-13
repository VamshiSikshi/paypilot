CREATE TABLE ai_insights (
    id BIGSERIAL PRIMARY KEY,

    merchant_id BIGINT NOT NULL,

    insight_type VARCHAR(50) NOT NULL,

    severity VARCHAR(20) NOT NULL,

    title VARCHAR(200) NOT NULL,

    description TEXT NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    source VARCHAR(30) NOT NULL,

    data JSONB,

    valid_from TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    valid_until TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ai_insights_merchant
        FOREIGN KEY (merchant_id)
        REFERENCES merchants(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_ai_insights_severity
        CHECK (
            severity IN (
                'INFO',
                'LOW',
                'MEDIUM',
                'HIGH',
                'CRITICAL'
            )
        ),

    CONSTRAINT chk_ai_insights_status
        CHECK (
            status IN (
                'ACTIVE',
                'DISMISSED',
                'RESOLVED',
                'EXPIRED'
            )
        ),

    CONSTRAINT chk_ai_insights_source
        CHECK (
            source IN (
                'AI',
                'RULE',
                'ANALYTICS'
            )
        ),

    CONSTRAINT chk_ai_insights_validity
        CHECK (
            valid_until IS NULL
            OR valid_until >= valid_from
        )
);

CREATE INDEX idx_ai_insights_merchant_id
    ON ai_insights(merchant_id);

CREATE INDEX idx_ai_insights_merchant_status
    ON ai_insights(merchant_id, status);

CREATE INDEX idx_ai_insights_merchant_severity
    ON ai_insights(merchant_id, severity);

CREATE INDEX idx_ai_insights_merchant_valid_from
    ON ai_insights(merchant_id, valid_from);

CREATE INDEX idx_ai_insights_type
    ON ai_insights(insight_type);