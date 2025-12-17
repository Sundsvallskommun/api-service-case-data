package se.sundsvall.casedata.service;

import static java.util.Collections.emptyList;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.REPORTER;
import static se.sundsvall.casedata.integration.db.model.enums.NotificationSubType.MESSAGE;
import static se.sundsvall.casedata.integration.messaging.MessagingMapper.toEmailRequest;
import static se.sundsvall.casedata.integration.messaging.MessagingMapper.toMessagingMessageRequest;
import static se.sundsvall.casedata.service.util.Constants.ERRAND_ENTITY_NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.MESSAGE_ATTACHMENT_ENTITY_NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.MESSAGE_ENTITY_NOT_FOUND;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNotification;

import generated.se.sundsvall.messaging.Message;
import generated.se.sundsvall.messaging.MessageParty;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.zalando.problem.Problem;
import se.sundsvall.casedata.api.model.MessageRequest;
import se.sundsvall.casedata.api.model.MessageResponse;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.MessageAttachmentRepository;
import se.sundsvall.casedata.integration.db.MessageRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.StakeholderEntity;
import se.sundsvall.casedata.integration.messaging.MessagingClient;
import se.sundsvall.casedata.integration.messagingsettings.MessagingSettingsIntegration;
import se.sundsvall.casedata.service.scheduler.MessageMapper;
import se.sundsvall.dept44.support.Identifier;

@Service
@Transactional
public class MessageService {

	static final String PARATRANSIT_DEPARTMENT_NAME = "PARATRANSIT";
	private static final String NOTIFICATION_TYPE = "UPDATE";
	private static final String NOTIFICATION_DESCRIPTION = "Meddelande skickat";
	private final MessageRepository messageRepository;
	private final ErrandRepository errandRepository;
	private final MessageAttachmentRepository messageAttachmentRepository;
	private final MessagingClient messagingClient;
	private final MessagingSettingsIntegration messagingSettingsIntegration;
	private final NotificationService notificationService;
	private final MetadataService metadataService;

	private final MessageMapper mapper;

	public MessageService(final MessageRepository messageRepository, final ErrandRepository errandRepository,
		final MessageAttachmentRepository messageAttachmentRepository, final MessagingClient messagingClient, final MessagingSettingsIntegration messagingSettingsIntegration, final NotificationService notificationService,
		final MetadataService metadataService,
		final MessageMapper mapper) {
		this.messageRepository = messageRepository;
		this.errandRepository = errandRepository;
		this.messageAttachmentRepository = messageAttachmentRepository;
		this.messagingClient = messagingClient;
		this.messagingSettingsIntegration = messagingSettingsIntegration;
		this.notificationService = notificationService;
		this.metadataService = metadataService;
		this.mapper = mapper;
	}

	public List<MessageResponse> findMessages(final Long errandId, final String municipalityId, final String namespace) {
		return mapper.toMessageResponses(messageRepository.findAllByErrandIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace), true);
	}

	public MessageResponse findMessage(final Long errandId, final String municipalityId, final String namespace, final String messageId) {
		final var messageEntity = messageRepository.findByMunicipalityIdAndNamespaceAndErrandIdAndMessageId(municipalityId, namespace, errandId, messageId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, MESSAGE_ENTITY_NOT_FOUND.formatted(messageId, namespace, municipalityId)));

		return mapper.toMessageResponse(messageEntity, true);
	}

	public List<MessageResponse> findExternalMessages(final Long errandId, final String municipalityId, final String namespace) {
		return mapper.toMessageResponses(messageRepository.findAllByErrandIdAndMunicipalityIdAndNamespaceAndInternalFalse(errandId, municipalityId, namespace), false);
	}

	public MessageResponse create(final Long errandId, final MessageRequest request, final String municipalityId, final String namespace) {
		final var errandEntity = errandRepository.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERRAND_ENTITY_NOT_FOUND.formatted(errandId, namespace, municipalityId)));

		final var messageEntity = messageRepository.save(mapper.toMessageEntity(request, errandId, municipalityId, namespace));
		notificationService.create(municipalityId, namespace, toNotification(errandEntity, NOTIFICATION_TYPE, NOTIFICATION_DESCRIPTION, MESSAGE), errandEntity);

		final var stakeholderEntity = getReporterStakeholder(errandEntity);

		if (doesCaseTypeExist(municipalityId, namespace, errandEntity.getCaseType()) && stakeholderEntity != null && !stakeholderEntity.getAdAccount().equals(request.getUsername())) {
			sendEmailNotification(municipalityId, namespace, errandEntity, stakeholderEntity, PARATRANSIT_DEPARTMENT_NAME);
		}

		return mapper.toMessageResponse(messageEntity, true);
	}

	public void updateViewedStatus(final Long errandId, final String messageId, final String municipalityId, final String namespace, final boolean isViewed) {
		verifyErrandExists(errandId, municipalityId, namespace);
		final var message = messageRepository
			.findByMunicipalityIdAndNamespaceAndErrandIdAndMessageId(municipalityId, namespace, errandId, messageId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, MESSAGE_ENTITY_NOT_FOUND.formatted(messageId, namespace, municipalityId)));

		message.setViewed(isViewed);
		messageRepository.save(message);
	}

	public void findMessageAttachmentAsStreamedResponse(final Long errandId, final String attachmentId, final String municipalityId, final String namespace, final HttpServletResponse response) {
		verifyErrandExists(errandId, municipalityId, namespace);
		try {
			final var attachmentEntity = messageAttachmentRepository
				.findByAttachmentIdAndMunicipalityIdAndNamespace(attachmentId, municipalityId, namespace)
				.orElseThrow(() -> Problem.valueOf(NOT_FOUND, MESSAGE_ATTACHMENT_ENTITY_NOT_FOUND.formatted(attachmentId, namespace, municipalityId)));

			final var file = attachmentEntity.getAttachmentData().getFile();

			response.addHeader(CONTENT_TYPE, attachmentEntity.getContentType());
			response.addHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + attachmentEntity.getName() + "\"");
			response.setContentLength((int) file.length());
			StreamUtils.copy(file.getBinaryStream(), response.getOutputStream());
		} catch (final IOException | SQLException e) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "%s occurred when copying file with attachment id '%s' to response: %s".formatted(e.getClass().getSimpleName(), attachmentId, e.getMessage()));
		}
	}

	private void verifyErrandExists(final Long errandId, final String municipalityId, final String namespace) {
		if (!errandRepository.existsByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)) {
			throw Problem.valueOf(NOT_FOUND, ERRAND_ENTITY_NOT_FOUND.formatted(errandId, namespace, municipalityId));
		}
	}

	public void sendMessageNotification(final String municipalityId, final String namespace, final Long errandId, final String departmentName) {

		final var errandEntity = errandRepository.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERRAND_ENTITY_NOT_FOUND.formatted(errandId, namespace, municipalityId)));

		final var messagingSettings = messagingSettingsIntegration.getMessagingsettings(municipalityId, namespace, departmentName);
		final var request = toMessagingMessageRequest(errandEntity, messagingSettings);

		sendMessageNotification(errandEntity, request);
	}

	public void sendMessageNotification(final ErrandEntity errandEntity, final generated.se.sundsvall.messaging.MessageRequest request) {

		final var partyId = Optional.ofNullable(request.getMessages())
			.map(List::getFirst)
			.map(Message::getParty)
			.map(MessageParty::getPartyId)
			.map(UUID::toString)
			.orElse(null);

		if (Identifier.get() != null && !Identifier.get().getValue().equalsIgnoreCase(partyId)) {
			final var message = messagingClient.sendMessage(errandEntity.getMunicipalityId(), request);

			if (message == null) {
				throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to create message notification");
			}
		}

	}

	private void sendEmailNotification(final String municipalityId, final String namespace, final ErrandEntity errandEntity, final StakeholderEntity stakeholderEntity, final String departmentName) {

		final var messagingSettings = messagingSettingsIntegration.getMessagingsettings(municipalityId, namespace, departmentName);
		final var request = toEmailRequest(errandEntity, messagingSettings, stakeholderEntity);

		messagingClient.sendEmail(errandEntity.getMunicipalityId(), request);
	}

	private StakeholderEntity getReporterStakeholder(final ErrandEntity errandEntity) {
		return Optional.ofNullable(errandEntity.getStakeholders())
			.orElse(emptyList())
			.stream()
			.filter(stakeholder -> stakeholder.getRoles().contains(REPORTER.name()))
			.findFirst()
			.orElse(null);
	}

	private boolean doesCaseTypeExist(final String municpalityId, final String namespace, final String type) {
		try {
			metadataService.getCaseType(municpalityId, namespace, type);
			return true;
		} catch (final Exception ignored) {
			return false;
		}

	}

}
