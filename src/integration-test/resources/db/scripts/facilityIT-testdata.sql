INSERT INTO errand (id, created, updated, version, application_received, case_title_addition,
                    created_by, created_by_client, description, diary_number, end_date,
                    errand_number, external_case_id, municipality_id, phase, priority, process_id,
                    start_date, updated_by, updated_by_client, channel, case_type, namespace)
VALUES (1, '2024-04-17 11:42:42.339545', '2024-04-17 11:50:31.705008', 4, '2024-04-17 11:38:47.49',
        'Eldstad/rökkanal, Skylt', 'UNKNOWN', 'UNKNOWN', 'Some description of the case.', '123',
        '2022-06-01', 'BUILD-2024-000001', 'caa230c6-abb4-4592-ad9a-34e263c2787b', '2281',
        'Aktualisering', 'MEDIUM', NULL, '2022-01-01', 'UNKNOWN', 'UNKNOWN', 'EMAIL',
        'MEX_EARLY_DIALOG_PLAN_NOTIFICATION', 'MY_NAMESPACE');

INSERT INTO stakeholder (id, created, updated, version, ad_account, authorized_signatory,
                         first_name, last_name, organization_name, organization_number, person_id,
                         `type`, errand_id, municipality_id, namespace)
VALUES (1, '2024-04-17 11:42:42.362476', '2024-04-17 11:42:42.362476', 0, 'string',
        'Test Testorsson', 'Test', 'Testorsson', 'Sundsvalls testfabrik', '19901010-1234',
        '3ed5bc30-6308-4fd5-a5a7-78d7f96f4438', 'PERSON', NULL, '2281', 'MY_NAMESPACE'),
       (2, '2024-04-17 11:42:42.362476', '2024-04-17 11:42:42.362476', 0, 'string',
        'Test Testorsson', 'Test', 'Testorsson', 'Sundsvalls testfabrik', '19901010-1234',
        '3ed5bc30-6308-4fd5-a5a7-78d7f96f4438', 'PERSON', 1, '2281', 'MY_NAMESPACE');

INSERT INTO facility (id, created, updated, version, address_category, apartment_number, attention,
                      care_of, city, country, house_number, invoice_marking, is_zoning_plan_area,
                      latitude, longitude, postal_code, property_designation, street, description,
                      facility_collection_name, main_facility, errand_id, facility_type,
                      municipality_id, namespace)
VALUES (1, '2024-04-25 15:37:23.525120', '2024-04-26 08:14:14.039373', 2, 'POSTAL_ADDRESS',
        'apartmentNumber 1', 'Attention', 'Cateof 1', 'City 1', 'Country 1', 'HouseNumber 1',
        'invoiceMarking 1', 1, 3.0, 2.0, 'PostalCode 1', 'PropertyDesignation 1', 'Street 1',
        'Description 1', 'FacilityCollectionName 1', 1, 1, 'WORKSHOP_BUILDING', '2281',
        'MY_NAMESPACE'),
       (2, '2024-04-25 15:37:23.525120', '2024-04-26 08:14:14.039373', 2, 'POSTAL_ADDRESS',
        'apartmentNumber 2', 'Attention', 'Cateof 2', 'City 2', 'Country 2', 'HouseNumber 2',
        'invoiceMarking 2', 1, 3.0, 2.0, 'PostalCode 2', 'PropertyDesignation 2', 'Street 2',
        'Description 2', 'FacilityCollectionName 3', 1, 1, 'WORKSHOP_BUILDING', '2281',
        'MY_NAMESPACE');

INSERT INTO facility_extra_parameters (facility_id, extra_parameter_value, extra_parameter_key)
VALUES (1, 'string1', 'additionalProp1'),
       (1, 'string2', 'additionalProp2'),
       (1, 'string3', 'additionalProp3');
