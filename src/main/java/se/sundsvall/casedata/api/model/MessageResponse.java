package se.sundsvall.casedata.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;

import se.sundsvall.casedata.integration.db.model.enums.Classification;
import se.sundsvall.casedata.integration.db.model.enums.Direction;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MessageResponse {

	@Schema(description = "The message ID", example = "12")
	private String messageID;

	@Schema(description = "The errand number", example = "PRH-2022-000001")
	private String errandNumber;

	@Enumerated(EnumType.STRING)
	@Schema(description = "If the message is inbound or outbound from the perspective of " +
		"case-data/e-service.", example = "INBOUND")
	private Direction direction;

	@Schema(description = "The E-service ID that the message was created in", example = "12")
	private String familyID;

	@Schema(description = "OpenE caseID", example = "12")
	private String externalCaseID;

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

	@Schema(description = "The message was delivered by", example = "EMAIL")
	private String messageType;

	@Schema(description = "The mobile number of the recipient", example = "+46701234567")
	private String mobileNumber;

	@Schema(description = "The email of the user that sent the message", example = "kalle.anka@ankeborg.se")
	private String email;

	@Schema(description = "The user ID of the user that sent the message", example = "12")
	private String userID;

	@Schema(description = "Signal if the message has been viewed or not", example = "true")
	private boolean viewed;

	@Schema(description = "The classification of the message")
	private Classification classification;

	@Schema(description = "List of attachments on the message")
	private List<AttachmentResponse> attachments;

	@Schema(description = "List of email headers on the message")
	private List<EmailHeaderDTO> emailHeaders;

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	@Builder(setterPrefix = "with")
	@ToString
	@EqualsAndHashCode
	public static class AttachmentResponse {

		@NotBlank
		@Schema(
			description = "The attachment ID",
			example = "aGVsbG8gd29ybGQK",
			requiredMode = REQUIRED
		)
		private String attachmentID;

		@NotBlank
		@Schema(
			description = "The attachment filename",
			example = "test.txt",
			requiredMode = REQUIRED
		)
		private String name;

		@Schema(description = "The attachment content type", example = "text/plain")
		private String contentType;

	}

}
