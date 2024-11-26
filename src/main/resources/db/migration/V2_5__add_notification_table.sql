create table notification (
    acknowledged bit,
    created datetime(6),
    errand_id bigint,
    expires datetime(6),
    modified datetime(6),
    content varchar(255),
    created_by varchar(255),
    created_by_full_name varchar(255),
    description varchar(255),
    id varchar(255) not null,
    municipality_id varchar(255) not null,
    namespace varchar(255) not null,
    owner_full_name varchar(255),
    owner_id varchar(255),
    type varchar(255),
    primary key (id)
) engine=InnoDB;


create index idx_notification_municipality_id 
   on notification (municipality_id);

create index idx_notification_namespace 
   on notification (namespace);

create index idx_notification_owner_id 
   on notification (owner_id);
   
alter table if exists notification 
   add constraint fk_notification_errand_id 
   foreign key (errand_id) 
   references errand (id);
