package se.sundsvall.casedata.service.scheduler;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.UUID.randomUUID;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

import generated.se.sundsvall.emailreader.Email;
import generated.se.sundsvall.emailreader.EmailAttachment;
import generated.se.sundsvall.webmessagecollector.MessageDTO;
import java.sql.Blob;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import se.sundsvall.casedata.api.model.EmailHeader;
import se.sundsvall.casedata.api.model.MessageRequest;
import se.sundsvall.casedata.api.model.MessageResponse;
import se.sundsvall.casedata.api.model.validation.enums.MessageType;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;
import se.sundsvall.casedata.integration.db.model.EmailHeaderEntity;
import se.sundsvall.casedata.integration.db.model.MessageAttachmentDataEntity;
import se.sundsvall.casedata.integration.db.model.MessageAttachmentEntity;
import se.sundsvall.casedata.integration.db.model.MessageEntity;
import se.sundsvall.casedata.integration.db.model.enums.Direction;
import se.sundsvall.casedata.integration.db.model.enums.Header;
import se.sundsvall.casedata.service.util.BlobBuilder;

@Component
public class MessageMapper {

	private final BlobBuilder blobBuilder;

	public MessageMapper(final BlobBuilder blobBuilder) {
		this.blobBuilder = blobBuilder;
	}

	public MessageEntity toMessageEntity(final Long errandId, final MessageDTO dto, final String municipalityId, final String namespace) {
		return MessageEntity.builder()
			.withMessageId(randomUUID().toString())
			.withErrandId(errandId)
			.withExternalCaseId(dto.getExternalCaseId())
			.withFamilyId(dto.getFamilyId())
			.withTextmessage(dto.getMessage())
			.withDirection(Direction.valueOf(dto.getDirection().name()))
			.withSent(dto.getSent())
			.withFirstName(dto.getFirstName())
			.withLastName(dto.getLastName())
			.withEmail(dto.getEmail())
			.withUserId(dto.getUserId())
			.withUsername(dto.getUsername())
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withMessageType(MessageType.WEBMESSAGE.name())
			.build();
	}

	public MessageEntity toMessageEntity(final MessageRequest request, final Long errandId, final String municipalityId, final String namespace) {
		final var entity = MessageEntity.builder()
			.withMessageId(request.getMessageId())
			.withErrandId(errandId)
			.withExternalCaseId(request.getExternalCaseId())
			.withFamilyId(request.getFamilyId())
			.withTextmessage(request.getMessage())
			.withSubject(request.getSubject())
			.withDirection(request.getDirection())
			.withSent(request.getSent())
			.withFirstName(request.getFirstName())
			.withLastName(request.getLastName())
			.withRecipients(request.getRecipients())
			.withMessageType(request.getMessageType())
			.withMobileNumber(request.getMobileNumber())
			.withEmail(request.getEmail())
			.withUserId(request.getUserId())
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withClassification(request.getClassification())
			.withUsername(request.getUsername())
			.withInternal(request.getInternal());

		Optional.ofNullable(request.getEmailHeaders()).ifPresent(headers -> entity.withHeaders(toEmailHeadersEntities(headers)));
		if (request.getAttachments() != null) {
			entity.withAttachments(toAttachmentEntities(request.getAttachments(),
				request.getMessageId(), municipalityId, namespace));
		}

		return entity.build();
	}

	public List<MessageResponse> toMessageResponses(final List<MessageEntity> allByExternalCaseId, final boolean includeViewed) {
		return allByExternalCaseId.stream()
			.map(messageEntity -> toMessageResponse(messageEntity, includeViewed))
			.toList();
	}

	public MessageResponse toMessageResponse(final MessageEntity entity, final boolean includeViewed) {
		if (entity == null) {
			return null;
		}

		final var response = MessageResponse.builder()
			.withErrandId(entity.getErrandId())
			.withExternalCaseId(entity.getExternalCaseId())
			.withFamilyId(entity.getFamilyId())
			.withMessage(entity.getTextmessage())
			.withMessageId(entity.getMessageId())
			.withSubject(entity.getSubject())
			.withDirection(entity.getDirection())
			.withSent(entity.getSent())
			.withFirstName(entity.getFirstName())
			.withLastName(entity.getLastName())
			.withMessageType(entity.getMessageType())
			.withMobileNumber(entity.getMobileNumber())
			.withRecipients(entity.getRecipients())
			.withEmail(entity.getEmail())
			.withUserId(entity.getUserId())
			.withUsername(entity.getUsername())
			.withClassification(entity.getClassification())
			.withInternal(entity.getInternal());

		if (includeViewed) {
			response.withViewed(entity.isViewed());
		}
		Optional.ofNullable(entity.getHeaders()).ifPresent(headers -> response.withEmailHeaders(toEmailHeaders(headers)));
		Optional.ofNullable(entity.getAttachments()).ifPresent(attachments -> response.withAttachments(toAttachmentResponses(attachments)));
		return response.build();
	}

	public List<EmailHeaderEntity> toEmailHeadersEntities(final List<EmailHeader> dtos) {
		return dtos.stream()
			.map(this::toEmailHeaderEntity)
			.toList();
	}

	public EmailHeaderEntity toEmailHeaderEntity(final EmailHeader header) {
		return EmailHeaderEntity.builder()
			.withHeader(header.getHeader())
			.withValues(header.getValues())
			.build();
	}

	public List<EmailHeader> toEmailHeaders(final List<EmailHeaderEntity> headers) {
		return headers.stream()
			.map(this::toEmailHeader)
			.toList();
	}

	public EmailHeader toEmailHeader(final EmailHeaderEntity header) {
		return EmailHeader.builder()
			.withHeader(header.getHeader())
			.withValues(header.getValues())
			.build();
	}

	public MessageAttachmentEntity toAttachmentEntity(final generated.se.sundsvall.webmessagecollector.MessageAttachment attachment, final String messageId, final String municipalityId, final String namespace) {

		return MessageAttachmentEntity.builder()
			.withAttachmentId(String.valueOf(attachment.getAttachmentId()))
			.withMessageID(messageId)
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withName(attachment.getName())
			.withContentType(attachment.getMimeType())
			.build();
	}

	public MessageAttachmentEntity toAttachmentEntity(final EmailAttachment attachment, final String messageId, final String municipalityId, final String namespace) {

		return MessageAttachmentEntity.builder()
			.withAttachmentId(String.valueOf(attachment.getId()))
			.withMessageID(messageId)
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withName(attachment.getName())
			.withContentType(attachment.getContentType())
			.build();
	}

	public AttachmentEntity toAttachmentEntity(final MessageAttachmentEntity attachment) {

		final var contentString = Optional.ofNullable(attachment.getAttachmentData())
			.map(data -> toContentString(data.getFile()))
			.orElse(null);

		return AttachmentEntity.builder()
			.withMunicipalityId(attachment.getMunicipalityId())
			.withNamespace(attachment.getNamespace())
			.withFile(contentString)
			.withName(attachment.getName())
			.withMimeType(attachment.getContentType())
			.build();
	}

	public List<MessageAttachmentEntity> toAttachmentEntities(final List<MessageRequest.AttachmentRequest> attachmentRequests, final String messageID, final String municipalityId, final String namespace) {
		return attachmentRequests.stream()
			.map(attachmentRequest -> toAttachmentEntity(attachmentRequest, messageID, municipalityId, namespace))
			.toList();
	}

	public MessageAttachmentEntity toAttachmentEntity(final MessageRequest.AttachmentRequest attachmentRequest, final String messageID, final String municipalityId, final String namespace) {
		return MessageAttachmentEntity.builder()
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withAttachmentId(randomUUID().toString())
			.withMessageID(messageID)
			.withName(attachmentRequest.getName())
			.withContentType(attachmentRequest.getContentType())
			.withAttachmentData(toAttachmentDataEntity(attachmentRequest))
			.build();
	}

	private MessageAttachmentDataEntity toAttachmentDataEntity(final MessageRequest.AttachmentRequest attachmentRequest) {
		return MessageAttachmentDataEntity.builder()
			.withFile(blobBuilder.createBlob(attachmentRequest.getContent()))
			.build();
	}

	public List<MessageResponse.AttachmentResponse> toAttachmentResponses(final List<MessageAttachmentEntity> attachment) {
		return attachment.stream()
			.map(this::toAttachmentResponse)
			.toList();
	}

	public MessageResponse.AttachmentResponse toAttachmentResponse(final MessageAttachmentEntity attachment) {
		return MessageResponse.AttachmentResponse.builder()
			.withName(attachment.getName())
			.withAttachmentId(attachment.getAttachmentId())
			.withContentType(attachment.getContentType())
			.build();
	}

	public MessageAttachmentDataEntity toMessageAttachmentData(final byte[] result) {
		return MessageAttachmentDataEntity.builder()
			.withFile(blobBuilder.createBlob(result))
			.build();
	}

	public String toContentString(final Blob blob) {
		try {
			return toContentString(blob.getBinaryStream().readAllBytes());
		} catch (final Exception e) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to convert binary stream to base64 representation");
		}
	}

	public String toContentString(final byte[] result) {
		return new String(Base64.getEncoder().encode(result), UTF_8);
	}

	public MessageEntity toMessage(final Email email, final String municipalityId, final String namespace, final Long errandId) {
		if (email == null) {
			return null;
		}
		return MessageEntity.builder()
			.withErrandId(errandId)
			.withMessageId(email.getId())
			.withDirection(Direction.INBOUND)
			.withFamilyId("")
			.withExternalCaseId("")
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withRecipients(email.getRecipients())
			.withSubject(email.getSubject())
			.withTextmessage(email.getMessage())
			.withSent(email.getReceivedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
			.withMessageType(MessageType.EMAIL.name())
			.withEmail(email.getSender())
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

}
