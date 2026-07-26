CREATE TABLE popular_products (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    price INT NOT NULL,
    source_name VARCHAR(255) NOT NULL,
    source_url VARCHAR(1000) NOT NULL,
    thumbnail_url VARCHAR(1000),
    rank_no INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_popular_products_rank (rank_no),
    CONSTRAINT chk_popular_products_price CHECK (price >= 0),
    CONSTRAINT chk_popular_products_rank_no CHECK (rank_no >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
