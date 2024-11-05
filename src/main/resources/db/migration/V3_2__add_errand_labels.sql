    create table errand_labels (
        value_order integer not null,
        errand_id bigint not null,
        value varchar(255),
        primary key (value_order, errand_id)
    ) engine=InnoDB;
    
    alter table if exists errand_labels 
       add constraint FK_errand_labels_errand_id 
       foreign key (errand_id) 
       references errand (id);