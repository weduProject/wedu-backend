CREATE TABLE calendar_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    event_date DATE NOT NULL,
    event_at DATETIME(6) NULL,
    category VARCHAR(30) NOT NULL,
    memo VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_calendar_events_user_date_at_id (user_id, event_date, event_at, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
