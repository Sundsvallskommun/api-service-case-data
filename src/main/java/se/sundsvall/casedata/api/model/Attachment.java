package se.sundsvall.casedata.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.TimeZoneStorage;
import se.sundsvall.casedata.api.model.validation.ValidAttachmentCategory;
import se.sundsvall.casedata.api.model.validation.ValidMapValueSize;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class Attachment {

	@Schema(description = "The id of the attachment", accessMode = READ_ONLY, example = "1")
	private Long id;

	@Schema(description = "The version of the attachment", accessMode = READ_ONLY, example = "1")
	private int version;

	@Schema(description = "The municipality ID", example = "2281", accessMode = READ_ONLY)
	private String municipalityId;

	@Schema(description = "Errand id associated with the attachment", accessMode = READ_ONLY, example = "123456")
	private Long errandId;

	@Schema(description = "Namespace", example = "my.namespace", accessMode = READ_ONLY)
	private String namespace;

	@Schema(description = "The date when this attachment was created", accessMode = READ_ONLY, example = "2023-10-01T12:00:00Z")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime created;

	@Schema(description = "The date when this attachment was last updated", accessMode = READ_ONLY, example = "2023-10-02T12:00:00Z")
	@TimeZoneStorage(NORMALIZE)
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

	@Schema(description = "Additional parameters for the attachment", example = "{\"key1\": \"value1\", \"key2\": \"value2\"}")
	@ValidMapValueSize(max = 8192)
	@Builder.Default
	private Map<String, String> extraParameters = new HashMap<>();

}
