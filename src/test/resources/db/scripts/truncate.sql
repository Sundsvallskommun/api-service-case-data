SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE table attachment;
TRUNCATE table attachment_extra_parameters;

--TRUNCATE table decision;

TRUNCATE table errand;
TRUNCATE table errand_extra_parameters;
TRUNCATE table errand_statuses;
--TRUNCATE table errand_message_ids;

TRUNCATE table facility;
TRUNCATE table facility_extra_parameters;

TRUNCATE table note;
TRUNCATE table note_extra_parameters;

TRUNCATE table message;
TRUNCATE table message_attachment;
TRUNCATE table message_attachment_data;

TRUNCATE table stakeholder;
TRUNCATE table stakeholder_extra_parameters;
TRUNCATE table stakeholder_addresses;
TRUNCATE table stakeholder_contact_information;
TRUNCATE table stakeholder_roles;

--TRUNCATE table jv_commit;
--TRUNCATE table jv_commit_property;
--TRUNCATE table jv_global_id;
--TRUNCATE table jv_snapshot;

SET FOREIGN_KEY_CHECKS = 1;