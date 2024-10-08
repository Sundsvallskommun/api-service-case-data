alter table note
    add column namespace varchar(255);
create index idx_note_namespace on note (namespace);

alter table stakeholder
    add column namespace varchar(255);
create index idx_stakeholder_namespace on stakeholder (namespace);

alter table message_attachment
    add column namespace varchar(255);
create index idx_message_attachment_namespace on message_attachment (namespace);

alter table message
    add column namespace varchar(255);
create index idx_message_namespace on message (namespace);

alter table facility
    add column namespace varchar(255);
create index idx_facility_namespace on facility (namespace);

alter table decision
    add column namespace varchar(255);
create index idx_decision_namespace on decision (namespace);

alter table attachment
    add column namespace varchar(255);
create index idx_attachment_namespace on attachment (namespace);

alter table appeal
    add column namespace varchar(255);
create index idx_appeal_namespace on appeal (namespace);

alter table errand
    add column namespace varchar(255);
create index idx_errand_namespace on errand (namespace);

create index idx_errand_municipality_id on errand (municipality_id);
