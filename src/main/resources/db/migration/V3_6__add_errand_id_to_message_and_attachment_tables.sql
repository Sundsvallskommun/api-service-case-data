alter table if exists message add column if not exists errand_id bigint;
create index if not exists idx_messsage_errand_id on message (errand_id);

update attachment a set a.errand_id = (select e.id from errand e where e.errand_number = a.errand_number) where a.errand_id is null;
update message m set m.errand_id = (select e.id from errand e where e.errand_number = m.errand_number);
