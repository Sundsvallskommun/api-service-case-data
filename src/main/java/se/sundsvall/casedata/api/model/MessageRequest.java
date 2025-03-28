package se.sundsvall.casedata.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sundsvall.casedata.api.model.validation.ValidMessageType;
import se.sundsvall.casedata.integration.db.model.enums.Classification;
import se.sundsvall.casedata.integration.db.model.enums.Direction;
import se.sundsvall.dept44.common.validators.annotation.ValidBase64;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class MessageRequest {

	@Schema(description = "The message ID", example = "12")
	private String messageId;

	@Schema(description = "If the message is inbound or outbound from the perspective of case-data/e-service.", example = "INBOUND")
	private Direction direction;

	@Schema(description = "The E-service ID that the message was created in", example = "12")
	private String familyId;

	@Schema(description = "OpenE caseID", example = "12")
	private String externalCaseId;

	@Schema(description = "The message", example = "Hello world")
	private String message;

	@Schema(description = "The time the message was sent", example = "2020-01-01 12:00:00")
	private String sent;

	@Schema(description = "The email-subject of the message", example = "Hello world")
	private String subject;

	@Schema(description = "The username of the user that sent the message", example = "username")
	private String username;

	@Schema(description = "The first name of the user that sent the message", example = "Kalle")
	private String firstName;

	@Schema(description = "The last name of the user that sent the message", example = "Anka")
	private String lastName;

	@ValidMessageType
	@Schema(description = "The message was delivered by", example = "EMAIL")
	private String messageType;

	@Schema(description = "The mobile number of the recipient", example = "+46701234567")
	private String mobileNumber;

	@Schema(description = "The email of the user that sent the message", example = "kalle.anka@ankeborg.se")
	private String email;

	@Schema(description = "List of email recipients", example = "[\"kalle.anka@ankeborg.se\"]")
	private List<String> recipients;

	@Schema(description = "The user ID of the user that sent the message", example = "12")
	private String userId;

	@Schema(description = "The classification of the message")
	private Classification classification;

	@Schema(description = "List of attachments on the message")
	private List<AttachmentRequest> attachments;

	@Schema(description = "List of email headers on the message")
	private List<EmailHeader> emailHeaders;

	@Schema(description = "Is message internal", example = "true")
	private Boolean internal;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	@Builder(setterPrefix = "with")
	@Schema(name = "MessageAttachment", description = "MessageResponse")
	public static class AttachmentRequest {

		@NotBlank
		@ValidBase64
		@Schema(description = "The attachment (file) content as a BASE64-encoded string", example = "aGVsbG8gd29ybGQK", requiredMode = REQUIRED)
		private String content;

		@NotBlank
		@Schema(description = "The attachment filename", example = "test.txt", requiredMode = REQUIRED)
		private String name;

		@Schema(description = "The attachment content type", example = "text/plain")
		private String contentType;
	}
}
