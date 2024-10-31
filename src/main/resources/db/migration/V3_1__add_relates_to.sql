create table related_errand
(
    errand_id             bigint,
    related_errand_id     bigint,
    id                    varchar(255) not null,
    related_errand_number varchar(255),
    relation_reason       varchar(255),
    primary key (id)
) engine = InnoDB;

alter table if exists related_errand
    add constraint FK_errand_related_errands_errand_id
        foreign key (errand_id)
            references errand (id);
