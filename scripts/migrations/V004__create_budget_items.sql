CREATE TABLE budget_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    category VARCHAR(30) NOT NULL,
    planned_amount DECIMAL(18, 0) NOT NULL,
    spent_amount DECIMAL(18, 0) NOT NULL DEFAULT 0,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_budget_items_user_id (user_id, id),
    INDEX idx_budget_items_user_category_id (user_id, category, id),
    INDEX idx_budget_items_user_completed (user_id, completed)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
