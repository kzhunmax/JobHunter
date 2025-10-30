ALTER TABLE users
    ADD email_verified BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE users
    ADD email_verify_token VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_users_verify_token ON users (email_verify_token);

