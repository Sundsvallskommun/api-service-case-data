# API-Changelog: version 11.0

## API-endpoints

### Notifications:

#### New endpoints:

```
- [PUT] /{municipalityId}/{namespace}/errands/{errandId}/notifications/global-acknowledged
```

### Status:

#### New endpoints:

```
- [PATCH] /{municipalityId}/{namespace}/errands/{errandId}/status
```

#### Removed endpoints:

```
- [PATCH] /{municipalityId}/{namespace}/errands/{errandId}/statuses
- [PUT] /{municipalityId}/{namespace}/errands/{errandId}/statuses
```

## API-Model updates

- **Errand**
  - **Added Fields:**
    - notifications: `List<Notification>`
    - status: `Status`
- **Notification**
  - **Added Fields:**
    - globalAcknowledged: `boolean`
- **PatchErrand**
  - **Added Fields:**
    - status: `Status`
- **Status**
  - **Added Fields:**
    - created: `OffsetDateTime`
  - **Removed Fields:**
    - dateTime: `OffsetDateTime`
- **PatchNotification**
  - **Added Fields:**
    - globalAcknowledged: `Boolean`

