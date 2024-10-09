alter table if exists errand
    add column suspended_to datetime(6);

alter table if exists errand
    add column suspended_from datetime(6);
