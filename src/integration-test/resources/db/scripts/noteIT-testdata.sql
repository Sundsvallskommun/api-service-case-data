INSERT INTO errand (id, created, updated, version, application_received, case_title_addition,
                    case_type, created_by, created_by_client, description, diary_number, end_date,
                    errand_number, external_case_id, municipality_id, phase, priority, process_id,
                    start_date, updated_by, updated_by_client, channel, namespace)
VALUES (1, '2022-12-02 15:13:45.363', '2022-12-02 15:15:01.563', 1, NULL,
        'Nytt parkeringstillstånd', 'PARKING_PERMIT', 'UNKNOWN', 'WSO2_test', '', '', NULL,
        'ERRAND-NUMBER-1', '', '2281', 'Aktualisering', 'MEDIUM',
        '896a44d8-724b-11ed-a840-0242ac110002', NULL, 'UNKNOWN', 'WSO_test', NULL, 'my.namespace'),
       (2, '2022-12-02 15:13:45.363', '2022-12-02 15:15:01.563', 1, NULL,
        'Nytt parkeringstillstånd', 'PARKING_PERMIT', 'UNKNOWN', 'WSO2_test', '', '', NULL,
        'ERRAND-NUMBER-2', '', '2281', 'Aktualisering', 'MEDIUM',
        '896a44d8-724b-11ed-a840-0242ac110002', NULL, 'UNKNOWN', 'WSO_test', NULL, 'my.namespace');

INSERT INTO note (id, created, updated, version, created_by, `text`, title, updated_by, errand_id,
                  municipality_id, namespace)
VALUES (1, '2023-10-02 15:13:45.363', '2023-10-02 15:13:45.363', 1, 'UNKNOWN', 'TEXT', 'TITLE-1',
        'testUser', 1, '2281', 'my.namespace');
