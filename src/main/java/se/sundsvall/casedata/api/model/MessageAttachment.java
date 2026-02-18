package se.sundsvall.casedata.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sundsvall.dept44.common.validators.annotation.ValidBase64;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class MessageAttachment {

	@NotBlank
	@Schema(description = "The attachment ID", examples = "12345678-1234-1234-1234-123456789012", requiredMode = REQUIRED)
	private String attachmentId;

	@Schema(description = "The municipality ID", examples = "2281", accessMode = READ_ONLY)
	private String municipalityId;

	@Schema(description = "Namespace", examples = "MY_NAMESPACE", accessMode = READ_ONLY)
	private String namespace;

	@NotBlank
	@Schema(description = "The attachment filename", examples = "test.txt", requiredMode = REQUIRED)
	private String name;

	@Schema(description = "The attachment content type", examples = "text/plain")
	private String contentType;

	@ValidBase64
	@Schema(description = "The attachment (file) content as a BASE64-encoded string", examples = "aGVsbG8gd29ybGQK", requiredMode = REQUIRED)
	private String content;
}
