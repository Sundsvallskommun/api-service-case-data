package se.sundsvall.casedata.service.scheduler.emailreader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static se.sundsvall.casedata.api.model.validation.enums.MessageType.EMAIL;
import static se.sundsvall.casedata.integration.db.model.enums.Direction.INBOUND;

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
import se.sundsvall.casedata.integration.db.model.EmailHeader;
import se.sundsvall.casedata.integration.db.model.enums.Header;

import generated.se.sundsvall.emailreader.Email;
import generated.se.sundsvall.emailreader.EmailAttachment;

@SpringBootTest(classes = {Application.class}, webEnvironment = MOCK)
@ActiveProfiles("junit")
class EmailReaderMapperTest {

	@Autowired
	private EmailReaderMapper emailReaderMapper;

	@Test
	void toAttachment() {

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

		final var result = emailReaderMapper.toAttachments(email);

		assertThat(result).isNotNull().hasSize(1)
			.element(0).satisfies(attachment -> {
				assertThat(attachment.getName()).isEqualTo("someName");
				assertThat(attachment.getFile()).isEqualTo("someContent");
			});
	}

	@Test
	void toAttachment_withNullEmail() {
		assertThat(emailReaderMapper.toAttachments(null)).isEmpty();
	}

	@Test
	void toAttachment_withNullEmailAttachments() {

		final var email = new Email()
			.id("someId")
			.subject("someSubject")
			.recipients(List.of("someRecipient"))
			.sender("someSender")
			.message("someMessage")
			.receivedAt(OffsetDateTime.now());

		final var result = emailReaderMapper.toAttachments(email);

		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	void toMessage() {
		final var messageID = UUID.randomUUID().toString();
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

		final var result = emailReaderMapper.toMessage(email);

		assertThat(result)
			.isNotNull()
			.extracting(
				"messageID",
				"direction",
				"subject",
				"textmessage",
				"messageType",
				"email")
			.containsExactlyInAnyOrder(
				email.getId(),
				INBOUND,
				"someSubject",
				"someMessage",
				EMAIL.name(),
				"someSender");

		assertThat(result.getHeaders()).hasSize(3).containsExactlyInAnyOrder(
			EmailHeader.builder()
				.withHeader(Header.MESSAGE_ID)
				.withValues(List.of("<some@message>"))
				.build(),
			EmailHeader.builder()
				.withHeader(Header.REFERENCES)
				.withValues(List.of("<some@message>", "<another@one>"))
				.build(),
			EmailHeader.builder()
				.withHeader(Header.IN_REPLY_TO)
				.withValues(List.of("<some@other>"))
				.build());

		assertThat(result.getSent())
			.isEqualTo(email.getReceivedAt()
				.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

		assertThat(result.getMessageID()).isNotNull()
			.satisfies(id -> assertThat(isValidUUID(id)).isTrue());

		assertThat(result.getAttachments()).isNotNull().hasSize(1)
			.element(0).satisfies(attachment -> {
				assertThat(attachment.getAttachmentID()).isNotNull()
					.satisfies(id -> assertThat(isValidUUID(id)).isTrue());
				assertThat(attachment.getName()).isEqualTo("someName");
				assertThat(attachment.getMessageID()).isEqualTo(messageID);
				assertThat(attachment.getAttachmentData().getFile().getBinaryStream().readAllBytes()).isEqualTo("someContent".getBytes());
				assertThat(attachment.getContentType()).isEqualTo("someContentType");
			});
	}

	@Test
	void toMessage_withNullEmail() {
		assertThat(emailReaderMapper.toMessage(null)).isNull();
	}

	@Test
	void toMessage_withNullAttachments() {

		final var email = new Email()
			.id(UUID.randomUUID().toString())
			.subject("someSubject")
			.recipients(List.of("someRecipient"))
			.sender("someSender")
			.message("someMessage")
			.receivedAt(OffsetDateTime.now());

		final var result = emailReaderMapper.toMessage(email);

		assertThat(result)
			.isNotNull()
			.extracting(
				"messageID",
				"direction",
				"subject",
				"textmessage",
				"messageType",
				"email")
			.containsExactlyInAnyOrder(
				email.getId(),
				INBOUND,
				"someSubject",
				"someMessage",
				EMAIL.name(),
				"someSender");

		assertThat(result.getSent())
			.isEqualTo(email.getReceivedAt()
				.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

		assertThat(result.getMessageID()).isNotNull()
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
