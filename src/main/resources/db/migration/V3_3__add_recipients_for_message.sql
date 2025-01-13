create table message_recipients
(
    message_id      varchar(255) not null,
    recipient_email varchar(255)
) engine=InnoDB;

alter table if exists message_recipients
add constraint fk_message_recipients_message_id
       foreign key (message_id)
       references message (messageid);
