INSERT INTO jv_global_id(global_id_pk, local_id, fragment, type_name, owner_id_fk)
VALUES (1, 1, null, 'se.sundsvall.casedata.integration.db.model.Errand', null),
       (2, 2, null, 'se.sundsvall.casedata.integration.db.model.Attachment', null),
       (3, 3, null, 'se.sundsvall.casedata.integration.db.model.Decision', null),
       (4, 4, null, 'se.sundsvall.casedata.integration.db.model.Facility', null),
       (5, 5, null, 'se.sundsvall.casedata.integration.db.model.Note', null),
       (6, 6, null, 'se.sundsvall.casedata.integration.db.model.Stakeholder', null);

INSERT INTO jv_commit(commit_pk, author, commit_date, commit_date_instant, commit_id)
VALUES (1, 'ANDSOD', '2024-01-25 12:00:00.000', '2024-01-25T12:00:00.000000000Z', 1),
       (2, 'ANDSOD', '2024-01-25 12:00:00.000', '2024-01-25T12:00:00.000000000Z', 2),
       (3, 'ANDSOD', '2024-01-25 12:00:00.000', '2024-01-25T12:00:00.000000000Z', 3),
       (4, 'ANDSOD', '2024-01-25 12:00:00.000', '2024-01-25T12:00:00.000000000Z', 4),
       (5, 'ANDSOD', '2024-01-25 12:00:00.000', '2024-01-25T12:00:00.000000000Z', 5),
       (6, 'ANDSOD', '2024-01-25 12:00:00.000', '2024-01-25T12:00:00.000000000Z', 6);

INSERT INTO jv_snapshot(snapshot_pk, type, version, state,
                        changed_properties, managed_type, global_id_fk, commit_fk)
VALUES (1, 'INITIAL', 1, '{"phase": "Aktualisering",
        "municipalityId": "1234",
        "attachments": [],
        "notes": [],
        "errandNumber": "PRH-2022-000001",
        "messageIds": [],
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
            "entity": "se.sundsvall.casedata.integration.db.model.Stakeholder",
            "cdoId": 1
          }
        ],
        "priority": "MEDIUM",
        "caseType": "PARKING_PERMIT",
        "diaryNumber": "",
        "caseTitleAddition": "Nytt parkeringstillstånd",
        "statuses": [
          {
            "valueObject": "se.sundsvall.casedata.integration.db.model.Status",
            "ownerId": {
              "entity": "se.sundsvall.casedata.integration.db.model.Errand",
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
        'se.sundsvall.casedata.integration.db.model.Errand',
        1, 1),
       (2, 'INITIAL', 1, '{ "note": "",
        "extension": "pdf",
        "file": "JVBERi0xLjUKJeLjz9",
        "name": "beslut-arende-PRH-2022-000001",
        "extraParameters": {},
        "mimeType": "application/pdf",
        "category": "BESLUT"}',
        '["note",'
            '"extension",'
            '"file",'
            '"name",'
            '"extraParameters",'
            '"mimeType",'
            '"category"]',
        'se.sundsvall.casedata.integration.db.model.Attachment', 2, 2),
       (3, 'INITIAL', 1,
        '{"law": [{
      "valueObject": "se.sundsvall.casedata.integration.db.model.Law",
      "ownerId": {
        "entity": "se.sundsvall.casedata.integration.db.model.Decision",
        "cdoId": 2
      },
      "fragment": "law/0"
    }
  ],
  "attachments": [
    {
      "entity": "se.sundsvall.casedata.integration.db.model.Attachment",
      "cdoId": 1
    }
  ],
  "errand": {
    "entity": "se.sundsvall.casedata.integration.db.model.Errand",
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
  "attachments",
  "errand",
  "decidedAt","description","extraParameters","decisionType","decisionOutcome","validFrom","validTo"]',
        'se.sundsvall.casedata.integration.db.model.Decision', 3, 3),
       (4, 'INITIAL', 1,
        '{
     "address": {
       "valueObject": "se.sundsvall.casedata.integration.db.model.Address",
       "ownerId": {
         "entity": "se.sundsvall.casedata.integration.db.model.Facility",
         "cdoId": 2
       },
       "fragment": "address"
     },
     "facilityType": "ONE_FAMILY_HOUSE",
     "errand": {
       "entity": "se.sundsvall.casedata.integration.db.model.Errand",
       "cdoId": 406
     },
     "mainFacility": true,
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
     "extraParameters",
     "facilityCollectionName"
   ]', 'se.sundsvall.casedata.integration.db.model.Facility', 4, 4),
       (5, 'INITIAL', 1,
        '{
     "errand": {
       "entity": "se.sundsvall.casedata.integration.db.model.Errand",
       "cdoId": 416
     },
     "extraParameters": {},
     "text": "\u003cp\u003eqqqqqqqqqqqqqqqq\u003c/p\u003e",
     "title": "w"
   }', '[
  "errand",
  "extraParameters",
  "text",
  "title"
]', 'se.sundsvall.casedata.integration.db.model.Note', 5, 5),
       (6, 'INITIAL', 1, '{
  "firstName": "Kalle",
  "lastName": "Anka",
  "addresses": [
    {
      "valueObject": "se.sundsvall.casedata.integration.db.model.Address",
      "ownerId": {
        "entity": "se.sundsvall.casedata.integration.db.model.Stakeholder",
        "cdoId": 246
      },
      "fragment": "addresses/0"
    }
  ],
  "contactInformation": [
    {
      "valueObject": "se.sundsvall.casedata.integration.db.model.ContactInformation",
      "ownerId": {
        "entity": "se.sundsvall.casedata.integration.db.model.Stakeholder",
        "cdoId": 246
      },
      "fragment": "contactInformation/0"
    },
    {
      "valueObject": "se.sundsvall.casedata.integration.db.model.ContactInformation",
      "ownerId": {
        "entity": "se.sundsvall.casedata.integration.db.model.Stakeholder",
        "cdoId": 246
      },
      "fragment": "contactInformation/1"
    }
  ],
  "errand": {
    "entity": "se.sundsvall.casedata.integration.db.model.Errand",
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
  "personId",
  "type"
]', 'se.sundsvall.casedata.integration.db.model.Stakeholder', 6, 6);
