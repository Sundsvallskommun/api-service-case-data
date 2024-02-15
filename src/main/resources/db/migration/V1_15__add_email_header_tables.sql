CREATE TABLE email_header (
    id BIGINT AUTO_INCREMENT,
    header enum('MESSAGE_ID', 'IN_REPLY_TO', 'REFERENCES'),
    message_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (message_id) REFERENCES message (messageID)
);

CREATE TABLE email_header_values (
    email_header_id BIGINT NOT NULL,
    value VARCHAR(2048) NOT NULL,
    value_index INT NOT NULL,
    FOREIGN KEY (email_header_id) REFERENCES email_header (id)
);

