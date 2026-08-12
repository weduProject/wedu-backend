CREATE TABLE gallery_images (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,

                                invitation_id BIGINT NOT NULL,

                                image_url TEXT NOT NULL,

                                sort_order INT NOT NULL,

                                CONSTRAINT fk_gallery_images_invitation
                                    FOREIGN KEY (invitation_id)
                                        REFERENCES invitations(id)
                                        ON DELETE CASCADE
);