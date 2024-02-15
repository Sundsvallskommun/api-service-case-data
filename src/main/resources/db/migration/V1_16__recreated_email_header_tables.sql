DROP TABLE email_header_values;
DROP TABLE email_header;


create table email_header
(
    id         bigint not null auto_increment,
    message_id varchar(255),
    header     enum ('IN_REPLY_TO','REFERENCES','MESSAGE_ID'),
    primary key (id)
) engine=InnoDB;

create table email_header_values
(
    value_index     integer not null,
    email_header_id bigint  not null,
    value           varchar(255),
    primary key (value_index, email_header_id)
) engine=InnoDB;

alter table if exists email_header
    add constraint fk_message_header_message_id
    foreign key (message_id)
    references message (messageid);

alter table if exists email_header_values
    add constraint fk_email_header_values_email_header_id
    foreign key (email_header_id)
    references email_header (id);

alter table if exists message
    modify message longtext null;