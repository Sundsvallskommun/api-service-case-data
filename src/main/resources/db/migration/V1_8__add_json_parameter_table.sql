create table if not exists json_parameter (
    id varchar(255) not null,
    errand_id bigint not null,
    parameter_key varchar(255) not null,
    schema_id varchar(255) not null,
    value longtext not null,
    primary key (id)
) engine=InnoDB;

create index if not exists idx_json_parameter_errand_id on json_parameter (errand_id);
create index if not exists idx_json_parameter_key on json_parameter (parameter_key);

alter table json_parameter
    add constraint fk_json_parameter_errand_id
    foreign key (errand_id) references errand(id)
    on delete cascade;
