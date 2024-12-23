package se.sundsvall.casedata.service.scheduler.emailreader;

import generated.se.sundsvall.emailreader.Email;
import generated.se.sundsvall.emailreader.EmailAttachment;
import org.springframework.stereotype.Component;
import se.sundsvall.casedata.api.model.validation.enums.MessageType;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;
import se.sundsvall.casedata.integration.db.model.EmailHeaderEntity;
import se.sundsvall.casedata.integration.db.model.MessageAttachmentDataEntity;
import se.sundsvall.casedata.integration.db.model.MessageAttachmentEntity;
import se.sundsvall.casedata.integration.db.model.MessageEntity;
import se.sundsvall.casedata.integration.db.model.enums.Direction;
import se.sundsvall.casedata.integration.db.model.enums.Header;
import se.sundsvall.casedata.service.util.BlobBuilder;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class EmailReaderMapper {

	private final BlobBuilder blobBuilder;

	public EmailReaderMapper(final BlobBuilder blobBuilder) {
		this.blobBuilder = blobBuilder;
	}

	List<AttachmentEntity> toAttachments(final Email email, final String municipalityId, final String namespace) {
		if (email == null) {
			return List.of();
		}
		return Optional.ofNullable(email.getAttachments()).orElse(List.of())
			.stream()
			.map(emailAttachment -> AttachmentEntity.builder()
				.withFile(emailAttachment.getContent())
				.withMunicipalityId(municipalityId)
				.withNamespace(namespace)
				.withName(emailAttachment.getName())
				.withMimeType(emailAttachment.getContentType())
				.build())
			.toList();
	}

	MessageEntity toMessage(final Email email, final String municipalityId, final String namespace) {
		if (email == null) {
			return null;
		}
		return MessageEntity.builder()
			.withMessageId(email.getId())
			.withDirection(Direction.INBOUND)
			.withFamilyId("")
			.withExternalCaseId("")
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withSubject(email.getSubject())
			.withTextmessage(email.getMessage())
			.withSent(email.getReceivedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
			.withMessageType(MessageType.EMAIL.name())
			.withEmail(email.getSender())
			.withAttachments(toMessageAttachments(email, municipalityId, namespace))
			.withHeaders(toEmailHeaders(email.getHeaders()))
			.build();
	}

	private List<EmailHeaderEntity> toEmailHeaders(final Map<String, List<String>> headers) {
		return Optional.ofNullable(headers).orElse(Collections.emptyMap()).entrySet().stream()
			.map(entry -> EmailHeaderEntity.builder()
				.withHeader(Header.valueOf(entry.getKey()))
				.withValues(entry.getValue())
				.build())
			.toList();
	}

	private List<MessageAttachmentEntity> toMessageAttachments(final Email email, final String municipalityId, final String namespace) {
		return Optional.ofNullable(email.getAttachments()).orElse(Collections.emptyList()).stream()
			.map(attachment -> MessageAttachmentEntity.builder()
				.withAttachmentId(UUID.randomUUID().toString())
				.withName(attachment.getName())
				.withMessageID(email.getId())
				.withAttachmentData(toMessageAttachmentData(attachment))
				.withContentType(attachment.getContentType())
				.withMunicipalityId(municipalityId)
				.withNamespace(namespace)
				.build())
			.toList();
	}

	private MessageAttachmentDataEntity toMessageAttachmentData(final EmailAttachment attachment) {
		return MessageAttachmentDataEntity.builder()
			.withFile(blobBuilder.createBlob(attachment.getContent()))
			.build();
	}

}
