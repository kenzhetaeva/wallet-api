CREATE TABLE transactions
(
    id BIGSERIAL PRIMARY KEY,
    from_account_id BIGINT REFERENCES accounts(id) ON DELETE CASCADE,
    to_account_id BIGINT REFERENCES accounts(id) ON DELETE CASCADE,
    amount NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(50) NOT NULL,
    type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
