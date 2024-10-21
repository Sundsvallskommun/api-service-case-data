package se.sundsvall.casedata.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;
import se.sundsvall.casedata.api.model.validation.ValidAttachmentCategory;
import se.sundsvall.casedata.api.model.validation.ValidMapValueSize;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class Attachment {

	@Schema(description = "The id of the attachment", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
	private long id;

	@Schema(description = "The version of the attachment", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
	private int version;

	@Schema(description = "The municipality ID", example = "2281", accessMode = Schema.AccessMode.READ_ONLY)
	private String municipalityId;

	@Schema(description = "Namespace", example = "my.namespace", accessMode = Schema.AccessMode.READ_ONLY)
	private String namespace;

	@Schema(description = "The date when this attachment was created", accessMode = Schema.AccessMode.READ_ONLY, example = "2023-10-01T12:00:00Z")
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime created;

	@Schema(description = "The date when this attachment was last updated", accessMode = Schema.AccessMode.READ_ONLY, example = "2023-10-02T12:00:00Z")
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime updated;

	@Schema(description = "Category of the attachment", example = "DOCUMENT")
	@ValidAttachmentCategory
	private String category;

	@Schema(description = "Name of the attachment", example = "Test Document")
	private String name;

	@Schema(description = "Note about the attachment", example = "This is a test document.")
	private String note;

	@Schema(description = "File extension of the attachment", example = "pdf")
	private String extension;

	@Schema(description = "MIME type of the attachment", example = "application/pdf")
	private String mimeType;

	@Schema(description = "Base64 encoded file content", example = "dGVzdCBjb250ZW50")
	private String file;

	@Schema(description = "Errand number associated with the attachment", example = "ERR123456")
	private String errandNumber;

	@Schema(description = "Additional parameters for the attachment", example = "{\"key1\": \"value1\", \"key2\": \"value2\"}")
	@ValidMapValueSize(max = 8192)
	@Builder.Default
	private Map<String, String> extraParameters = new HashMap<>();

}
