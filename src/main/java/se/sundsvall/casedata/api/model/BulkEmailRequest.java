package se.sundsvall.casedata.api.model;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class BulkEmailRequest {

	@NotEmpty
	@ArraySchema(schema = @Schema(description = "Email address for a recipient. One individual email is sent per recipient.", examples = "recipient@recipient.se", requiredMode = REQUIRED))
	private List<@NotBlank @Email String> recipients;

	@NotBlank
	@Schema(description = "Subject", examples = "Subject", requiredMode = REQUIRED)
	private String subject;

	@NotBlank
	@Schema(description = "Message in plain text", examples = "Message in plain text", requiredMode = REQUIRED)
	private String message;

	@Schema(description = "Message in HTML format", examples = "<p>Hello world</p>")
	private String htmlMessage;

	@NotBlank
	@Schema(description = "The department name to use when resolving which messaging settings (i.e. sender address) to use", examples = "CONVERSATION", requiredMode = REQUIRED)
	private String departmentName;

	@Valid
	@ArraySchema(schema = @Schema(description = "List with Base64 encoded email attachments"))
	private List<MessageRequest.AttachmentRequest> attachments;
}
