INSERT INTO errand (id, created, updated, version, application_received, case_title_addition,
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

INSERT INTO attachment (id, created, updated, version, category, extension, file, mime_type, name,
                        note, errand_id, decision_id, municipality_id, namespace)
VALUES (1, '2022-12-02 15:13:45.363', '2022-12-02 15:15:01.563', 1, 'MEDICAL_CONFIRMATION', '.pdf',
        'FILE-1', 'application/pdf', 'test1.pdf', 'NOTE-1', 1, NULL, '2281',
        'my.namespace'),
       (2, '2022-12-02 15:13:45.363', '2022-12-02 15:15:01.563', 1, 'PASSPORT_PHOTO', '.pdf',
        'FILE-2', 'application/pdf', 'test2.pdf', 'NOTE-2', 1, NULL, '2281',
        'my.namespace'),
       (3, '2022-12-02 15:13:45.363', '2022-12-02 15:15:01.563', 1, 'POLICE_REPORT', '.pdf',
        'FILE-3', 'application/pdf', 'test3.pdf', 'NOTE-3', 2, NULL, '2281',
        'my.namespace'),
       (4, '2022-12-02 15:13:45.363', '2022-12-02 15:15:01.563', 1, 'ANSUPA', '.pdf', 'FILE-4',
        'application/pdf', 'test4.pdf', 'NOTE-4', 2, NULL, '2281',
        'my.namespace');

INSERT INTO message_attachment_data (id, file)
VALUES (1,
        FROM_BASE64('iVBORw0KGgoAAAANSUhEUgAAAIsAAACPCAMAAAD9VtjbAAAAAXNSR0IArs4c6QAAAARnQU1BAACx jwv8YQUAAAL3UExURXFxcQICAgAAAAEBAS8vLxERERUVFRQUFBMTExYWFgcHBw4ODsLCwurq6ujo 6OPj49ra2tvb2+fn5+np6ebm5tzc3N7e3uXl5eLi4uDg4NnZ2eHh4dbW1t3d3d/f39XV1dfX1+vr 61ZWVhAQEP////f39/b29vX19fn5+f7+/vv7+/T09Pz8/PLy8v39/fHx8fPz8/Dw8Pr6+mFhYdTU 1O/v7+7u7u3t7ezs7Pj4+FxcXF1dXWNjY2RkZGBgYNPT015eXl9fX+Tk5M3Nzb6+vrW1tby8vMHB wbu7u8PDw8vLy9jY2JKSkmVlZUZGRisrKxgYGAoKCgQEBAMDAwYGBiEhIUFBQVdXV25ubo6OjrKy so+Pj0tLSxwcHBoaGkVFRYuLi9HR0aampk5OTltbW8XFxaOjozg4OA0NDXt7e01NTTs7O5ubm7+/ v2dnZw8PD09PTx4eHlNTUzk5OWpqagwMDBcXFyMjIy4uLjc3NzQ0NDAwMCoqKiAgIHh4eGJiYoyM jIiIiK6urtDQ0H19fTIyMgUFBbi4uAsLC729vcjIyBkZGbOzs6qqqpSUlFBQULq6ukRERHNzc6ys rIGBgXZ2dmlpaSQkJFRUVBsbG9nZ2Jycmx8fH6GhoUxMTNLS0jY2NlJSUiYmJp+fn6CgoKSkpFhY WJGRkc7OziIiImhoaEhISFpaWqurq4eHh6ioqMzMzFlZWUZGRa+vr3l5eIqKim1tbXx8fCUlJZyc nImJic/Pz29vbxMTFD4+Ppqami0tLaenp0BAQFFRUYaGhsnJySwsLEdHR6WlpT09PaKioklJSYOD g8DAwAkJCXl5eZmZmX5+fjExMYSEhJ2dnbe3t42NjVtbWikpKYWFhTo6OkJCQn9/f3p6end3d5aW lrCwsLa2ticnJ3JycpeXl6mpqYCAgJ6enggICMbGxsTExENDQ8rKyjMzM2tra5iYmHV1dRISErm5 uZWVlVVVVWZmZpOTkz8/P5CQkEpKSrS0tDw8PAAAAOdW1PAAAAD9dFJOU/////////////////// //////////////////////////////////////////////////////////////////////////// //////////////////////////////////////////////////////////////////////////// //////////////////////////////////////////////////////////////////////////// //////////////////////////////////////////////////////////////////////////// /////////////wD2TzQDAAAACXBIWXMAAA7DAAAOwwHHb6hkAAAM3klEQVR4Xu1by5EjuQ7cmAhd xwPd+jAWyAa5JQN0aDN0kymyQB7oJt0fMvGvIkvduy9i99AcTSEFJEEWCKI+3f3X/dd/pV3+MvBf aD9zGbefuYzbz1zGLeey+7eajV/msrse0PbS5sDlPwArxeFpMyhx2X+gHeRfB0UuTXMg0jUhDaw0 L5tAiYvNxRkOIL8FIANALoAEAzK5g7hceUpkONDOFSwVfgJ7KhIg+g14n9ZbNeu57BBWWiEAVFHA SlEsCkQuNBjMgcvOHayR5IsRFn0cQG6YmmJMWWsoB/mi55gntAYpF2Bp0oUCMJnALJY0VAz3UVK/ BJb+cwqmiVxxIHLRCXMa7iONjAdC/kMqVIsC11DmF1XgIEqFJgWYVD6BSlAGuXvFRM3HJoCcA99Z i04BIDt4U1/wATBZgFHWADIA5ArMZjmsL2hghJfQjIEI919zkcAUWV/WwLmDNRK3YnMGJJHLCYBc A8g3XEhFW/uIJAGaX1AYwIfIABxRQRJlUEKjgPHD9wBB2agvS2oDLhUowvhsafLN/X7lQB3vaTCE AxlONgDkfn89no/arj62Ti+5PnROoYC/EZfYBRpl+eyPp9fjkrdC4mP3eDxfp7NyhyMTuSSY7qPG EqATTIBT5vf96bPOorfd43QF09wEgGOi4nhWX8AxpoHoW8HH4fZpvebtdVSqdRKRWQSZyfO1+gJA GQCWj9vDumy3u7AxcnM3AOP6Yowp4Bld38fE2kO3A3sXL6lQzWAuEhexkWFZwSWHDCA9b/M0WbXd 0XqpO/ix5ho4nu9pacoagziJLzWZTOttMgFWcLhGzoCcgC+vjzWu/Dpn6naf1xdQXHaFgMN3pyI5 I90xMv05gIgBBnNZ3ddhbILQZK/adpcH2+VSHgGzvWzE9BJAz35SX9yaAB8HHyejRru8TkfsLVAg 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'));

INSERT INTO message_attachment (message_attachment_data_id, attachmentid, content_type, messageid,
                                name, municipality_id, namespace)
VALUES (1, '05b29c30-4512-46c0-9d82-d0f11cb04bae', 'image/png',
        '02485d15-fa8b-488a-a907-fa4de5d6e5c9', 'test_image.png', '2281', 'my.namespace');

INSERT INTO stakeholder(id, created, updated, version, ad_account, authorized_signatory, first_name,
                        last_name, organization_name, organization_number, person_id, `type`,
                        errand_id, municipality_id, namespace)
VALUES (1, '2022-12-02 15:13:45.363', '2022-12-02 15:15:01.563', 0, 'AD-1', NULL, 'John',
        'Doe', NULL, NULL, 'd7af5f83-166a-468b-ab86-da8ca30ea97c', 'PERSON', 1, '2281',
        'my.namespace'),
       (2, '2022-12-02 15:13:45.363', '2022-12-02 15:15:01.563', 0, 'AD-2', NULL, 'Kalle',
        'Anka', NULL, NULL, 'd1af5f83-166a-468b-ab86-da8ca30ea97c', 'PERSON', 1, '2281',
        'my.namespace'),
       (6, '2022-12-02 15:13:45.363', '2022-12-02 15:15:01.563', 0, 'AD-3', NULL, 'Kajsa',
        'Anka', NULL, NULL, 'd2af5f83-166a-468b-ab86-da8ca30ea97c', 'PERSON', 1, '2281',
        'my.namespace');

INSERT INTO stakeholder_roles(stakeholder_id, roles, role_order)
VALUES (1, 'APPLICANT', 0),
       (2, 'APPLICANT', 0),
       (6, 'CONTROL_OFFICIAL', 0);


INSERT INTO decision(version, created, decided_at, decided_by_id, errand_id, id, updated,
                     valid_from, valid_to, description, decision_outcome, decision_type,
                     municipality_id, namespace)
VALUES (1, '2024-01-01 12:00:00.000', '2024-01-01 12:00:00.000', 2, 1, 3,
        '2024-01-01 12:00:00.000', '2024-01-01 12:00:00.000', '2025-01-01 12:00:00.000',
        'Some description', 'APPROVAL', 'RECOMMENDED', '2281', 'my.namespace');

INSERT INTO facility (id, created, updated, version, address_category, apartment_number, attention,
                      care_of, city, country, house_number, invoice_marking, is_zoning_plan_area,
                      latitude, longitude, postal_code, property_designation, street, description,
                      facility_collection_name, main_facility, errand_id, facility_type,
                      municipality_id, namespace)
VALUES (1, '2024-04-25 15:37:23.525120', '2024-04-26 08:14:14.039373', 2, 'POSTAL_ADDRESS',
        'apartmentNumber 1', 'Attention', 'Cateof 1', 'City 1', 'Country 1', 'HouseNumber 1',
        'invoiceMarking 1', 1, 3.0, 2.0, 'PostalCode 1', 'PropertyDesignation 1', 'Street 1',
        'Description 1', 'FacilityCollectionName 1', 1, 1, 'WORKSHOP_BUILDING', '2281',
        'my.namespace'),
       (4, '2024-04-25 15:37:23.525120', '2024-04-26 08:14:14.039373', 2, 'POSTAL_ADDRESS',
        'apartmentNumber 2', 'Attention', 'Cateof 2', 'City 2', 'Country 2', 'HouseNumber 2',
        'invoiceMarking 2', 1, 3.0, 2.0, 'PostalCode 2', 'PropertyDesignation 2', 'Street 2',
        'Description 2', 'FacilityCollectionName 3', 1, 1, 'WORKSHOP_BUILDING', '2281',
        'my.namespace');

INSERT INTO facility_extra_parameters (facility_id, extra_parameter_value, extra_parameter_key)
VALUES (1, 'string1', 'additionalProp1'),
       (1, 'string2', 'additionalProp2'),
       (1, 'string3', 'additionalProp3');

INSERT INTO note (id, created, updated, version, created_by, `text`, title, updated_by, errand_id,
                  municipality_id, namespace)
VALUES (5, '2023-10-02 15:13:45.363', '2023-10-02 15:13:45.363', 1, 'UNKNOWN', 'TEXT', 'TITLE-1',
        'testUser', 1, '2281', 'my.namespace');

INSERT INTO jv_global_id(global_id_pk, local_id, fragment, type_name, owner_id_fk)
VALUES (1, 1, null, 'se.sundsvall.casedata.integration.db.model.ErrandEntity', null),
       (2, 2, null, 'se.sundsvall.casedata.integration.db.model.AttachmentEntity', null),
       (3, 3, null, 'se.sundsvall.casedata.integration.db.model.DecisionEntity', null),
       (4, 4, null, 'se.sundsvall.casedata.integration.db.model.FacilityEntity', null),
       (5, 5, null, 'se.sundsvall.casedata.integration.db.model.NoteEntity', null),
       (6, 6, null, 'se.sundsvall.casedata.integration.db.model.StakeholderEntity', null);

INSERT INTO jv_commit(commit_pk, author, commit_date, commit_date_instant, commit_id)
VALUES (1, 'ANDSOD', '2024-01-25 12:00:00.000', '2024-01-25T12:00:00.000000000Z', 1),
       (2, 'ANDSOD', '2024-01-25 12:00:00.000', '2024-01-25T12:00:00.000000000Z', 2),
       (3, 'ANDSOD', '2024-01-25 12:00:00.000', '2024-01-25T12:00:00.000000000Z', 3),
       (4, 'ANDSOD', '2024-01-25 12:00:00.000', '2024-01-25T12:00:00.000000000Z', 4),
       (5, 'ANDSOD', '2024-01-25 12:00:00.000', '2024-01-25T12:00:00.000000000Z', 5),
       (6, 'ANDSOD', '2024-01-25 12:00:00.000', '2024-01-25T12:00:00.000000000Z', 6);

INSERT INTO jv_snapshot(snapshot_pk, type, version, state, changed_properties, managed_type,
                        global_id_fk, commit_fk)
VALUES (1, 'INITIAL', 1, '{"phase": "Aktualisering",
        "attachments": [],
        "notes": [],
        "errandNumber": "PRH-2022-000001",
        "messageIds": [],
        "municipalityId": "2281",
        "description": "",
        "extraParameters": {
          "disability.walkingAbility": "true",
          "application.applicant.testimonial": "true",
          "consent.view.transportationServiceDetails": "false",
          "disability.aid": "Inget",
          "disability.canBeAloneWhileParking": "true",
          "application.role": "SELF",
          "application.applicant.capacity": "DRIVER",
          "application.applicant.signingAbility": "false",
          "disability.walkingDistance.max": "",
          "disability.walkingDistance.beforeRest": "",
          "consent.contact.doctor": "false",
          "application.reason": "",
          "disability.canBeAloneWhileParking.note": "",
          "disability.duration": "P6M"
        },
        "stakeholders": [
          {
            "entity": "se.sundsvall.casedata.integration.db.model.StakeholderEntity",
            "cdoId": 1
          }
        ],
        "priority": "MEDIUM",
        "caseType": "PARKING_PERMIT",
        "diaryNumber": "",
        "caseTitleAddition": "Nytt parkeringstillstånd",
        "statuses": [
          {
            "valueObject": "se.sundsvall.casedata.integration.db.model.StatusEntity",
            "ownerId": {
              "entity": "se.sundsvall.casedata.integration.db.model.ErrandEntity",
              "cdoId": 1
            },
            "fragment": "statuses/0"
          }
        ],
        "decisions": [],
        "facilities": []
      }',
        '["phase",
          "municipalityId",
          "attachments",
          "notes",
          "errandNumber",
          "messageIds",
          "description",
          "extraParameters",
          "stakeholders",
          "priority",
          "caseType",
          "diaryNumber",
          "caseTitleAddition",
          "statuses",
          "decisions",
          "facilities"
        ]',
        'se.sundsvall.casedata.integration.db.model.ErrandEntity',
        1, 1),
       (2, 'INITIAL', 1, '{ "note": "",
        "extension": "pdf",
        "file": "JVBERi0xLjUKJeLjz9",
        "name": "beslut-arende-PRH-2022-000001",
        "extraParameters": {},
        "mimeType": "application/pdf",
        "municipalityId": "2281",
        "category": "BESLUT"}',
        '["note",'
            '"extension",'
            '"file",'
            '"municicpalityId",'
            '"name",'
            '"extraParameters",'
            '"mimeType",'
            '"category"]',
        'se.sundsvall.casedata.integration.db.model.AttachmentEntity', 2, 2),
       (3, 'INITIAL', 1,
        '{"law": [{
      "valueObject": "se.sundsvall.casedata.integration.db.model.LawEntity",
      "ownerId": {
        "entity": "se.sundsvall.casedata.integration.db.model.DecisionEntity",
        "cdoId": 2
      },
      "fragment": "law/0"
    }
   ],
   "attachments": [
    {
      "entity": "se.sundsvall.casedata.integration.db.model.AttachmentEntity",
      "cdoId": 1
    }
   ],
   "errand": {
    "entity": "se.sundsvall.casedata.integration.db.model.ErrandEntity",
    "cdoId": 1
   },
   "decidedAt": "2022-12-02T14:14:39.944Z",
   "description": "\u003cp\u003eBifalles.\u003c/p\u003e",
   "extraParameters": {},
   "decisionType": "PROPOSED",
   "decisionOutcome": "APPROVAL",
   "validFrom": "2022-12-02T14:14:39.943Z",
   "validTo": "2022-12-02T14:14:39.944Z"}', '[
  "law",
  "municipalityId",
  "attachments",
  "errand",
  "decidedAt","description","extraParameters","decisionType","decisionOutcome","validFrom","validTo"]',
        'se.sundsvall.casedata.integration.db.model.DecisionEntity', 3, 3),
       (4, 'INITIAL', 1,
        '{
     "address": {
       "valueObject": "se.sundsvall.casedata.integration.db.model.AddressEntity",
       "ownerId": {
         "entity": "se.sundsvall.casedata.integration.db.model.FacilityEntity",
         "cdoId": 2
       },
       "fragment": "address"
     },
     "facilityType": "ONE_FAMILY_HOUSE",
     "errand": {
       "entity": "se.sundsvall.casedata.integration.db.model.ErrandEntity",
       "cdoId": 406
     },
     "mainFacility": true,
     "municipalityId": "2281",
     "description": "En fritextbeskrivning av facility.",
     "extraParameters": {
       "additionalProp1": "string",
       "additionalProp3": "string",
       "additionalProp2": "string"
     },
     "facilityCollectionName": "Sundsvalls testfabrik"
   }',
        '[
     "address",
     "facilityType",
     "errand",
     "mainFacility",
     "description",
     "municipalityId",
     "extraParameters",
     "facilityCollectionName"
   ]', 'se.sundsvall.casedata.integration.db.model.FacilityEntity', 4, 4),
       (5, 'INITIAL', 1, '{
     "errand": {
       "entity": "se.sundsvall.casedata.integration.db.model.ErrandEntity",
       "cdoId": 416
     },
     "extraParameters": {},
     "text": "\u003cp\u003eqqqqqqqqqqqqqqqq\u003c/p\u003e",
     "municipalityId": "2281",
     "title": "w"
   }', '[
  "errand",
  "extraParameters",
  "municipalityId",
  "text",
  "title"
]', 'se.sundsvall.casedata.integration.db.model.NoteEntity', 5, 5),
       (6, 'INITIAL', 1, '{
  "firstName": "Kalle",
  "lastName": "Anka",
  "municipalityId": "2281",
  "addresses": [
    {
      "valueObject": "se.sundsvall.casedata.integration.db.model.AddressEntity",
      "ownerId": {
        "entity": "se.sundsvall.casedata.integration.db.model.StakeholderEntity",
        "cdoId": 246
      },
      "fragment": "addresses/0"
    }
  ],
  "contactInformation": [
    {
      "valueObject": "se.sundsvall.casedata.integration.db.model.ContactInformationEntity",
      "ownerId": {
        "entity": "se.sundsvall.casedata.integration.db.model.StakeholderEntity",
        "cdoId": 246
      },
      "fragment": "contactInformation/0"
    },
    {
      "valueObject": "se.sundsvall.casedata.integration.db.model.ContactInformationEntity",
      "ownerId": {
        "entity": "se.sundsvall.casedata.integration.db.model.StakeholderEntity",
        "cdoId": 246
      },
      "fragment": "contactInformation/1"
    }
  ],
  "errand": {
    "entity": "se.sundsvall.casedata.integration.db.model.ErrandEntity",
    "cdoId": 90
  },
  "roles": [
    "APPLICANT"
  ],
  "extraParameters": {},
  "personId": "d7af5383-166a-468b-ab86-da8ccd0ea97c",
  "type": "PERSON"
}', '[
  "firstName",
  "lastName",
  "addresses",
  "contactInformation",
  "errand",
  "roles",
  "extraParameters",
  "municipalityId",
  "personId",
  "type"
]', 'se.sundsvall.casedata.integration.db.model.StakeholderEntity', 6, 6);
