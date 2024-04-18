START TRANSACTION;
update message
set message_type_tmp = message_type;

alter table message
    drop column message_type;

alter table message
    change column message_type_tmp message_type varchar(255);
COMMIT;
