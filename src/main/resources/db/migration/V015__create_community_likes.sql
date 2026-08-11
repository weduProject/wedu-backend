CREATE TABLE community_post_likes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_community_post_likes_post_user (post_id, user_id),
    INDEX idx_community_post_likes_user_id (user_id, id),
    CONSTRAINT fk_community_post_likes_post
        FOREIGN KEY (post_id) REFERENCES community_posts (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE community_comment_likes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    comment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_community_comment_likes_comment_user (comment_id, user_id),
    INDEX idx_community_comment_likes_user_id (user_id, id),
    CONSTRAINT fk_community_comment_likes_comment
        FOREIGN KEY (comment_id) REFERENCES community_comments (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
