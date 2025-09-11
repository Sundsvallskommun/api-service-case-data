create table if not exists case_type
(
    display_name    varchar(255),
    municipality_id varchar(10),
    namespace       varchar(100),
    type            varchar(100) not null,
    primary key (type)
) engine = InnoDB;
