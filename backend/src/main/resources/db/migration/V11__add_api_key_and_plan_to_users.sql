ALTER TABLE users
    ADD api_key VARCHAR(255);

ALTER TABLE users
    ADD pricing_plan VARCHAR(255) NOT NULL DEFAULT 'FREE';

CREATE UNIQUE INDEX IF NOT EXISTS idx_users_api_key ON users (api_key);