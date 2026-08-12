ALTER TABLE invitations

    ADD COLUMN groom_photo TEXT,
ADD COLUMN bride_photo TEXT,

ADD COLUMN latitude DOUBLE,
ADD COLUMN longitude DOUBLE,

ADD COLUMN groom_bank VARCHAR(100),
ADD COLUMN groom_account VARCHAR(100),
ADD COLUMN groom_account_holder VARCHAR(100),

ADD COLUMN bride_bank VARCHAR(100),
ADD COLUMN bride_account VARCHAR(100),
ADD COLUMN bride_account_holder VARCHAR(100),

ADD COLUMN groom_parent_contact VARCHAR(100),
ADD COLUMN bride_parent_contact VARCHAR(100),

ADD COLUMN transport_guide TEXT,
ADD COLUMN parking_guide TEXT,
ADD COLUMN public_transport_guide TEXT,

ADD COLUMN bgm_url TEXT;