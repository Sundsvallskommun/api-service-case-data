package se.sundsvall.casedata.service;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.zalando.problem.Status.NOT_FOUND;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import jakarta.servlet.http.HttpServletResponse;
import se.sundsvall.casedata.api.model.MessageAttachmentDTO;
import se.sundsvall.casedata.api.model.MessageRequest;
import se.sundsvall.casedata.api.model.MessageResponse;
import se.sundsvall.casedata.integration.db.MessageAttachmentRepository;
import se.sundsvall.casedata.integration.db.MessageRepository;
import se.sundsvall.casedata.service.scheduler.MessageMapper;

@Service
public class MessageService {

	private final MessageRepository repository;

	private final MessageAttachmentRepository attachmentRepository;

	private final MessageMapper mapper;

	public MessageService(final MessageRepository repository, final MessageAttachmentRepository attachmentRepository, final MessageMapper mapper) {
		this.repository = repository;
		this.attachmentRepository = attachmentRepository;
		this.mapper = mapper;
	}

	public List<MessageResponse> getMessagesByErrandNumber(final String errandNumber) {
		return mapper.toMessageResponses(repository.findAllByErrandNumber(errandNumber));
	}

	public void saveMessage(final MessageRequest request) {
		repository.save(mapper.toMessageEntity(request));
	}

	public void updateViewedStatus(final String messageID, final boolean isViewed) {
		final var message = repository
			.findById(messageID)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Message with id %s not found".formatted(messageID)));

		message.setViewed(isViewed);
		repository.save(message);
	}

	public MessageAttachmentDTO getMessageAttachment(final String attachmentID) {
		return mapper.toAttachmentDto(attachmentRepository.findById(attachmentID).orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, "MessageAttachment not found")));
	}

	public void getMessageAttachmentStreamed(String attachmentID, HttpServletResponse response) {
		try {
			final var attachmentEntity = attachmentRepository
				.findById(attachmentID)
				.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, "MessageAttachment not found"));

			final var file = attachmentEntity.getAttachmentData().getFile();

			response.addHeader(CONTENT_TYPE, attachmentEntity.getContentType());
			response.addHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + attachmentEntity.getName() + "\"");
			response.setContentLength((int) file.length());
			StreamUtils.copy(file.getBinaryStream(), response.getOutputStream());
		} catch (IOException | SQLException e) {
			throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "%s occurred when copying file with attachment id '%s' to response: %s".formatted(e.getClass().getSimpleName(), attachmentID, e.getMessage()));
		}
	}
}
