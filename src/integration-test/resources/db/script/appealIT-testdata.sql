INSERT INTO errand (id, created,updated,version,application_received,case_title_addition,created_by,created_by_client,description,diary_number,end_date,errand_number,external_case_id,municipality_id,phase,priority,process_id,start_date,updated_by,updated_by_client,channel,case_type) VALUES
	 (1, '2024-04-17 11:42:42.339545','2024-04-17 11:50:31.705008',4,'2024-04-17 11:38:47.49','Eldstad/rökkanal, Skylt','UNKNOWN','UNKNOWN','Some description of the case.','123','2022-06-01','BUILD-2024-000001','caa230c6-abb4-4592-ad9a-34e263c2787b','2281','Aktualisering','MEDIUM',NULL,'2022-01-01','UNKNOWN','UNKNOWN','EMAIL','NYBYGGNAD_ANSOKAN_OM_BYGGLOV');

INSERT INTO stakeholder (id, created,updated,version,ad_account,authorized_signatory,first_name,last_name,organization_name,organization_number,person_id,`type`,errand_id) VALUES
	 (1, '2024-04-17 11:42:42.362476','2024-04-17 11:42:42.362476',0,'string','Test Testorsson','Test','Testorsson','Sundsvalls testfabrik','19901010-1234','3ed5bc30-6308-4fd5-a5a7-78d7f96f4438','PERSON', NULL),
	 (2, '2024-04-17 11:42:42.362476','2024-04-17 11:42:42.362476',0,'string','Test Testorsson','Test','Testorsson','Sundsvalls testfabrik','19901010-1234','3ed5bc30-6308-4fd5-a5a7-78d7f96f4438','PERSON', 1);

INSERT INTO decision (id, created,updated,version,decided_at,decision_outcome,decision_type,description,valid_from,valid_to,decided_by_id,errand_id) VALUES
	 (1, '2024-04-17 11:42:42.367478','2024-04-17 11:42:42.367478',0,'2024-04-17 11:38:47.49','APPROVAL','RECOMMENDED','string','2024-04-17 11:38:47.49','2024-04-17 11:38:47.49',1,1),
	 (2, '2024-04-17 12:42:42.367478','2024-04-17 12:42:42.367478',0,'2024-04-17 12:38:47.49','DISMISSAL','RECOMMENDED','string','2024-04-17 11:38:47.49','2024-04-17 11:38:47.49',1,1);;

INSERT INTO appeal (id, version,appeal_concern_communicated_at,created,decision_id,errand_id,registered_at,updated,description,status,timeliness_review) VALUES
	 (1, 0,'2024-04-17 11:44:11.089','2024-04-17 11:45:54.256607',1,1,'2024-04-17 11:44:11.089','2024-04-17 11:45:54.256607','Test överklagan','NEW','NOT_CONDUCTED');