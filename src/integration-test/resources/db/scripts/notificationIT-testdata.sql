INSERT INTO errand (id, created, updated, version, application_received, case_title_addition,
                    created_by, created_by_client, description, diary_number, end_date,
                    errand_number, external_case_id, municipality_id, phase, priority, process_id,
                    start_date, updated_by, updated_by_client, channel, case_type, namespace)
VALUES (1, '2024-04-17 11:42:42.339545', '2024-04-17 11:50:31.705008', 4, '2024-04-17 11:38:47.49',
        'Eldstad/rökkanal, Skylt', 'UNKNOWN', 'UNKNOWN', 'Some description of the case.', '123',
        '2022-06-01', 'BUILD-2024-000001', 'caa230c6-abb4-4592-ad9a-34e263c2787b', '2281',
        'Aktualisering', 'MEDIUM', NULL, '2022-01-01', 'UNKNOWN', 'UNKNOWN', 'EMAIL',
        'NYBYGGNAD_ANSOKAN_OM_BYGGLOV', 'MY_NAMESPACE');

INSERT INTO stakeholder (id, created, updated, version, ad_account, authorized_signatory,
                         first_name, last_name, organization_name, organization_number, person_id,
                         `type`, errand_id, municipality_id, namespace)
VALUES (1, '2024-04-17 11:42:42.362476', '2024-04-17 11:42:42.362476', 0, 'string',
        'Test Testorsson', 'Test', 'Testorsson', 'Sundsvalls testfabrik', '19901010-1234',
        '3ed5bc30-6308-4fd5-a5a7-78d7f96f4438', 'PERSON', NULL, '2281', 'MY_NAMESPACE'),
       (2, '2024-04-17 11:42:42.362476', '2024-04-17 11:42:42.362476', 0, 'string',
        'Test Testorsson', 'Test', 'Testorsson', 'Sundsvalls testfabrik', '19901010-1234',
        '3ed5bc30-6308-4fd5-a5a7-78d7f96f4438', 'PERSON', 1, '2281', 'MY_NAMESPACE');

INSERT INTO notification (acknowledged, global_acknowledged, created, errand_id, expires, modified, content, created_by, created_by_full_name, description, id, municipality_id, namespace, owner_full_name, owner_id, `type`) VALUES
	 (0,0,'2024-10-15 10:16:29.097000',1,'2024-11-14 08:16:29.096572',NULL,NULL,'creator123','Creator Creatorson','Ärende skapat','25d818b7-763e-4b77-9fce-1c7dfc42deb2','2281','MY_NAMESPACE','Joe Doe','testuser1','CREATE'),
	 (0,0,'2024-10-14 11:25:51.457000',1,'2000-10-30 23:30:00.000000','2024-10-14 12:31:31.147000','Some content of the notification 1','TestUser','Test Testsson','Some description of the notification 1 ','b7d6cc06-a08b-48a1-b7d3-1241b3bc345f','2281','MY_NAMESPACE','Test Testsson','testuser2','UPDATE'),
	 (1,1,'2024-10-14 11:25:51.457000',1,'2000-10-30 23:30:00.000000','2024-10-14 12:31:31.147000','Some content of the notification 2','TestUser','Test Testsson','Some description of the notification 2','b28ce1ec-2557-4007-b50c-fc7a273cadee','2281','MY_NAMESPACE','Test Testsson','testuser2','UPDATE');
