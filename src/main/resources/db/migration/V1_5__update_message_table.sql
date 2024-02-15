drop table message;

CREATE TABLE message
(
    messageid         VARCHAR(255) PRIMARY KEY,
    errand_number     VARCHAR(255),
    direction         VARCHAR(255),
    familyid          VARCHAR(255),
    external_caseid   VARCHAR(255),
    subject           VARCHAR(255),
    message           VARCHAR(255),
    username          VARCHAR(255),
    first_name        VARCHAR(255),
    last_name         VARCHAR(255),
    email             VARCHAR(255),
    userid            VARCHAR(255),
    sent              VARCHAR(255)
);

CREATE TABLE message_attachment
(
    attachmentid VARCHAR(255) PRIMARY KEY,
    messageid    VARCHAR(255),
    content      longtext,
    name         VARCHAR(255),
    content_type VARCHAR(255)
);