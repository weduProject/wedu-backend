ALTER TABLE users
    MODIFY social_id VARCHAR(255) NOT NULL,
    ADD COLUMN password_hash VARCHAR(255) NULL,
    ADD CONSTRAINT uk_users_email UNIQUE (email);
