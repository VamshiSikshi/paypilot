CREATE TABLE sync_jobs (
    id BIGSERIAL PRIMARY KEY,

    merchant_id BIGINT NOT NULL,

    resource_type VARCHAR(50) NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    trigger_type VARCHAR(30) NOT NULL,

    started_at TIMESTAMPTZ,

    completed_at TIMESTAMPTZ,

    records_processed INTEGER NOT NULL DEFAULT 0,

    records_created INTEGER NOT NULL DEFAULT 0,

    records_updated INTEGER NOT NULL DEFAULT 0,

    error_message TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_sync_jobs_merchant
        FOREIGN KEY (merchant_id)
        REFERENCES merchants(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_sync_jobs_resource_type
        CHECK (
            resource_type IN (
                'PAYMENTS',
                'ORDERS',
                'REFUNDS',
                'SETTLEMENTS',
                'SETTLEMENT_RECONCILIATION'
            )
        ),

    CONSTRAINT chk_sync_jobs_status
        CHECK (
            status IN (
                'PENDING',
                'RUNNING',
                'COMPLETED',
                'FAILED'
            )
        ),

    CONSTRAINT chk_sync_jobs_trigger_type
        CHECK (
            trigger_type IN (
                'MANUAL',
                'SCHEDULED',
                'WEBHOOK',
                'SYSTEM'
            )
        ),

    CONSTRAINT chk_sync_jobs_records_processed
        CHECK (records_processed >= 0),

    CONSTRAINT chk_sync_jobs_records_created
        CHECK (records_created >= 0),

    CONSTRAINT chk_sync_jobs_records_updated
        CHECK (records_updated >= 0)
);

CREATE INDEX idx_sync_jobs_merchant_id
    ON sync_jobs(merchant_id);

CREATE INDEX idx_sync_jobs_merchant_status
    ON sync_jobs(merchant_id, status);

CREATE INDEX idx_sync_jobs_merchant_created_at
    ON sync_jobs(merchant_id, created_at);

CREATE INDEX idx_sync_jobs_resource_type
    ON sync_jobs(resource_type);

CREATE INDEX idx_sync_jobs_trigger_type
    ON sync_jobs(trigger_type);