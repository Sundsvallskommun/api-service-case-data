INSERT INTO errand(id, created, updated, version, application_received, case_title_addition,
                   case_type, created_by,
                   created_by_client, description, diary_number, end_date, errand_number,
                   external_case_id,
                   municipality_id, phase, priority, process_id, start_date, updated_by,
                   updated_by_client, channel, namespace)
VALUES (1, '2022-12-02 15:13:45.363', '2022-12-02 15:15:01.563', 1, NULL,
        'Nytt parkeringstillstånd', 'PARKING_PERMIT', 'UNKNOWN', 'WSO2_test', '', '', NULL,
        'ERRAND-NUMBER-1', '', '2281', 'Aktualisering', 'MEDIUM',
        '896a44d8-724b-11ed-a840-0242ac110002', NULL, 'UNKNOWN', 'WSO_test', NULL, 'my.namespace'),
       (2, '2022-12-02 15:13:45.363', '2022-12-02 15:15:01.563', 1, NULL,
        'Nytt parkeringstillstånd', 'PARKING_PERMIT', 'UNKNOWN', 'WSO2_test', '', '', NULL,
        'ERRAND-NUMBER-2', '', '2281', 'Aktualisering', 'MEDIUM',
        '896a44d8-724b-11ed-a840-0242ac110002', NULL, 'UNKNOWN', 'WSO_test', NULL, 'my.namespace');


INSERT INTO message(messageid, errand_number, direction, familyid, external_caseid, subject,
                    message,
                    username, first_name, last_name, email, userid, sent, message_type,
                    mobile_number, viewed, municipality_id, namespace)
VALUES ('a8883fb9-60b4-4f38-9f48-642070ff49ee', 'ERRAND-NUMBER-1', 'INBOUND', 387, 1234, 'Subject',
        'Message', 'kctest', 'john', 'doe', 'johndoe@email.com', 'userid',
        '2024-01-01T12:00:00.000000', 'EMAIL', '0701234567', 1, '2281', 'my.namespace');

INSERT INTO email_header(id, header, message_id)
VALUES (1, 'MESSAGE_ID', 'a8883fb9-60b4-4f38-9f48-642070ff49ee'),
       (2, 'IN_REPLY_TO', 'a8883fb9-60b4-4f38-9f48-642070ff49ee'),
       (3, 'REFERENCES', 'a8883fb9-60b4-4f38-9f48-642070ff49ee');

INSERT INTO email_header_values(email_header_id, value, value_index)
VALUES (1, '<test123@domain.com>', 0),
       (2, '<123test@domain.com>', 0),
       (3, '<123test@domain.com>', 0),
       (3, '<123@domain.com>', 1),
       (3, '<456@domain.com>', 2);

INSERT INTO message_recipients(message_id, recipient_email)
VALUES ('a8883fb9-60b4-4f38-9f48-642070ff49ee', 'johndoe@email.com'),
       ('a8883fb9-60b4-4f38-9f48-642070ff49ee', 'someemail@sundsvall.se');
