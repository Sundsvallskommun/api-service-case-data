alter table if exists message
    add column if not exists html_message longtext;
