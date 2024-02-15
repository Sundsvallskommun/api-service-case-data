create table message
(
    id                bigint       not null
        primary key,
    email             varchar(255) null,
    external_case_id  varchar(255) null,
    family_id         varchar(255) null,
    first_name        varchar(255) null,
    last_name         varchar(255) null,
    message           varchar(255) null,
    message_id        varchar(255) null,
    posted_by_manager bit          null,
    sent              varchar(255) null,
    user_id           varchar(255) null,
    username          varchar(255) null
);