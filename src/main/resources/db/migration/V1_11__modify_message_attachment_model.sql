start transaction;
	
-- Add new message_attachment_data-table and corresponding foreign key column in message_attachment-table
create table message_attachment_data (
    id integer not null auto_increment,
    attachmentid varchar(255),
    file longblob,
    primary key (id)
) engine=InnoDB;

alter table message_attachment
    add column message_attachment_data_id integer not null;

-- Convert content-data from base64 to binary and save it in message_attachment_data-table
insert into message_attachment_data (attachmentid, file) 
    select attachmentid, from_base64(content) from message_attachment;

-- Update corresponding row in message_attachment-table with correct id for data
update message_attachment ma, message_attachment_data mad 
   set ma.message_attachment_data_id = mad.id
 where ma.attachmentid = mad.attachmentid;

 -- Drop temporary column in message_attachment_data-table
alter table message_attachment_data
    drop column attachmentid;
 
-- After convertion, add unique indexs and constraints to tables 
alter table if exists message_attachment 
   add constraint UK_message_attachment_data_id unique (message_attachment_data_id);

alter table if exists message_attachment 
   add constraint fk_message_attachment_data_message_attachment 
   foreign key (message_attachment_data_id) 
   references message_attachment_data (id);

-- Remove content column fron message_attachment-table
alter table message_attachment
    drop column content;

commit;