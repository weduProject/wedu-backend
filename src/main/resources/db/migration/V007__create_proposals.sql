CREATE TABLE proposals (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_proposals_user_id UNIQUE (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE proposal_items (
    proposal_id BIGINT NOT NULL,
    category VARCHAR(20) NOT NULL,
    product_id BIGINT NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    item_price BIGINT NOT NULL,
    PRIMARY KEY (proposal_id, category),
    CONSTRAINT fk_proposal_items_proposal FOREIGN KEY (proposal_id) REFERENCES proposals (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
