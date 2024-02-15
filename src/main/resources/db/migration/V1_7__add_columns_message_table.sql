ALTER TABLE message
    ADD COLUMN message_type ENUM('SMS', 'EMAIL');

ALTER TABLE message
    ADD COLUMN mobile_number VARCHAR(255);
