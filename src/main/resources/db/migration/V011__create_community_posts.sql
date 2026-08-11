CREATE TABLE community_posts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    author_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content VARCHAR(5000) NOT NULL,
    theme VARCHAR(30) NOT NULL,
    anonymous BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_community_posts_created_id (created_at, id),
    INDEX idx_community_posts_theme_created_id (theme, created_at, id),
    INDEX idx_community_posts_author_id (author_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
