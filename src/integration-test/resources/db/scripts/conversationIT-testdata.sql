INSERT INTO errand(id, created, updated, version, application_received, case_title_addition,
                   case_type, created_by,
                   created_by_client, description, diary_number, end_date, errand_number,
                   external_case_id,
                   municipality_id, phase, priority, process_id, start_date, updated_by,
                   updated_by_client, channel, namespace)
VALUES (1, '2022-12-02 15:13:45.363', '2022-12-02 15:15:01.563', 1, NULL,
        'Nytt parkeringstillstånd', 'PARKING_PERMIT', 'UNKNOWN', 'UNKNOWN', '', '', NULL,
        'ERRAND-NUMBER-1', '', '2281', 'Aktualisering', 'MEDIUM',
        '896a44d8-724b-11ed-a840-0242ac110002', NULL, 'UNKNOWN', 'UNKNOWN', NULL, 'MY_NAMESPACE'),
       (2, '2022-12-02 15:13:45.363', '2022-12-02 15:15:01.563', 1, NULL,
        'Nytt parkeringstillstånd', 'PARKING_PERMIT', 'UNKNOWN', 'UNKNOWN', '', '', NULL,
        'ERRAND-NUMBER-2', '', '2281', 'Aktualisering', 'MEDIUM',
        '896a44d8-724b-11ed-a840-0242ac110002', NULL, 'UNKNOWN', 'UNKNOWN', NULL, 'MY_NAMESPACE');

-- Insert data into conversation table
INSERT INTO conversation (municipality_id, latest_synced_sequence_number, namespace, type, errand_id, id,
                          message_exchange_id, topic)
VALUES ('2281', 100, 'MY_NAMESPACE', 'EXTERNAL', '1', '896a44d8-724b-11ed-a840-0242ac110002',
        'c1a1b2c3-d4e5-f6a7-b8c9-d0e1f2a3b4c5',
        'Topic 1'),
       ('2281', 101, 'MY_NAMESPACE', 'INTERNAL', '2', '896a44d8-724b-11ed-a840-0242ac110003',
        'c1a1b2c3-d4e5-f6a7-b8c9-d0e1f2a3b4c6',
        'Topic 2');


-- Insert data into conversation_relation_id table
INSERT INTO conversation_relation_id (conversation_id, relation_id)
VALUES ('896a44d8-724b-11ed-a840-0242ac110002', 'RELATION-ID-1'),
       ('896a44d8-724b-11ed-a840-0242ac110003', 'RELATION-ID-2');
