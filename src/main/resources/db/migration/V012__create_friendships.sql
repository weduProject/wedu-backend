CREATE TABLE friendships (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    friend_user_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_friendships_user_friend UNIQUE (user_id, friend_user_id),
    KEY idx_friendships_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
