CREATE TABLE product_reviews (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating INT NOT NULL,
    content VARCHAR(1000),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_product_reviews_user_product UNIQUE (user_id, product_id),
    KEY idx_product_reviews_product_created_id (product_id, created_at, id),
    KEY idx_product_reviews_user_created_id (user_id, created_at, id),
    CONSTRAINT chk_product_reviews_rating CHECK (rating BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
