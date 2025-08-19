# API Changelog 11.2 vs. 11.7

## GET /{municipalityId}/{namespace}/errands
- :warning: added the new 'ESERVICE_KATLA' enum value to the 'content/items/channel' response property for the response status '200'
-  the 'query' request parameter 'filter' became optional


## POST /{municipalityId}/{namespace}/errands
-  added the new 'ESERVICE_KATLA' enum value to the request property 'channel'


## GET /{municipalityId}/{namespace}/errands/{errandId}
- :warning: added the new 'ESERVICE_KATLA' enum value to the 'channel' response property for the response status '200'


## GET /{municipalityId}/{namespace}/errands/{errandId}/communication/conversations
- :warning: removed the optional property '/items/externalReferences' from the response with the '200' status


## POST /{municipalityId}/{namespace}/errands/{errandId}/communication/conversations
- :warning: removed the request property 'externalReferences'


## GET /{municipalityId}/{namespace}/errands/{errandId}/communication/conversations/{conversationId}
- :warning: removed the optional property 'externalReferences' from the response with the '200' status


## PATCH /{municipalityId}/{namespace}/errands/{errandId}/communication/conversations/{conversationId}
- :warning: removed the request property 'externalReferences'
- :warning: removed the optional property 'externalReferences' from the response with the '200' status


## GET /{municipalityId}/{namespace}/errands/{errandId}/communication/conversations/{conversationId}/messages
- :warning: the 'content/items/attachments/items/id' response's property type/format changed from 'integer'/'int64' to 'string'/'' for status '200'
- :warning: removed the optional property 'content/items/attachments/items/category' from the response with the '200' status
- :warning: removed the optional property 'content/items/attachments/items/errandId' from the response with the '200' status
- :warning: removed the optional property 'content/items/attachments/items/extension' from the response with the '200' status
- :warning: removed the optional property 'content/items/attachments/items/extraParameters' from the response with the '200' status
- :warning: removed the optional property 'content/items/attachments/items/file' from the response with the '200' status
- :warning: removed the optional property 'content/items/attachments/items/municipalityId' from the response with the '200' status
- :warning: removed the optional property 'content/items/attachments/items/name' from the response with the '200' status
- :warning: removed the optional property 'content/items/attachments/items/namespace' from the response with the '200' status
- :warning: removed the optional property 'content/items/attachments/items/note' from the response with the '200' status
- :warning: removed the optional property 'content/items/attachments/items/updated' from the response with the '200' status
- :warning: removed the optional property 'content/items/attachments/items/version' from the response with the '200' status
-  added the optional property 'content/items/attachments/items/fileName' to the response with the '200' status
-  added the optional property 'content/items/attachments/items/fileSize' to the response with the '200' status
-  added the optional property 'content/items/type' to the response with the '200' status
-  the response optional property 'content/items/attachments/items/created' became not read-only for the status '200'
-  the response optional property 'content/items/attachments/items/id' became not read-only for the status '200'


## POST /{municipalityId}/{namespace}/errands/{errandId}/communication/conversations/{conversationId}/messages
- :warning: removed the request property 'message/attachments/items/category'
- :warning: removed the request property 'message/attachments/items/extension'
- :warning: removed the request property 'message/attachments/items/extraParameters'
- :warning: removed the request property 'message/attachments/items/file'
- :warning: removed the request property 'message/attachments/items/name'
- :warning: removed the request property 'message/attachments/items/note'
-  added the new optional request property 'message/attachments/items/fileName'
-  added the new optional request property 'message/attachments/items/fileSize'
-  the request optional property 'message/attachments/items/created' became not read-only
-  the request optional property 'message/attachments/items/id' became not read-only
-  the 'message/attachments/items/id' request property type/format was generalized from 'integer'/'int64' to 'string'/''


## GET /{municipalityId}/{namespace}/errands/{errandId}/communication/conversations/{conversationId}/messages/{messageId}/attachments/{attachmentId}
-  endpoint added

