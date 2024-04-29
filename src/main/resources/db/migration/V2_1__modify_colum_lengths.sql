alter table attachment_extra_parameters
    modify extra_parameter_value varchar(8192) null;

alter table decision_extra_parameters
    modify extra_parameter_value varchar(8192) null;

alter table errand_extra_parameters
    modify extra_parameter_value varchar(8192) null;

alter table facility_extra_parameters
    modify extra_parameter_value varchar(8192) null;

alter table note_extra_parameters
    modify extra_parameter_value varchar(8192) null;

alter table stakeholder_extra_parameters
    modify extra_parameter_value varchar(8192) null;

alter table errand
    modify description varchar(8192) null;
