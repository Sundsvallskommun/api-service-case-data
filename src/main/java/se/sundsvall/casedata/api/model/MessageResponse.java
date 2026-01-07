package se.sundsvall.casedata.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static jakarta.persistence.EnumType.STRING;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Enumerated;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import se.sundsvall.casedata.integration.db.model.enums.Classification;
import se.sundsvall.casedata.integration.db.model.enums.Direction;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class MessageResponse {

	@Schema(description = "The message ID", examples = "12")
	private String messageId;

	@Schema(description = "The errand ID", examples = "123")
	private Long errandId;

	@Schema(description = "The municipality ID", examples = "2281")
	private String municipalityId;

	@Schema(description = "Namespace", examples = "MY_NAMESPACE")
	private String namespace;

	@Enumerated(STRING)
	@Schema(description = "If the message is inbound or outbound from the perspective of case-data/e-service.", examples = "INBOUND")
	private Direction direction;

	@Schema(description = "The E-service ID that the message was created in", examples = "12")
	private String familyId;

	@Schema(description = "OpenE caseID", examples = "12")
	private String externalCaseId;

	@Schema(description = "The message", examples = "Hello world")
	private String message;

	@Schema(description = "The message in HTML format", examples = "<p>Hello world</p>")
	private String htmlMessage;

	@Schema(description = "The time the message was sent", examples = "2020-01-01 12:00:00")
	private String sent;

	@Schema(description = "The email-subject of the message", examples = "Hello world")
	private String subject;

	@Schema(description = "The username of the user that sent the message", examples = "username")
	private String username;

	@Schema(description = "The first name of the user that sent the message", examples = "Kalle")
	private String firstName;

	@Schema(description = "The last name of the user that sent the message", examples = "Anka")
	private String lastName;

	@Schema(description = "The message was delivered by", examples = "EMAIL")
	private String messageType;

	@Schema(description = "The mobile number of the recipient", examples = "+46701740605")
	private String mobileNumber;

	@Schema(description = "The recipients of the message, if email", examples = "[\"kalle.anka@ankeborg.se\"]")
	private List<String> recipients;

	@Schema(description = "The email of the user that sent the message", examples = "kalle.anka@ankeborg.se")
	private String email;

	@Schema(description = "The user ID of the user that sent the message", examples = "12")
	private String userId;

	@Schema(description = "Signal if the message has been viewed or not", examples = "true")
	private Boolean viewed;

	@Schema(description = "The classification of the message")
	private Classification classification;

	@Schema(description = "List of attachments on the message")
	private List<AttachmentResponse> attachments;

	@Schema(description = "List of email headers on the message")
	private List<EmailHeader> emailHeaders;

	@Schema(description = "Is message internal", examples = "true")
	private Boolean internal;

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	@Builder(setterPrefix = "with")
	@ToString
	@EqualsAndHashCode
	public static class AttachmentResponse {

		@Schema(description = "The attachment ID", examples = "aGVsbG8gd29ybGQK", requiredMode = REQUIRED)
		private String attachmentId;

		@Schema(description = "The attachment filename", examples = "test.txt", requiredMode = REQUIRED)
		private String name;

		@Schema(description = "The attachment content type", examples = "text/plain")
		private String contentType;
	}
}
