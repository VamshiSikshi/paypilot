ALTER TABLE action_executions
    ADD COLUMN idempotency_key VARCHAR(100);

ALTER TABLE action_executions
    ADD CONSTRAINT uq_action_executions_idempotency_key
    UNIQUE (idempotency_key);
