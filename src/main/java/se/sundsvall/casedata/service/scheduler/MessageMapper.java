package se.sundsvall.casedata.service.scheduler;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;

import generated.se.sundsvall.webmessagecollector.MessageDTO;
import se.sundsvall.casedata.api.model.EmailHeaderDTO;
import se.sundsvall.casedata.api.model.MessageAttachmentDTO;
import se.sundsvall.casedata.api.model.MessageRequest;
import se.sundsvall.casedata.api.model.MessageResponse;
import se.sundsvall.casedata.api.model.validation.enums.MessageType;
import se.sundsvall.casedata.integration.db.model.Attachment;
import se.sundsvall.casedata.integration.db.model.EmailHeader;
import se.sundsvall.casedata.integration.db.model.Message;
import se.sundsvall.casedata.integration.db.model.MessageAttachment;
import se.sundsvall.casedata.integration.db.model.MessageAttachmentData;
import se.sundsvall.casedata.integration.db.model.enums.Direction;
import se.sundsvall.casedata.service.util.BlobBuilder;

@Component
public class MessageMapper {

	private final BlobBuilder blobBuilder;

	public MessageMapper(final BlobBuilder blobBuilder) {
		this.blobBuilder = blobBuilder;
	}

	public Message toMessageEntity(final String errandNumber, final MessageDTO dto, final String municipalityId) {
		return Message.builder()
			.withMessageID(UUID.randomUUID().toString())
			.withErrandNumber(errandNumber)
			.withExternalCaseID(dto.getExternalCaseId())
			.withFamilyID(dto.getFamilyId())
			.withTextmessage(dto.getMessage())
			.withDirection(Direction.valueOf(dto.getDirection().name()))
			.withSent(dto.getSent())
			.withFirstName(dto.getFirstName())
			.withLastName(dto.getLastName())
			.withEmail(dto.getEmail())
			.withUserID(dto.getUserId())
			.withUsername(dto.getUsername())
			.withMunicipalityId(municipalityId)
			.withMessageType(MessageType.WEBMESSAGE.name())
			.build();
	}

	public Message toMessageEntity(final MessageRequest request, final String municipalityId) {
		final var entity = Message.builder()
			.withMessageID(request.getMessageID())
			.withErrandNumber(request.getErrandNumber())
			.withExternalCaseID(request.getExternalCaseID())
			.withFamilyID(request.getFamilyID())
			.withTextmessage(request.getMessage())
			.withSubject(request.getSubject())
			.withDirection(request.getDirection())
			.withSent(request.getSent())
			.withFirstName(request.getFirstName())
			.withLastName(request.getLastName())
			.withMessageType(request.getMessageType())
			.withMobileNumber(request.getMobileNumber())
			.withEmail(request.getEmail())
			.withUserID(request.getUserID())
			.withMunicipalityId(municipalityId)
			.withClassification(request.getClassification())
			.withUsername(request.getUsername());

		Optional.ofNullable(request.getEmailHeaders()).ifPresent(headers -> entity.withHeaders(toEmailHeaders(headers)));
		if (request.getAttachmentRequests() != null) {
			entity.withAttachments(toAttachmentEntities(request.getAttachmentRequests(),
				request.getMessageID()));
		}
		return entity.build();
	}

	public List<MessageResponse> toMessageResponses(final List<Message> allByExternalCaseId) {
		return allByExternalCaseId.stream()
			.map(this::toMessageResponse)
			.toList();
	}

	public MessageResponse toMessageResponse(final Message entity) {
		if (entity == null) {
			return null;
		}

		final var response = MessageResponse.builder()
			.withErrandNumber(entity.getErrandNumber())
			.withExternalCaseID(entity.getExternalCaseID())
			.withFamilyID(entity.getFamilyID())
			.withMessage(entity.getTextmessage())
			.withMessageID(entity.getMessageID())
			.withSubject(entity.getSubject())
			.withDirection(entity.getDirection())
			.withSent(entity.getSent())
			.withFirstName(entity.getFirstName())
			.withLastName(entity.getLastName())
			.withMessageType(entity.getMessageType())
			.withMobileNumber(entity.getMobileNumber())
			.withEmail(entity.getEmail())
			.withUserID(entity.getUserID())
			.withUsername(entity.getUsername())
			.withClassification(entity.getClassification())
			.withViewed(entity.isViewed());

		Optional.ofNullable(entity.getHeaders()).ifPresent(headers -> response.withEmailHeaders(toEmailHeaderDtos(headers)));
		Optional.ofNullable(entity.getAttachments()).ifPresent(attachments -> response.withAttachments(toAttachmentResponses(attachments)));
		return response.build();
	}

	public List<EmailHeader> toEmailHeaders(final List<EmailHeaderDTO> dtos) {
		return dtos.stream()
			.map(this::toEmailHeader)
			.toList();
	}

	public EmailHeader toEmailHeader(final EmailHeaderDTO dto) {
		return EmailHeader.builder()
			.withHeader(dto.getHeader())
			.withValues(dto.getValues())
			.build();
	}

	public List<EmailHeaderDTO> toEmailHeaderDtos(final List<EmailHeader> headers) {
		return headers.stream()
			.map(this::toEmailHeaderDto)
			.toList();
	}

	public EmailHeaderDTO toEmailHeaderDto(final EmailHeader header) {
		return EmailHeaderDTO.builder()
			.withHeader(header.getHeader())
			.withValues(header.getValues())
			.build();
	}

	public MessageAttachment toAttachmentEntity(final generated.se.sundsvall.webmessagecollector.MessageAttachment attachment, final String messageId) {

		return MessageAttachment.builder()
			.withAttachmentID(String.valueOf(attachment.getAttachmentId()))
			.withMessageID(messageId)
			.withName(attachment.getName())
			.withContentType(attachment.getMimeType())
			.build();
	}

	public Attachment toAttachment(final MessageAttachment attachment) {
		try {
			return Attachment.builder()
				.withFile(Base64.getEncoder().encodeToString(attachment.getAttachmentData().getFile().getBinaryStream().readAllBytes()))
				.withName(attachment.getName())
				.withMimeType(attachment.getContentType())
				.build();
		} catch (final Exception e) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to convert binary stream to base64 representation");
		}
	}

	public List<MessageAttachment> toAttachmentEntities(final List<MessageRequest.AttachmentRequest> attachmentRequests, final String messageID) {
		return attachmentRequests.stream()
			.map(attachmentRequest -> toAttachmentEntity(attachmentRequest, messageID))
			.toList();
	}

	public MessageAttachment toAttachmentEntity(final MessageRequest.AttachmentRequest attachmentRequest, final String messageID) {
		return MessageAttachment.builder()
			.withAttachmentID(UUID.randomUUID().toString())
			.withMessageID(messageID)
			.withName(attachmentRequest.getName())
			.withContentType(attachmentRequest.getContentType())
			.withAttachmentData(toAttachmentDataEntity(attachmentRequest))
			.build();
	}

	private MessageAttachmentData toAttachmentDataEntity(final MessageRequest.AttachmentRequest attachmentRequest) {
		return MessageAttachmentData.builder()
			.withFile(blobBuilder.createBlob(attachmentRequest.getContent()))
			.build();
	}

	public List<MessageResponse.AttachmentResponse> toAttachmentResponses(final List<MessageAttachment> attachment) {
		return attachment.stream()
			.map(this::toAttachmentResponse)
			.toList();
	}

	public MessageResponse.AttachmentResponse toAttachmentResponse(final MessageAttachment attachment) {
		return MessageResponse.AttachmentResponse.builder()
			.withName(attachment.getName())
			.withAttachmentID(attachment.getAttachmentID())
			.withContentType(attachment.getContentType())
			.build();
	}

	public MessageAttachmentDTO toAttachmentDto(final MessageAttachment attachment) {
		try {
			return MessageAttachmentDTO.builder()
				.withName(attachment.getName())
				.withAttachmentID(attachment.getAttachmentID())
				.withContent(new String(Base64.getEncoder().encode(attachment.getAttachmentData().getFile().getBinaryStream().readAllBytes()), UTF_8))
				.withContentType(attachment.getContentType())
				.build();
		} catch (final Exception e) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to convert binary stream to base64 representation");
		}
	}

	public MessageAttachmentData toMessageAttachmentData(final byte[] result) {
		return MessageAttachmentData.builder()
			.withFile(blobBuilder.createBlob(result))
			.build();
	}
}
