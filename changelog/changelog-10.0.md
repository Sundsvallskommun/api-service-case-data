# API-Changelog: version 10.0

## API-endpoints

### Appeals:

#### Removed endpoints:

- [GET] /{municipalityId}/{namespace}/errands/{errandId}/appeals
- [PATCH] /{municipalityId}/{namespace}/errands/{errandId}/appeals
- [DELETE] /{municipalityId}/{namespace}/errands/{errandId}/appeals/{appealId}
- [GET] /{municipalityId}/{namespace}/errands/{errandId}/appeals/{appealId}
- [PATCH] /{municipalityId}/{namespace}/errands/{errandId}/appeals/{appealId}
- [PUT] /{municipalityId}/{namespace}/errands/{errandId}/appeals/{appealId}

### Attachments:

#### New endpoints:

- [GET] /{municipalityId}/{namespace}/errands/{errandId}/attachments

#### Removed endpoints:

- [GET] /{municipalityId}/{namespace}/attachments/errand/{errandNumber}

### MessageAttachments:

#### Removed endpoints:

- [GET] /{municipalityId}/{namespace}/errands/{errandId}/messageattachments/{attachmentId}/streamed

### Messages:

#### New endpoints:

- [GET] /{municipalityId}/{namespace}/errands/{errandId}/messages
- [GET] /{municipalityId}/{namespace}/errands/{errandId}/messages/{messageId}
- [GET] /{municipalityId}/{namespace}/errands/{errandId}/messages/{messageId}/attachments/{attachmentId}

#### Removed endpoints:

- [GET] /{municipalityId}/{namespace}/messages/{errandNumber}

### Notifications:

#### New endpoints:

- [GET] /{municipalityId}/{namespace}/errands/{errandId}/notifications
- [POST] /{municipalityId}/{namespace}/errands/{errandId}/notifications
- [DELETE] /{municipalityId}/{namespace}/errands/{errandId}/notifications/{notificationId}
- [GET] /{municipalityId}/{namespace}/errands/{errandId}/notifications/{notificationId}

#### Removed endpoints:

- [POST] /{municipalityId}/{namespace}/notifications
- [DELETE] /{municipalityId}/{namespace}/notifications/{notificationId}
- [GET] /{municipalityId}/{namespace}/notifications/{notificationId}

### Parking permit:

#### Removed endpoints:

- [GET] /{municipalityId}/{namespace}/parking-permits

## API-Model updates

- **Facility**
  - **Fields with Changed Types:**
    - id: `long` -> `Long`
- **MessageResponse**
  - **Added Fields:**
    - errandId: `Long`
    - recipients: `List<String>`
  - **Removed Fields:**
    - errandNumber: `String`
- **Stakeholder**
  - **Fields with Changed Types:**
    - id: `long` -> `Long`
- **Errand**
  - **Added Fields:**
    - labels: `List<String>`
    - relatesTo: `List<RelatedErrand>`
  - **Removed Fields:**
    - appeals: `List<Appeal>`
  - **Fields with Changed Types:**
    - id: `long` -> `Long`
- **Note**
  - **Fields with Changed Types:**
    - id: `long` -> `Long`
- **MessageRequest**
  - **Added Fields:**
    - attachments: `List<AttachmentRequest>`
    - recipients: `List<String>`
  - **Removed Fields:**
    - attachmentRequests: `List<AttachmentRequest>`
    - errandNumber: `String`
- **PatchNotification**
  - **Added Fields:**
    - errandId: `Long`
- **Attachment**
  - **Added Fields:**
    - errandId: `Long`
  - **Removed Fields:**
    - errandNumber: `String`
  - **Fields with Changed Types:**
    - id: `long` -> `Long`
- **Decision**
  - **Fields with Changed Types:**
    - id: `long` -> `Long`
- **PatchErrand**
  - **Added Fields:**
    - labels: `List<String>`
    - relatesTo: `List<RelatedErrand>`
- **ValidAppealStatusConstraintValidator** *(Renamed to UniqueDecisionTypeValidator)*
- **RelatedErrand** *(Added)*
  - **Fields:**
    - errandId: `Long`
    - errandNumber: `String`
    - relationReason: `String`
- **PatchAppeal** *(Removed)*
  - **Fields:**
    - description: `String`
    - status: `String`
    - timelinessReview: `String`
- **Appeal** *(Removed)*
  - **Fields:**
    - appealConcernCommunicatedAt: `OffsetDateTime`
    - created: `OffsetDateTime`
    - decisionId: `Long`
    - description: `String`
    - id: `long`
    - municipalityId: `String`
    - namespace: `String`
    - registeredAt: `OffsetDateTime`
    - status: `String`
    - timelinessReview: `String`
    - updated: `OffsetDateTime`
    - version: `int`
- **GetParkingPermit** *(Removed)*
  - **Fields:**
    - artefactPermitNumber: `String`
    - artefactPermitStatus: `String`
    - errandDecision: `Decision`
    - errandId: `Long`

