ALTER TABLE payments
    ADD COLUMN order_id BIGINT;

ALTER TABLE payments
    ADD CONSTRAINT fk_payments_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE SET NULL;

CREATE INDEX idx_payments_order_id
    ON payments(order_id);