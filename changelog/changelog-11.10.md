# API-Changelog: version 11.10

## API-endpoints

### Errands extra parameters:

#### New endpoints:

```
- [GET] /{municipalityId}/{namespace}/errands/{errandId}/extraparameters
- [PATCH] /{municipalityId}/{namespace}/errands/{errandId}/extraparameters
- [DELETE] /{municipalityId}/{namespace}/errands/{errandId}/extraparameters/{parameterKey}
- [GET] /{municipalityId}/{namespace}/errands/{errandId}/extraparameters/{parameterKey}
- [PATCH] /{municipalityId}/{namespace}/errands/{errandId}/extraparameters/{parameterKey}
```

## API-Model updates

- **ExtraParameter**
  - **Added Fields:**
    - id: `string`

