ALTER TABLE users
    DROP CONSTRAINT fk_users_merchant;

ALTER TABLE users
    ADD CONSTRAINT fk_users_merchant
    FOREIGN KEY (merchant_id)
    REFERENCES merchants(id)
    ON DELETE CASCADE;


DROP INDEX idx_users_merchant_id;

DROP INDEX idx_razorpay_connections_external_account;