INSERT INTO errand(id, created, updated, version, application_received, case_title_addition,
                   case_type, created_by, created_by_client, description, diary_number, end_date,
                   errand_number, external_case_id, municipality_id, phase, priority, process_id,
                   start_date, updated_by, updated_by_client, channel, namespace, status,
                   status_created, status_description)
VALUES (1, '2022-12-02 15:13:45.363', '2022-12-02 15:15:01.563', 1, NULL,
        'Nytt parkeringstillstånd', 'PARKING_PERMIT', 'UNKNOWN', 'WSO2_test', '', '', NULL,
        'PRH-2022-000029', '123', '2281', 'Aktualisering', 'MEDIUM',
        '896a44d8-724b-11ed-a840-0242ac110002', NULL, 'UNKNOWN', 'WSO_test', NULL, 'MY_NAMESPACE',
        'UNKNOWN', '2022-12-02 15:15:01.563', 'Status description'),
       (2, '2022-12-02 15:13:45.363', '2022-12-02 15:15:01.563', 1, NULL,
        'Nytt parkeringstillstånd', 'PARKING_PERMIT', 'UNKNOWN', 'WSO2_test', '', '', NULL,
        'PRH-2022-000030', '', '2281', 'Aktualisering', 'MEDIUM',
        '896a44d8-724b-11ed-a840-0242ac110002', NULL, 'UNKNOWN', 'WSO_test', NULL, 'MY_NAMESPACE',
        'UNKNOWN', '2022-12-02 15:15:01.563', 'Status description');

INSERT INTO json_parameter(id, errand_id, parameter_key, schema_id, value)
VALUES ('json-param-1', 1, 'formData1', '2281_person_1.0', '{"firstName":"Joe","lastName":"Doe"}'),
       ('json-param-2', 1, 'formData2', '2281_address_1.0', '{"street":"Main St","city":"Springfield"}');

INSERT INTO case_type(id, type, display_name, municipality_id, namespace)
VALUES ('00000000-0000-0000-0000-000000000001', 'PARKING_PERMIT', '', 2281, 'SBK_PARKING_PERMIT'),
       ('00000000-0000-0000-0000-000000000002', 'PARKING_PERMIT', '', 2281, 'MY_NAMESPACE');
