CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,

    merchant_id BIGINT NOT NULL,

    email VARCHAR(255) NOT NULL,

    display_name VARCHAR(150) NOT NULL,

    role VARCHAR(30) NOT NULL,

    status VARCHAR(30) NOT NULL,

    last_login_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_users_merchant
        FOREIGN KEY (merchant_id)
        REFERENCES merchants(id),

    CONSTRAINT uq_users_merchant_email
        UNIQUE (merchant_id, email),

    CONSTRAINT chk_users_role
        CHECK (role IN ('OWNER', 'FINANCE', 'VIEWER')),

    CONSTRAINT chk_users_status
        CHECK (status IN ('INVITED', 'ACTIVE', 'SUSPENDED', 'DEACTIVATED'))
);

CREATE INDEX idx_users_merchant_id
    ON users(merchant_id);


CREATE TABLE user_auth_accounts (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,

    provider VARCHAR(30) NOT NULL,

    provider_user_id VARCHAR(255),

    password_hash VARCHAR(255),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_auth_accounts_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_auth_accounts_user_provider
        UNIQUE (user_id, provider),

    CONSTRAINT chk_auth_accounts_provider
        CHECK (provider IN ('LOCAL', 'GOOGLE')),

    CONSTRAINT chk_auth_accounts_credentials
        CHECK (
            (provider = 'LOCAL'
                AND password_hash IS NOT NULL
                AND provider_user_id IS NULL)
            OR
            (provider = 'GOOGLE'
                AND provider_user_id IS NOT NULL
                AND password_hash IS NULL)
        )
);

CREATE INDEX idx_auth_accounts_provider_lookup
    ON user_auth_accounts(provider, provider_user_id);