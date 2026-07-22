CREATE TABLE calendar_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    event_date DATE NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_calendar_events_user_date_id (user_id, event_date, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
