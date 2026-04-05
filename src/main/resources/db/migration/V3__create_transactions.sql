CREATE TABLE IF NOT EXISTS transactions (
    id          BIGSERIAL PRIMARY KEY,
    amount      NUMERIC(15,2)   NOT NULL CHECK (amount > 0),
    type        VARCHAR(20)     NOT NULL CHECK (type IN ('INCOME','EXPENSE','INVESTMENT')),
    category_id BIGINT          REFERENCES categories(id) ON DELETE SET NULL,
    date        DATE            NOT NULL,
    notes       VARCHAR(500),
    created_by  BIGINT          NOT NULL REFERENCES users(id),
    deleted     BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP
);

CREATE INDEX idx_transactions_type        ON transactions(type);
CREATE INDEX idx_transactions_date        ON transactions(date DESC);
CREATE INDEX idx_transactions_category    ON transactions(category_id);
CREATE INDEX idx_transactions_created_by  ON transactions(created_by);
CREATE INDEX idx_transactions_deleted     ON transactions(deleted);
