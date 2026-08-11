CREATE TABLE share_links (
    id BIGINT NOT NULL AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    token VARCHAR(36) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_share_links_owner_id UNIQUE (owner_id),
    CONSTRAINT uk_share_links_token UNIQUE (token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
