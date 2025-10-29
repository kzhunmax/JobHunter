ALTER TABLE users
    ADD reset_password_token VARCHAR(255);

ALTER TABLE users
    ADD reset_password_token_expiry TIMESTAMP WITHOUT TIME ZONE;

CREATE INDEX IF NOT EXISTS idx_users_reset_token ON users (reset_password_token);