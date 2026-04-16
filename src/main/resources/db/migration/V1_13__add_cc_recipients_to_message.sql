    create table message_cc_recipients (
        message_id varchar(255) not null,
        recipient_email varchar(255),
        constraint fk_message_cc_recipients_message_id foreign key (message_id) references message (messageID)
    );
