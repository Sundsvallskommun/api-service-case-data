alter table if exists notification modify column acknowledged bit(1) default 0 not null;
alter table if exists notification modify column global_acknowledged bit(1) default 0 not null;