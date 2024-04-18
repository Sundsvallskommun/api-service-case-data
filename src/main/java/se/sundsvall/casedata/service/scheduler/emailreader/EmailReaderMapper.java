package se.sundsvall.casedata.service.scheduler.emailreader;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import se.sundsvall.casedata.integration.db.model.Attachment;
import se.sundsvall.casedata.integration.db.model.EmailHeader;
import se.sundsvall.casedata.integration.db.model.Message;
import se.sundsvall.casedata.integration.db.model.MessageAttachment;
import se.sundsvall.casedata.integration.db.model.MessageAttachmentData;
import se.sundsvall.casedata.integration.db.model.enums.Direction;
import se.sundsvall.casedata.integration.db.model.enums.Header;
import se.sundsvall.casedata.integration.db.model.enums.MessageType;
import se.sundsvall.casedata.service.util.BlobBuilder;

import generated.se.sundsvall.emailreader.Email;
import generated.se.sundsvall.emailreader.EmailAttachment;

@Component
public class EmailReaderMapper {

	private final BlobBuilder blobBuilder;

	public EmailReaderMapper(final BlobBuilder blobBuilder) {
		this.blobBuilder = blobBuilder;
	}

	List<Attachment> toAttachments(final Email email) {
		if (email == null) {
			return List.of();
		}
		return Optional.ofNullable(email.getAttachments()).orElse(List.of())
			.stream()
			.map(emailAttachment ->
				(Attachment) Attachment.builder()
					.withFile(emailAttachment.getContent())
					.withName(emailAttachment.getName())
					.build())
			.toList();
	}

	Message toMessage(final Email email) {
		if (email == null) {
			return null;
		}
		return Message.builder()
			.withMessageID(email.getId())
			.withDirection(Direction.INBOUND)
			.withFamilyID("")
			.withExternalCaseID("")
			.withSubject(email.getSubject())
			.withTextmessage(email.getMessage())
			.withSent(email.getReceivedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
			.withMessageType(MessageType.EMAIL.name())
			.withEmail(email.getSender())
			.withAttachments(toMessageAttachments(email))
			.withHeaders(toEmailHeaders(email.getHeaders()))
			.build();
	}

	private List<EmailHeader> toEmailHeaders(final Map<String, List<String>> headers) {
		return Optional.ofNullable(headers).orElse(Collections.emptyMap()).entrySet().stream()
			.map(entry -> EmailHeader.builder()
				.withHeader(Header.valueOf(entry.getKey()))
				.withValues(entry.getValue())
				.build())
			.toList();
	}

	private List<MessageAttachment> toMessageAttachments(final Email email) {

		return Optional.ofNullable(email.getAttachments()).orElse(Collections.emptyList()).stream()
			.map(attachment -> MessageAttachment.builder()
				.withAttachmentID(UUID.randomUUID().toString())
				.withName(attachment.getName())
				.withMessageID(email.getId())
				.withAttachmentData(toMessageAttachmentData(attachment))
				.withContentType(attachment.getContentType())
				.build())
			.toList();
	}

	private MessageAttachmentData toMessageAttachmentData(final EmailAttachment attachment) {
		return MessageAttachmentData.builder()
			.withFile(blobBuilder.createBlob(attachment.getContent()))
			.build();
	}

}
