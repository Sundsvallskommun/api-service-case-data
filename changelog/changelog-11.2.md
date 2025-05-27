# API-Changelog: version 11.2

## API-endpoints

### Messages:

#### New endpoints:

```
- [GET] /{municipalityId}/{namespace}/errands/{errandId}/communication/conversations
- [POST] /{municipalityId}/{namespace}/errands/{errandId}/communication/conversations
- [GET] /{municipalityId}/{namespace}/errands/{errandId}/communication/conversations/{conversationId}
- [PATCH] /{municipalityId}/{namespace}/errands/{errandId}/communication/conversations/{conversationId}
- [GET] /{municipalityId}/{namespace}/errands/{errandId}/communication/conversations/{conversationId}/messages
- [POST] /{municipalityId}/{namespace}/errands/{errandId}/communication/conversations/{conversationId}/messages
```

## API-Model updates

- **Conversation** *(Added)*
  - **Fields:**
    - externalReferences: `array`
    - id: `string`
    - metadata: `array`
    - participants: `array`
    - relationIds: `array`
    - topic: `string`
    - type: `object`
- **ConversationType** *(Added)*
  - No fields
- **Identifier** *(Added)*
  - **Fields:**
    - type: `string`
    - value: `string`
- **KeyValues** *(Added)*
  - **Fields:**
    - key: `string`
    - values: `array`
- **Message** *(Added)*
  - **Fields:**
    - attachments: `array`
    - content: `string`
    - created: `string`
    - createdBy: `object`
    - id: `string`
    - inReplyToMessageId: `string`
    - readBy: `array`
- **ReadBy** *(Added)*
  - **Fields:**
    - identifier: `object`
    - readAt: `string`

