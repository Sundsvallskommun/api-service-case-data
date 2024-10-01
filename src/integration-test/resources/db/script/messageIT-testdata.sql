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
