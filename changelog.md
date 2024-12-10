# API-Changelog: api-service-case-data version 9.0

## API-endpoints

### Appeals:

#### New endpoints:

- [GET] /{municipalityId}/{namespace}/errands/{errandId}/appeals
- [PATCH] /{municipalityId}/{namespace}/errands/{errandId}/appeals
- [DELETE] /{municipalityId}/{namespace}/errands/{errandId}/appeals/{appealId}
- [GET] /{municipalityId}/{namespace}/errands/{errandId}/appeals/{appealId}
- [PATCH] /{municipalityId}/{namespace}/errands/{errandId}/appeals/{appealId}
- [PUT] /{municipalityId}/{namespace}/errands/{errandId}/appeals/{appealId}

#### Removed endpoints:

- [GET] /{municipalityId}/appeals/{appealId}
- [PATCH] /{municipalityId}/appeals/{appealId}
- [PUT] /{municipalityId}/appeals/{appealId}

### Attachments:

#### New endpoints:

- [GET] /{municipalityId}/{namespace}/attachments/errand/{errandNumber}
- [POST] /{municipalityId}/{namespace}/errands/{errandId}/attachments
- [DELETE] /{municipalityId}/{namespace}/errands/{errandId}/attachments/{attachmentId}
- [GET] /{municipalityId}/{namespace}/errands/{errandId}/attachments/{attachmentId}
- [PATCH] /{municipalityId}/{namespace}/errands/{errandId}/attachments/{attachmentId}
- [PUT] /{municipalityId}/{namespace}/errands/{errandId}/attachments/{attachmentId}

#### Removed endpoints:

- [POST] /{municipalityId}/attachments
- [GET] /{municipalityId}/attachments/errand/{errandNumber}
- [DELETE] /{municipalityId}/attachments/{attachmentId}
- [GET] /{municipalityId}/attachments/{attachmentId}
- [PATCH] /{municipalityId}/attachments/{attachmentId}
- [PUT] /{municipalityId}/attachments/{attachmentId}

### Decisions:

#### New endpoints:

- [GET] /{municipalityId}/{namespace}/errands/{errandId}/decisions
- [PATCH] /{municipalityId}/{namespace}/errands/{errandId}/decisions
- [DELETE] /{municipalityId}/{namespace}/errands/{errandId}/decisions/{decisionId}
- [GET] /{municipalityId}/{namespace}/errands/{errandId}/decisions/{decisionId}
- [PATCH] /{municipalityId}/{namespace}/errands/{errandId}/decisions/{decisionId}
- [PUT] /{municipalityId}/{namespace}/errands/{errandId}/decisions/{decisionId}

#### Removed endpoints:

- [GET] /{municipalityId}/decisions/{decisionId}
- [PATCH] /{municipalityId}/decisions/{decisionId}
- [PUT] /{municipalityId}/decisions/{decisionId}

### Errands:

#### New endpoints:

- [GET] /{municipalityId}/{namespace}/errands
- [POST] /{municipalityId}/{namespace}/errands
- [GET] /{municipalityId}/{namespace}/errands/{errandId}
- [PATCH] /{municipalityId}/{namespace}/errands/{errandId}

#### Removed endpoints:

- [GET] /{municipalityId}/errands
- [POST] /{municipalityId}/errands
- [GET] /{municipalityId}/errands/{errandId}
- [PATCH] /{municipalityId}/errands/{errandId}
- [PATCH] /{municipalityId}/errands/{errandId}/appeals
- [DELETE] /{municipalityId}/errands/{errandId}/appeals/{appealId}
- [GET] /{municipalityId}/errands/{errandId}/decisions
- [PATCH] /{municipalityId}/errands/{errandId}/decisions
- [DELETE] /{municipalityId}/errands/{errandId}/decisions/{decisionId}
- [GET] /{municipalityId}/errands/{errandId}/facilities
- [POST] /{municipalityId}/errands/{errandId}/facilities
- [PUT] /{municipalityId}/errands/{errandId}/facilities
- [DELETE] /{municipalityId}/errands/{errandId}/facilities/{facilityId}
- [GET] /{municipalityId}/errands/{errandId}/facilities/{facilityId}
- [PATCH] /{municipalityId}/errands/{errandId}/facilities/{facilityId}
- [PATCH] /{municipalityId}/errands/{errandId}/notes
- [DELETE] /{municipalityId}/errands/{errandId}/notes/{noteId}
- [PATCH] /{municipalityId}/errands/{errandId}/stakeholders
- [PUT] /{municipalityId}/errands/{errandId}/stakeholders
- [DELETE] /{municipalityId}/errands/{errandId}/stakeholders/{stakeholderId}
- [PATCH] /{municipalityId}/errands/{errandId}/statuses
- [PUT] /{municipalityId}/errands/{errandId}/statuses

### Facilities:

#### New endpoints:

- [GET] /{municipalityId}/{namespace}/errands/{errandId}/facilities
- [POST] /{municipalityId}/{namespace}/errands/{errandId}/facilities
- [PUT] /{municipalityId}/{namespace}/errands/{errandId}/facilities
- [DELETE] /{municipalityId}/{namespace}/errands/{errandId}/facilities/{facilityId}
- [GET] /{municipalityId}/{namespace}/errands/{errandId}/facilities/{facilityId}
- [PATCH] /{municipalityId}/{namespace}/errands/{errandId}/facilities/{facilityId}

### History:

#### New endpoints:

- [GET] /{municipalityId}/{namespace}/errands/{errandId}/attachments/{attachmentId}/history
- [GET] /{municipalityId}/{namespace}/errands/{errandId}/decisions/{decisionId}/history
- [GET] /{municipalityId}/{namespace}/errands/{errandId}/facilities/{facilityId}/history
- [GET] /{municipalityId}/{namespace}/errands/{errandId}/history
- [GET] /{municipalityId}/{namespace}/errands/{errandId}/notes/{noteId}/history
- [GET] /{municipalityId}/{namespace}/errands/{errandId}/stakeholders/{stakeholderId}/history

#### Removed endpoints:

- [GET] /{municipalityId}/attachments/{attachmentId}/history
- [GET] /{municipalityId}/decisions/{decisionId}/history
- [GET] /{municipalityId}/errands/{errandId}/history
- [GET] /{municipalityId}/facilities/{facilityId}/history
- [GET] /{municipalityId}/notes/{noteId}/history
- [GET] /{municipalityId}/stakeholders/{stakeholderId}/history

### MessageAttachments:

#### New endpoints:

- [GET] /{municipalityId}/{namespace}/errands/{errandId}/messageattachments/{attachmentId}/streamed

#### Removed endpoints:

- [GET] /{municipalityId}/messageattachments/{attachmentId}
- [GET] /{municipalityId}/messageattachments/{attachmentId}/streamed

### Messages:

#### New endpoints:

- [POST] /{municipalityId}/{namespace}/errands/{errandId}/messages
- [PUT] /{municipalityId}/{namespace}/errands/{errandId}/messages/{messageId}/viewed/{isViewed}
- [GET] /{municipalityId}/{namespace}/messages/{errandNumber}

#### Removed endpoints:

- [POST] /{municipalityId}/messages
- [GET] /{municipalityId}/messages/{errandNumber}
- [PUT] /{municipalityId}/messages/{messageId}/viewed/{isViewed}

### Notes:

#### New endpoints:

- [GET] /{municipalityId}/{namespace}/errands/{errandId}/notes
- [PATCH] /{municipalityId}/{namespace}/errands/{errandId}/notes
- [DELETE] /{municipalityId}/{namespace}/errands/{errandId}/notes/{noteId}
- [GET] /{municipalityId}/{namespace}/errands/{errandId}/notes/{noteId}
- [PATCH] /{municipalityId}/{namespace}/errands/{errandId}/notes/{noteId}

#### Removed endpoints:

- [GET] /{municipalityId}/notes/errand/{errandId}
- [DELETE] /{municipalityId}/notes/{noteId}
- [GET] /{municipalityId}/notes/{noteId}
- [PATCH] /{municipalityId}/notes/{noteId}

### Notifications:

#### New endpoints:

- [GET] /{municipalityId}/{namespace}/notifications
- [PATCH] /{municipalityId}/{namespace}/notifications
- [POST] /{municipalityId}/{namespace}/notifications
- [DELETE] /{municipalityId}/{namespace}/notifications/{notificationId}
- [GET] /{municipalityId}/{namespace}/notifications/{notificationId}

### Parking permit:

#### New endpoints:

- [GET] /{municipalityId}/{namespace}/parking-permits

#### Removed endpoints:

- [GET] /{municipalityId}/parking-permits

### Stakeholders:

#### New endpoints:

- [GET] /{municipalityId}/{namespace}/errands/{errandId}/stakeholders
- [PATCH] /{municipalityId}/{namespace}/errands/{errandId}/stakeholders
- [PUT] /{municipalityId}/{namespace}/errands/{errandId}/stakeholders
- [DELETE] /{municipalityId}/{namespace}/errands/{errandId}/stakeholders/{stakeholderId}
- [GET] /{municipalityId}/{namespace}/errands/{errandId}/stakeholders/{stakeholderId}
- [PATCH] /{municipalityId}/{namespace}/errands/{errandId}/stakeholders/{stakeholderId}
- [PUT] /{municipalityId}/{namespace}/errands/{errandId}/stakeholders/{stakeholderId}

#### Removed endpoints:

- [GET] /{municipalityId}/stakeholders
- [GET] /{municipalityId}/stakeholders/{stakeholderId}
- [PATCH] /{municipalityId}/stakeholders/{stakeholderId}
- [PUT] /{municipalityId}/stakeholders/{stakeholderId}

### Status:

#### New endpoints:

- [PATCH] /{municipalityId}/{namespace}/errands/{errandId}/statuses
- [PUT] /{municipalityId}/{namespace}/errands/{errandId}/statuses

## API-Model updates

- **MessageRequest**
  - **Renamed Fields:**
    - familyID (`String`) -> familyId (`String`)
    - userID (`String`) -> userId (`String`)
    - externalCaseID (`String`) -> externalCaseId (`String`)
    - messageID (`String`) -> messageId (`String`)
  - **Fields with Changed Types:**
    - emailHeaders: `List<EmailHeaderDTO>` -> `List<EmailHeader>`
- **AttachmentResponse**
  - **Renamed Fields:**
    - attachmentID (`String`) -> attachmentId (`String`)
- **MessageResponse**
  - **Added Fields:**
    - municipalityId: `String`
    - namespace: `String`
  - **Renamed Fields:**
    - messageID (`String`) -> messageId (`String`)
    - externalCaseID (`String`) -> externalCaseId (`String`)
    - userID (`String`) -> userId (`String`)
    - familyID (`String`) -> familyId (`String`)
  - **Fields with Changed Types:**
    - emailHeaders: `List<EmailHeaderDTO>` -> `List<EmailHeader>`
- **ContactInformationDTO** *(Renamed to ContactInformation)*
- **GetParkingPermitDTO** *(Renamed to GetParkingPermit)*
  - **Fields with Changed Types:**
    - errandDecision: `DecisionDTO` -> `Decision`
- **PatchDecisionDTO** *(Renamed to PatchDecision)*
- **PatchAppealDTO** *(Renamed to PatchAppeal)*
- **EmailHeaderDTO** *(Renamed to EmailHeader)*
- **CoordinatesDTO** *(Renamed to Coordinates)*
- **HistoryDTO** *(Renamed to History)*
- **AddressDTO** *(Renamed to Address)*
  - **Fields with Changed Types:**
    - location: `CoordinatesDTO` -> `Coordinates`
- **PatchErrandDTO** *(Renamed to PatchErrand)*
  - **Added Fields:**
    - suspension: `Suspension`
  - **Fields with Changed Types:**
    - extraParameters: `Map<String, String>` -> `List<ExtraParameter>`
    - facilities: `List<FacilityDTO>` -> `List<Facility>`
- **StatusDTO** *(Renamed to Status)*
- **LawDTO** *(Renamed to Law)*
- **ErrandDTO** *(Renamed to Errand)*
  - **Added Fields:**
    - created: `OffsetDateTime`
    - id: `Long`
    - municipalityId: `String`
    - namespace: `String`
    - suspension: `Suspension`
    - updated: `OffsetDateTime`
    - version: `int`
  - **Fields with Changed Types:**
    - statuses: `List<StatusDTO>` -> `List<Status>`
    - notes: `List<NoteDTO>` -> `List<Note>`
    - appeals: `List<AppealDTO>` -> `List<Appeal>`
    - decisions: `List<DecisionDTO>` -> `List<Decision>`
    - stakeholders: `List<StakeholderDTO>` -> `List<Stakeholder>`
    - facilities: `List<FacilityDTO>` -> `List<Facility>`
    - extraParameters: `Map<String, String>` -> `List<ExtraParameter>`
- **StakeholderDTO** *(Renamed to Stakeholder)*
  - **Added Fields:**
    - created: `OffsetDateTime`
    - id: `Long`
    - municipalityId: `String`
    - namespace: `String`
    - updated: `OffsetDateTime`
    - version: `int`
  - **Fields with Changed Types:**
    - addresses: `List<AddressDTO>` -> `List<Address>`
    - contactInformation: `List<ContactInformationDTO>` -> `List<ContactInformation>`
- **DecisionDTO** *(Renamed to Decision)*
  - **Added Fields:**
    - created: `OffsetDateTime`
    - id: `Long`
    - municipalityId: `String`
    - namespace: `String`
    - updated: `OffsetDateTime`
    - version: `int`
  - **Fields with Changed Types:**
    - decidedBy: `StakeholderDTO` -> `Stakeholder`
    - attachments: `List<AttachmentDTO>` -> `List<Attachment>`
    - law: `List<LawDTO>` -> `List<Law>`
- **AttachmentDTO** *(Renamed to Attachment)*
  - **Added Fields:**
    - created: `OffsetDateTime`
    - id: `Long`
    - municipalityId: `String`
    - namespace: `String`
    - updated: `OffsetDateTime`
    - version: `int`
- **MessageAttachmentDTO** *(Renamed to MessageAttachment)*
  - **Added Fields:**
    - municipalityId: `String`
    - namespace: `String`
  - **Renamed Fields:**
    - attachmentID (`String`) -> attachmentId (`String`)
- **FacilityDTO** *(Renamed to Facility)*
  - **Added Fields:**
    - created: `OffsetDateTime`
    - id: `Long`
    - municipalityId: `String`
    - namespace: `String`
    - updated: `OffsetDateTime`
    - version: `int`
  - **Fields with Changed Types:**
    - address: `AddressDTO` -> `Address`
- **AppealDTO** *(Renamed to Appeal)*
  - **Added Fields:**
    - created: `OffsetDateTime`
    - id: `Long`
    - municipalityId: `String`
    - namespace: `String`
    - updated: `OffsetDateTime`
    - version: `int`
- **NoteDTO** *(Renamed to Note)*
  - **Added Fields:**
    - created: `OffsetDateTime`
    - id: `Long`
    - municipalityId: `String`
    - namespace: `String`
    - updated: `OffsetDateTime`
    - version: `int`
- **PatchNotification** *(Added)*
  - **Fields:**
    - acknowledged: `Boolean`
    - content: `String`
    - description: `String`
    - expires: `OffsetDateTime`
    - id: `String`
    - ownerId: `String`
    - type: `String`
- **ValidSuspensionConstraintValidator** *(Added)*
  - No fields
- **Suspension** *(Added)*
  - **Fields:**
    - suspendedFrom: `OffsetDateTime`
    - suspendedTo: `OffsetDateTime`
- **Notification** *(Added)*
  - **Fields:**
    - acknowledged: `boolean`
    - content: `String`
    - created: `OffsetDateTime`
    - createdBy: `String`
    - createdByFullName: `String`
    - description: `String`
    - errandId: `Long`
    - errandNumber: `String`
    - expires: `OffsetDateTime`
    - id: `String`
    - modified: `OffsetDateTime`
    - municipalityId: `String`
    - namespace: `String`
    - ownerFullName: `String`
    - ownerId: `String`
    - type: `String`
- **ExtraParameter** *(Added)*
  - **Fields:**
    - displayName: `String`
    - key: `String`
    - values: `List<String>`
- **BaseDTO** *(Removed)*
  - **Fields:**
    - created: `OffsetDateTime`
    - id: `Long`
    - updated: `OffsetDateTime`
    - version: `int`
- **ExtraParameterDTO** *(Removed)*
  - **Fields:**
    - extraParameters: `Map<String, String>`

