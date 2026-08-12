CREATE TABLE budgets (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    total_budget DECIMAL(18, 0) NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_budgets_user_id UNIQUE (user_id),
    CONSTRAINT chk_budgets_total_budget CHECK (total_budget >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
