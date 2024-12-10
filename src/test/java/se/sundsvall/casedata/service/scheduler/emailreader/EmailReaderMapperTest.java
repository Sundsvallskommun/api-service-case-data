package se.sundsvall.casedata.service.scheduler.emailreader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static se.sundsvall.casedata.api.model.validation.enums.MessageType.EMAIL;
import static se.sundsvall.casedata.integration.db.model.enums.Direction.INBOUND;

import generated.se.sundsvall.emailreader.Email;
import generated.se.sundsvall.emailreader.EmailAttachment;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.integration.db.model.EmailHeaderEntity;
import se.sundsvall.casedata.integration.db.model.enums.Header;

@SpringBootTest(classes = {
	Application.class
}, webEnvironment = MOCK)
@ActiveProfiles("junit")
class EmailReaderMapperTest {

	@Autowired
	private EmailReaderMapper emailReaderMapper;

	@Test
	void toAttachment() {

		// Arrange
		final var email = new Email()
			.id("someId")
			.subject("someSubject")
			.recipients(List.of("someRecipient"))
			.sender("someSender")
			.message("someMessage")
			.receivedAt(OffsetDateTime.now())
			.attachments(List.of(new EmailAttachment()
				.name("someName")
				.content("someContent")
				.contentType("someContentType")));
		final var namespace = "someNamespace";
		final var municipalityId = "someMunicipalityId";

		// Act
		final var result = emailReaderMapper.toAttachments(email, municipalityId, namespace);

		// Assert
		assertThat(result).isNotNull().hasSize(1)
			.element(0).satisfies(attachment -> {
				assertThat(attachment.getName()).isEqualTo("someName");
				assertThat(attachment.getFile()).isEqualTo("someContent");
				assertThat(attachment.getMimeType()).isEqualTo("someContentType");
				assertThat(attachment.getNamespace()).isEqualTo(namespace);
				assertThat(attachment.getMunicipalityId()).isEqualTo(municipalityId);
			});
	}

	@Test
	void toAttachment_withNullEmail() {
		assertThat(emailReaderMapper.toAttachments(null, null, null)).isEmpty();
	}

	@Test
	void toAttachment_withNullEmailAttachments() {

		// Arrange
		final var email = new Email()
			.id("someId")
			.subject("someSubject")
			.recipients(List.of("someRecipient"))
			.sender("someSender")
			.message("someMessage")
			.receivedAt(OffsetDateTime.now());
		final var namespace = "someNamespace";
		final var municipalityId = "someMunicipalityId";
		// Act
		final var result = emailReaderMapper.toAttachments(email, namespace, municipalityId);

		// Assert
		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	void toMessage() {
		// Arrange
		final var messageID = UUID.randomUUID().toString();
		final var namespace = "someNamespace";
		final var municipalityId = "someMunicipalityId";
		final var email = new Email()
			.id(messageID)
			.subject("someSubject")
			.recipients(List.of("someRecipient"))
			.sender("someSender")
			.message("someMessage")
			.receivedAt(OffsetDateTime.now())
			.headers(Map.of(
				Header.MESSAGE_ID.toString(), List.of("<some@message>"),
				Header.REFERENCES.toString(), List.of("<some@message>", "<another@one>"),
				Header.IN_REPLY_TO.toString(), List.of("<some@other>")))
			.attachments(List.of(new EmailAttachment()
				.name("someName")
				.content(Base64.getEncoder().encodeToString("someContent".getBytes()))
				.contentType("someContentType")));

		// Act
		final var result = emailReaderMapper.toMessage(email, municipalityId, namespace);

		// Assert
		assertThat(result)
			.isNotNull()
			.extracting(
				"messageId",
				"direction",
				"subject",
				"textmessage",
				"messageType",
				"email",
				"municipalityId")
			.containsExactlyInAnyOrder(
				email.getId(),
				INBOUND,
				"someSubject",
				"someMessage",
				EMAIL.name(),
				"someSender",
				"someMunicipalityId");

		assertThat(result.getHeaders()).hasSize(3).containsExactlyInAnyOrder(
			EmailHeaderEntity.builder()
				.withHeader(Header.MESSAGE_ID)
				.withValues(List.of("<some@message>"))
				.build(),
			EmailHeaderEntity.builder()
				.withHeader(Header.REFERENCES)
				.withValues(List.of("<some@message>", "<another@one>"))
				.build(),
			EmailHeaderEntity.builder()
				.withHeader(Header.IN_REPLY_TO)
				.withValues(List.of("<some@other>"))
				.build());

		assertThat(result.getSent())
			.isEqualTo(email.getReceivedAt()
				.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

		assertThat(result.getMessageId()).isNotNull()
			.satisfies(id -> assertThat(isValidUUID(id)).isTrue());

		assertThat(result.getAttachments()).isNotNull().hasSize(1)
			.element(0).satisfies(attachment -> {
				assertThat(attachment.getAttachmentId()).isNotNull()
					.satisfies(id -> assertThat(isValidUUID(id)).isTrue());
				assertThat(attachment.getName()).isEqualTo("someName");
				assertThat(attachment.getMessageID()).isEqualTo(messageID);
				assertThat(attachment.getAttachmentData().getFile().getBinaryStream().readAllBytes()).isEqualTo("someContent".getBytes());
				assertThat(attachment.getContentType()).isEqualTo("someContentType");
			});
	}

	@Test
	void toMessage_withNullEmail() {
		assertThat(emailReaderMapper.toMessage(null, null, null)).isNull();
	}

	@Test
	void toMessage_withNullAttachments() {

		// Arrange
		final var municipalityId = "someMunicipalityId";
		final var namespace = "someNamespace";
		final var email = new Email()
			.id(UUID.randomUUID().toString())
			.subject("someSubject")
			.recipients(List.of("someRecipient"))
			.sender("someSender")
			.message("someMessage")
			.receivedAt(OffsetDateTime.now());

		// Act
		final var result = emailReaderMapper.toMessage(email, municipalityId, namespace);

		// Assert
		assertThat(result)
			.isNotNull()
			.extracting(
				"messageId",
				"direction",
				"subject",
				"textmessage",
				"messageType",
				"email",
				"municipalityId")
			.containsExactlyInAnyOrder(
				email.getId(),
				INBOUND,
				"someSubject",
				"someMessage",
				EMAIL.name(),
				"someSender",
				"someMunicipalityId");

		assertThat(result.getSent())
			.isEqualTo(email.getReceivedAt()
				.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

		assertThat(result.getMessageId()).isNotNull()
			.satisfies(id -> assertThat(isValidUUID(id)).isTrue());

		assertThat(result.getAttachments()).isNotNull().isEmpty();
	}

	private boolean isValidUUID(final String uuidString) {
		try {
			UUID.fromString(uuidString);
			return true;
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}

}
