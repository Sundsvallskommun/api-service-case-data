set foreign_key_checks = 0;

truncate table attachment;
truncate table attachment_extra_parameters;
truncate table decision;
truncate table decision_extra_parameters;
truncate table decision_laws;
truncate table errand;
truncate table errand_extra_parameters;
truncate table errand_message_ids;
truncate table errand_statuses;
truncate table errand_labels;
truncate table facility;
truncate table facility_extra_parameters;
truncate table jv_commit;
truncate table jv_commit_property;
truncate table jv_global_id;
truncate table jv_snapshot;
truncate table message;
truncate table message_attachment;
truncate table message_attachment_data;
truncate table email_header;
truncate table email_header_values;
truncate table note;
truncate table note_extra_parameters;
truncate table notification;
truncate table stakeholder;
truncate table stakeholder_addresses;
truncate table stakeholder_contact_information;
truncate table stakeholder_extra_parameters;
truncate table stakeholder_roles;
truncate table message_recipients;

set foreign_key_checks = 1;
