# API-Changelog: version 11.1

## API-endpoints

### Messages:

#### New endpoints:

```
- [GET] /{municipalityId}/{namespace}/errands/{errandId}/messages/external
```

## API-Model updates

- **MessageRequest**
  - **Added Fields:**
    - internal: `boolean`
- **MessageResponse**
  - **Added Fields:**
    - internal: `boolean`
- **Notification**
  - **Added Fields:**
    - subType: `string`

