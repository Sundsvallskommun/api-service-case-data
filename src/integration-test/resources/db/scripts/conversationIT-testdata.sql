INSERT INTO errand(id, created, updated, version, application_received, case_title_addition, case_type, created_by, created_by_client, description, diary_number, end_date, errand_number, external_case_id, municipality_id, phase, priority, process_id, start_date, updated_by, updated_by_client, channel, namespace)
VALUES (1, '2022-12-02 15:13:45.363', '2022-12-02 15:15:01.563', 1, NULL, 'Nytt parkeringstillstånd', 'PARKING_PERMIT', 'UNKNOWN', 'UNKNOWN', '', '', NULL, 'ERRAND-NUMBER-1', '', '2281', 'Aktualisering', 'MEDIUM', '896a44d8-724b-11ed-a840-0242ac110002', NULL, 'UNKNOWN', 'UNKNOWN', NULL, 'MY_NAMESPACE'),
       (2, '2022-12-02 15:13:45.363', '2022-12-02 15:15:01.563', 1, NULL, 'Nytt parkeringstillstånd', 'PARKING_PERMIT', 'UNKNOWN', 'UNKNOWN', '', '', NULL, 'ERRAND-NUMBER-2', '', '2281', 'Aktualisering', 'MEDIUM', '896a44d8-724b-11ed-a840-0242ac110002', NULL, 'UNKNOWN', 'UNKNOWN', NULL, 'MY_NAMESPACE'),
       (3, '2022-12-02 15:13:45.363', '2022-12-02 15:15:01.563', 1, NULL, 'Avvikelseärende', 'PARATRANSIT', 'UNKNOWN', 'UNKNOWN', '', '', NULL, 'ERRAND-NUMBER-3', '', '2281', 'Aktualisering', 'MEDIUM', '896a44d8-724b-11ed-a840-0242ac110003', NULL, 'UNKNOWN', 'UNKNOWN', NULL, 'MY_NAMESPACE');

-- Insert data into stakeholder table
INSERT INTO stakeholder (id, created, updated, version, ad_account, authorized_signatory, first_name, last_name, organization_name, organization_number, person_id, `type`, errand_id, municipality_id, namespace)
VALUES (1, '2024-04-17 11:42:42.362476', '2024-04-17 11:42:42.362476', 0, 'own01own', 'Owner Ownersson', 'Owner', 'Ownersson', 'Sundsvalls testfabrik', '19900101-1234', '3ed5bc30-6308-4fd5-a5a7-78d7f96f4438', 'PERSON', 1, '2281', 'MY_NAMESPACE'),
       (2, '2024-04-17 11:42:42.362476', '2024-04-17 11:42:42.362476', 0, null, 'Citizen Citizensson', 'Citizen', 'Citizensson', 'Sundsvalls testfabrik', '19900202-5678', '352ec7b4-cae0-4216-8d47-3e10246b2952', 'PERSON', 1, '2281', 'MY_NAMESPACE'),
       (3, '2024-04-17 11:42:42.362476', '2024-04-17 11:42:42.362476', 0, 'adm01adm', 'Admin Adminsson', 'Admin', 'Adminsson', 'Sundsvalls testfabrik', '19900303-2345', NULL, 'PERSON', 3, '2281', 'MY_NAMESPACE'),
       (4, '2024-04-17 11:42:42.362476', '2024-04-17 11:42:42.362476', 0, 'tes02rep', 'Test Reportersson', 'Test', 'Reportersson', 'Sundsvalls testfabrik', '19900404-6789', NULL, 'PERSON', 3, '2281', 'MY_NAMESPACE');
       
INSERT INTO stakeholder_roles(stakeholder_id, roles, role_order)
VALUES (1, 'ADMINISTRATOR', 0),
       (2, 'APPLICANT', 0),
       (3, 'ADMINISTRATOR', 0),
       (4, 'REPORTER', 0);

INSERT INTO stakeholder_contact_information(stakeholder_id, contact_type, value, contact_information_order)
VALUES (4, 'EMAIL', 'reporter.email@testdomain.local', 0);

-- Insert data into conversation table
INSERT INTO conversation (municipality_id, latest_synced_sequence_number, namespace, type, errand_id, id, message_exchange_id, topic)
VALUES ('2281', 100, 'MY_NAMESPACE', 'EXTERNAL', '1', '896a44d8-724b-11ed-a840-0242ac110002', 'c1a1b2c3-d4e5-f6a7-b8c9-d0e1f2a3b4c5', 'Topic 1'),
       ('2281', 101, 'MY_NAMESPACE', 'INTERNAL', '2', '896a44d8-724b-11ed-a840-0242ac110003', 'c1a1b2c3-d4e5-f6a7-b8c9-d0e1f2a3b4c6', 'Topic 2'),
       ('2281', 102, 'MY_NAMESPACE', 'INTERNAL', '3', '896a44d8-724b-11ed-a840-0242ac110004', 'c1a1b2c3-d4e5-f6a7-b8c9-d0e1f2a3b4c7', 'Topic 3');

-- Insert data into conversation_relation_id table
INSERT INTO conversation_relation_id (conversation_id, relation_id)
VALUES ('896a44d8-724b-11ed-a840-0242ac110002', 'RELATION-ID-1'),
       ('896a44d8-724b-11ed-a840-0242ac110003', 'RELATION-ID-2'),
       ('896a44d8-724b-11ed-a840-0242ac110004', 'RELATION-ID-3');