alter table if exists errand
    drop constraint if exists UK_errand_errand_number;


alter table if exists errand
    add constraint UK_errand_errand_number_municipality_id unique (errand_number, municipality_id);
