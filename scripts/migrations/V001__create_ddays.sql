CREATE TABLE ddays (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    wedding_date DATE NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_ddays_user_id UNIQUE (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
