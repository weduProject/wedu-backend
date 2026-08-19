CREATE TABLE gallery_images (
    id BIGINT NOT NULL AUTO_INCREMENT,
    invitation_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    sort_order INT NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_gallery_images_invitation_id (invitation_id),
    CONSTRAINT fk_gallery_images_invitation
        FOREIGN KEY (invitation_id) REFERENCES invitations (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
