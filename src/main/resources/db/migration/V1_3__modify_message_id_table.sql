alter table errand_message_ids add ad_account varchar(255) null;
alter table errand_message_ids change message_ids message_id varchar(255) not null;

alter table errand_message_ids add constraint primary key (errand_id, message_id);