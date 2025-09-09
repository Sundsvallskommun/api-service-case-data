create table if not exists case_type
(
    display_name varchar(255),
    type         varchar(255) not null,
    primary key (type)
) engine = InnoDB;
