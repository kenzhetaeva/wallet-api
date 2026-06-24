CREATE TABLE accounts
(
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    currency VARCHAR(50) NOT NULL,
    balance NUMERIC(19,2) NOT NULL DEFAULT 0.00
);

CREATE INDEX idx_accounts_user_id ON refresh_tokens(user_id);
