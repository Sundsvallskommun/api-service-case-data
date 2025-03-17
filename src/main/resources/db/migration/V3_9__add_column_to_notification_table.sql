alter table if exists notification
    add column if not exists global_acknowledged bit after acknowledged;
    
update notification set global_acknowledged = 0;