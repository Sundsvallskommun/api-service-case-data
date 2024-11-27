package se.sundsvall.casedata.service;

import static java.text.MessageFormat.format;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.ERRAND_WAS_NOT_FOUND;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNotification;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.casedata.api.model.MessageRequest;
import se.sundsvall.casedata.api.model.MessageResponse;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.MessageAttachmentRepository;
import se.sundsvall.casedata.integration.db.MessageRepository;
import se.sundsvall.casedata.service.scheduler.MessageMapper;

@Service
public class MessageService {

	private static final String NOTIFICATION_TYPE = "UPDATE";
	private static final String NOTIFICATION_DESCRIPTION = "Meddelande mottaget";
	private final MessageRepository messageRepository;

	private final ErrandRepository errandRepository;

	private final MessageAttachmentRepository messageAttachmentRepository;

	private final NotificationService notificationService;

	private final MessageMapper mapper;

	public MessageService(final MessageRepository messageRepository, final ErrandRepository errandRepository,
		final MessageAttachmentRepository messageAttachmentRepository, final NotificationService notificationService, final MessageMapper mapper) {
		this.messageRepository = messageRepository;
		this.errandRepository = errandRepository;
		this.messageAttachmentRepository = messageAttachmentRepository;
		this.notificationService = notificationService;
		this.mapper = mapper;
	}

	public List<MessageResponse> getMessagesByErrandNumber(final String errandNumber, final String municipalityId, final String namespace) {
		return mapper.toMessageResponses(messageRepository.findAllByErrandNumberAndMunicipalityIdAndNamespace(errandNumber, municipalityId, namespace));
	}

	public void saveMessageOnErrand(final Long errandId, final MessageRequest request, final String municipalityId, final String namespace) {
		final var errand = errandRepository.findByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(ERRAND_WAS_NOT_FOUND.formatted(errandId))));
		messageRepository.save(mapper.toMessageEntity(request, municipalityId, namespace));
		notificationService.createNotification(municipalityId, namespace, toNotification(errand, NOTIFICATION_TYPE, NOTIFICATION_DESCRIPTION));
	}

	public void updateViewedStatus(final Long errandId, final String messageId, final String municipalityId, final String namespace, final boolean isViewed) {
		verifyErrandExists(errandId, municipalityId, namespace);
		final var message = messageRepository
			.findByMessageIdAndMunicipalityIdAndNamespace(messageId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Message with id %s not found".formatted(messageId)));

		message.setViewed(isViewed);
		messageRepository.save(message);
	}

	public void getMessageAttachmentStreamed(final Long errandId, final String attachmentId, final String municipalityId, final String namespace, final HttpServletResponse response) {
		verifyErrandExists(errandId, municipalityId, namespace);
		try {
			final var attachmentEntity = messageAttachmentRepository
				.findByAttachmentIdAndMunicipalityIdAndNamespace(attachmentId, municipalityId, namespace)
				.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, "MessageAttachment not found"));

			final var file = attachmentEntity.getAttachmentData().getFile();

			response.addHeader(CONTENT_TYPE, attachmentEntity.getContentType());
			response.addHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + attachmentEntity.getName() + "\"");
			response.setContentLength((int) file.length());
			StreamUtils.copy(file.getBinaryStream(), response.getOutputStream());
		} catch (final IOException | SQLException e) {
			throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "%s occurred when copying file with attachment id '%s' to response: %s".formatted(e.getClass().getSimpleName(), attachmentId, e.getMessage()));
		}
	}

	private void verifyErrandExists(final Long errandId, final String municipalityId, final String namespace) {
		if (!errandRepository.existsByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)) {
			throw Problem.valueOf(NOT_FOUND, format(ERRAND_WAS_NOT_FOUND.formatted(errandId)));
		}
	}

}
