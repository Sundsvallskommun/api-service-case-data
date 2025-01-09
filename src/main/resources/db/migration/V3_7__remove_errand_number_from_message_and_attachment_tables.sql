alter table if exists message
    drop column if exists errand_number;

alter table if exists attachment
    drop column if exists errand_number;

drop index if exists attachment_errand_number_idx on attachment;
