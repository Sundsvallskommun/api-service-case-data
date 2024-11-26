INSERT INTO errand
(id, created, updated, version, application_received, case_title_addition, case_type, created_by,
 created_by_client, description, diary_number, end_date, errand_number, external_case_id,
 municipality_id, phase, priority, process_id, start_date, updated_by, updated_by_client, channel,
 namespace)
VALUES (1, '2022-12-02 15:13:45.363', '2022-12-02 15:15:01.563', 1, NULL,
        'Nytt parkeringstillstånd', 'PARKING_PERMIT', 'UNKNOWN', 'WSO2_test', '', '', NULL,
        'ERRAND-NUMBER-1', '', '2281', 'Aktualisering', 'MEDIUM',
        '896a44d8-724b-11ed-a840-0242ac110002', NULL, 'UNKNOWN', 'WSO_test', NULL, 'my.namespace'),
       (2, '2022-12-02 15:13:45.363', '2022-12-02 15:15:01.563', 1, NULL,
        'Nytt parkeringstillstånd', 'PARKING_PERMIT', 'UNKNOWN', 'WSO2_test', '', '', NULL,
        'ERRAND-NUMBER-2', '', '2281', 'Aktualisering', 'MEDIUM',
        '896a44d8-724b-11ed-a840-0242ac110002', NULL, 'UNKNOWN', 'WSO_test', NULL, 'my.namespace');

INSERT INTO errand_extra_parameters (errand_id, id, display_name, parameters_key)
values (1, '123e4567-e89b-12d3-a456-426614174000', 'test', 'artefact.permit.number'),
       (2, '123e4567-e89b-12d3-a456-426614174001', 'test', 'artefact.permit.number');

INSERT INTO errand_extra_parameter_values (extra_parameter_id, value)
VALUES ('123e4567-e89b-12d3-a456-426614174000', 'Test value1'),
       ('123e4567-e89b-12d3-a456-426614174001', 'Test value2');

INSERT INTO stakeholder
(id, created, updated, version, ad_account, authorized_signatory, first_name, last_name,
 organization_name, organization_number, person_id, `type`, errand_id, namespace)
VALUES (1, '2022-12-02 15:13:45.363', '2022-12-02 15:15:01.563', 0, 'AD-1', NULL, 'John',
        'Doe', NULL, NULL, 'd7af5f83-166a-468b-ab86-da8ca30ea97c', 'PERSON', 1, 'my.namespace'),
       (2, '2022-12-02 15:13:45.363', '2022-12-02 15:15:01.563', 0, 'AD-2', NULL, 'Kalle',
        'Anka', NULL, NULL, 'd1af5f83-166a-468b-ab86-da8ca30ea97c', 'PERSON', 1, 'my.namespace'),
       (3, '2022-12-02 15:13:45.363', '2022-12-02 15:15:01.563', 0, 'AD-3', NULL, 'Kajsa',
        'Anka', NULL, NULL, 'd2af5f83-166a-468b-ab86-da8ca30ea97c', 'PERSON', 1, 'my.namespace');

INSERT INTO stakeholder_roles(stakeholder_id, roles, role_order)
VALUES (1, 'APPLICANT', 0),
       (2, 'APPLICANT', 0),
       (3, 'CONTROL_OFFICIAL', 0);
