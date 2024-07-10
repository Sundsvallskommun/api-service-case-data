alter table note
    add column municipality_id varchar(255);
create index idx_note_municipality_id on note (municipality_id);

alter table stakeholder
    add column municipality_id varchar(255);
create index idx_stakeholder_municipality_id on stakeholder (municipality_id);

alter table message_attachment
    add column municipality_id varchar(255);
create index idx_message_attachment_municipality_id on message_attachment (municipality_id);

alter table message
    add column municipality_id varchar(255);
create index idx_message_municipality_id on message (municipality_id);

alter table facility
    add column municipality_id varchar(255);
create index idx_facility_municipality_id on facility (municipality_id);

alter table decision
    add column municipality_id varchar(255);
create index idx_decision_municipality_id on decision (municipality_id);

alter table attachment
    add column municipality_id varchar(255);
create index idx_attachment_municipality_id on attachment (municipality_id);

alter table appeal
    add column municipality_id varchar(255);
create index idx_appeal_municipality_id on appeal (municipality_id);