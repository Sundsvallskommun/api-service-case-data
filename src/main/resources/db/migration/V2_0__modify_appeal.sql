alter table decision drop foreign key FK_decision_appeal_id;
alter table decision drop column appeal_id;
alter table attachment drop foreign key FK_appeal_id;
alter table attachment drop column appeal_id;
drop table appeal_extra_parameters;
drop table appeal;
