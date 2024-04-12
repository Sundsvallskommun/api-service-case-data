alter table decision drop foreign key FK_decision_appeal_id;
alter table decision drop column appeal_id;
alter table attachment drop foreign key FK_appeal_id;
alter table attachment drop column appeal_id;
drop table appeal_extra_parameters;
drop table appeal;

create table appeal (
    version integer,
    appeal_concern_communicated_at datetime(6),
    created datetime(6),
    decision_id bigint,
    errand_id bigint,
    id bigint not null auto_increment,
    registered_at datetime(6),
    updated datetime(6),
    description text,
    status varchar(255),
    timeliness_review varchar(255),
    primary key (id)
) engine=InnoDB;

alter table if exists appeal
   add constraint FK_appeal_decision_id
   foreign key (decision_id)
   references decision (id);

alter table if exists appeal
   add constraint FK_appeal_errand_id
   foreign key (errand_id)
   references errand (id);