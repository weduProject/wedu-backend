CREATE TABLE checklist_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    category VARCHAR(20) NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_checklist_items_user_id (user_id, id),
    INDEX idx_checklist_items_user_category_id (user_id, category, id),
    INDEX idx_checklist_items_user_completed (user_id, completed)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
