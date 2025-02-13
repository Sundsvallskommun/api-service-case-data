alter table if exists errand
    add column if not exists status varchar(255);

alter table if exists errand
    add column if not exists status_created datetime(6);

alter table if exists errand
    add column if not exists status_description varchar(255);
