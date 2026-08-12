CREATE TABLE invitations (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT,

                             user_id BIGINT NOT NULL UNIQUE,
                             template_id VARCHAR(100) NOT NULL,
                             title VARCHAR(255) NOT NULL,

                             status VARCHAR(20) NOT NULL,

    -- 신랑·신부 정보
                             groom_name VARCHAR(100),
                             bride_name VARCHAR(100),
                             groom_contact VARCHAR(50),
                             bride_contact VARCHAR(50),
                             groom_parents VARCHAR(100),
                             bride_parents VARCHAR(100),

    -- 예식 정보
                             wedding_date DATE,
                             wedding_time VARCHAR(50),
                             venue_name VARCHAR(255),
                             venue_address VARCHAR(500),
                             venue_detail VARCHAR(255),

    -- 청첩장 문구
                             main_greeting TEXT,
                             invitation_message TEXT,
                             additional_message TEXT,

    -- 디자인 설정
                             main_color VARCHAR(50),
                             font_family VARCHAR(100),

                             created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                 ON UPDATE CURRENT_TIMESTAMP
);