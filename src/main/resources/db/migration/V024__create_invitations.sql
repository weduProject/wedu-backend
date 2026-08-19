CREATE TABLE invitations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    template_id VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,

    groom_name VARCHAR(255),
    bride_name VARCHAR(255),
    groom_photo TEXT,
    bride_photo TEXT,
    groom_contact VARCHAR(255),
    bride_contact VARCHAR(255),
    groom_parents VARCHAR(255),
    bride_parents VARCHAR(255),

    wedding_date DATE,
    wedding_time VARCHAR(255),
    venue_name VARCHAR(255),
    venue_address VARCHAR(500),
    venue_detail VARCHAR(255),
    latitude DOUBLE,
    longitude DOUBLE,

    main_greeting TEXT,
    invitation_message TEXT,
    additional_message TEXT,

    groom_bank VARCHAR(255),
    groom_account VARCHAR(255),
    groom_account_holder VARCHAR(255),
    bride_bank VARCHAR(255),
    bride_account VARCHAR(255),
    bride_account_holder VARCHAR(255),

    groom_parent_contact VARCHAR(255),
    bride_parent_contact VARCHAR(255),

    transport_guide TEXT,
    parking_guide TEXT,
    public_transport_guide TEXT,

    main_color VARCHAR(255),
    font_family VARCHAR(255),
    bgm_url TEXT,
    design_settings TEXT,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_invitations_user_id UNIQUE (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
