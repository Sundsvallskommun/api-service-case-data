create table case_type
(
    municipality_id varchar(10)  not null,
    namespace       varchar(100) not null,
    type            varchar(100),
    display_name    varchar(255),
    id              varchar(255) not null,
    primary key (id)
) engine = InnoDB;

create index idx_case_type_municipality_namespace
    on case_type (municipality_id, namespace);

alter table if exists case_type
    add constraint ux_case_type_municipality_namespace_type unique (municipality_id, namespace, type);
