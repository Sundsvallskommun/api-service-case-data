package se.sundsvall.casedata.service;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.zalando.problem.Status.NOT_FOUND;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.casedata.api.model.MessageAttachmentDTO;
import se.sundsvall.casedata.api.model.MessageRequest;
import se.sundsvall.casedata.api.model.MessageResponse;
import se.sundsvall.casedata.integration.db.MessageAttachmentRepository;
import se.sundsvall.casedata.integration.db.MessageRepository;
import se.sundsvall.casedata.service.scheduler.MessageMapper;

@Service
public class MessageService {

	private final MessageRepository messageRepository;

	private final MessageAttachmentRepository messageAttachmentRepository;

	private final MessageMapper mapper;

	public MessageService(final MessageRepository messageRepository,
		final MessageAttachmentRepository messageAttachmentRepository, final MessageMapper mapper) {
		this.messageRepository = messageRepository;
		this.messageAttachmentRepository = messageAttachmentRepository;
		this.mapper = mapper;
	}

	public List<MessageResponse> getMessagesByErrandNumber(final String errandNumber, final String municipalityId, final String namespace) {
		return mapper.toMessageResponses(messageRepository.findAllByErrandNumberAndMunicipalityIdAndNamespace(errandNumber, municipalityId, namespace));
	}

	public void saveMessage(final MessageRequest request, final String municipalityId, final String namespace) {
		messageRepository.save(mapper.toMessageEntity(request, municipalityId, namespace));
	}

	public void updateViewedStatus(final String messageId, final String municipalityId, final String namespace, final boolean isViewed) {
		final var message = messageRepository
			.findByMessageIDAndMunicipalityIdAndNamespace(messageId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Message with id %s not found".formatted(messageId)));

		message.setViewed(isViewed);
		messageRepository.save(message);
	}

	public MessageAttachmentDTO getMessageAttachment(final String attachmentId, final String municipalityId, final String namespace) {
		return mapper.toAttachmentDto(messageAttachmentRepository.findByAttachmentIDAndMunicipalityIdAndNamespace(attachmentId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, "MessageAttachment not found")));
	}

	public void getMessageAttachmentStreamed(final String attachmentId, final String municipalityId, final String namespace, final HttpServletResponse response) {
		try {
			final var attachmentEntity = messageAttachmentRepository
				.findByAttachmentIDAndMunicipalityIdAndNamespace(attachmentId, municipalityId, namespace)
				.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, "MessageAttachment not found"));

			final var file = attachmentEntity.getAttachmentData().getFile();

			response.addHeader(CONTENT_TYPE, attachmentEntity.getContentType());
			response.addHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + attachmentEntity.getName() + "\"");
			response.setContentLength((int) file.length());
			StreamUtils.copy(file.getBinaryStream(), response.getOutputStream());
		} catch (IOException | SQLException e) {
			throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "%s occurred when copying file with attachment id '%s' to response: %s".formatted(e.getClass().getSimpleName(), attachmentId, e.getMessage()));
		}
	}

}
