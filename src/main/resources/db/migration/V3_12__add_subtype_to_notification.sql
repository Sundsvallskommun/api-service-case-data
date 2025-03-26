alter table if exists notification
    add column if not exists sub_type varchar(255);
