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

INSERT INTO errand_extra_parameters(errand_id, extra_parameter_key, extra_parameter_value)
VALUES (1, 'artefact.permit.number', 'Test value1'),
       (2, 'artefact.permit.number', 'Test value2');

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
