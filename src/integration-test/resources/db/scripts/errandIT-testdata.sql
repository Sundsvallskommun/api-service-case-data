INSERT INTO errand(id, created, updated, version, application_received, case_title_addition,
                   case_type, created_by,
                   created_by_client, description, diary_number, end_date, errand_number,
                   external_case_id,
                   municipality_id, phase, priority, process_id, start_date, updated_by,
                   updated_by_client, channel, namespace, status, status_created, status_description)
VALUES (1, '2022-12-02 15:13:45.363', '2022-12-02 15:15:01.563', 1, NULL,
        'Nytt parkeringstillstånd', 'PARKING_PERMIT', 'UNKNOWN', 'WSO2_test', '', '', NULL,
        'PRH-2022-000029', '', '2281', 'Aktualisering', 'MEDIUM',
        '896a44d8-724b-11ed-a840-0242ac110002', NULL, 'UNKNOWN', 'WSO_test', NULL, 'MY_NAMESPACE', 'UNKNOWN',
        '2022-12-02 15:15:01.563', 'Status description'),
       (2, '2022-12-02 15:13:45.363', '2022-12-02 15:15:01.563', 1, NULL,
        'Nytt parkeringstillstånd', 'PARKING_PERMIT', 'UNKNOWN', 'WSO2_test', '', '', NULL,
        'PRH-2022-000030', '', '2281', 'Aktualisering', 'MEDIUM',
        '896a44d8-724b-11ed-a840-0242ac110002', NULL, 'UNKNOWN', 'WSO_test', NULL, 'MY_NAMESPACE', 'UNKNOWN',
        '2022-12-02 15:15:01.563', 'Status description');

INSERT INTO errand_labels (value_order, errand_id, value)
VALUES (0, 1, 'errand-1-label-1'),
       (1, 1, 'errand-1-label-2'),
       (0, 2, 'errand-2-label-1'),
       (1, 2, 'errand-2-label-2');
