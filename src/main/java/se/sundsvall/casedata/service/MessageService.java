package se.sundsvall.casedata.service;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.notEqual;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.REPORTER;
import static se.sundsvall.casedata.integration.db.model.enums.NotificationSubType.MESSAGE;
import static se.sundsvall.casedata.integration.messaging.MessagingMapper.TYPE_REPORTER_SUPPORT_TEXT;
import static se.sundsvall.casedata.integration.messaging.MessagingMapper.toEmailRequest;
import static se.sundsvall.casedata.integration.messaging.MessagingMapper.toMessagingMessageRequest;
import static se.sundsvall.casedata.service.model.Constants.DEPARTMENT_NAME_PARATRANSIT;
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
import se.sundsvall.dept44.support.Identifier.Type;

@Service
@Transactional
public class MessageService {

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
		final var errandEntity = fetchErrand(municipalityId, namespace, errandId);

		final var messageEntity = messageRepository.save(mapper.toMessageEntity(request, errandId, municipalityId, namespace));
		notificationService.create(municipalityId, namespace, toNotification(errandEntity, NOTIFICATION_TYPE, NOTIFICATION_DESCRIPTION, MESSAGE), errandEntity);

		final var reporterStakeholder = getReporterStakeholder(errandEntity);

		if (doesCaseTypeExist(municipalityId, namespace, errandEntity.getCaseType()) && reporterStakeholder != null && !reporterStakeholder.getAdAccount().equals(request.getUsername())) {
			sendEmailNotification(municipalityId, namespace, errandEntity, reporterStakeholder, DEPARTMENT_NAME_PARATRANSIT);
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

	/**
	 * Method for sending notification message to external applicant stakeholder by partyId (called from
	 * ConversationService.createMessage).
	 *
	 * @param municipalityId of the errand that the message belongs to
	 * @param namespace      of the errand that the message belongs to
	 * @param errandId       of the errand that the message belongs to
	 * @param departmentName the department name to use when retreiving which messaging settings to use
	 */
	public void sendMessageNotification(final String municipalityId, final String namespace, final Long errandId, final String departmentName) {
		final var errandEntity = fetchErrand(municipalityId, namespace, errandId);

		final var messagingSettings = messagingSettingsIntegration.getMessagingsettings(municipalityId, namespace, departmentName);
		final var request = toMessagingMessageRequest(errandEntity, messagingSettings);

		sendMessageNotification(errandEntity, request);
	}

	/**
	 * Method for sending notification email to reporter stakeholder and in the process also create a notification to errand
	 * owner and reporter stakeholder (called from ConversationService.createMessage).
	 *
	 * @param municipalityId of the errand that the message belongs to
	 * @param namespace      of the errand that the message belongs to
	 * @param errandId       of the errand that the message belongs to
	 * @param departmentName the department name to use when retreiving which messaging settings to use
	 */
	public void sendEmailNotification(final String municipalityId, final String namespace, final Long errandId, final String departmentName) {
		final var errandEntity = fetchErrand(municipalityId, namespace, errandId);
		final var reporterStakeholder = getReporterStakeholder(errandEntity);

		// Create a notification and send email if logic determins that mail should be sent
		notificationService.create(municipalityId, namespace, toNotification(errandEntity, NOTIFICATION_TYPE, NOTIFICATION_DESCRIPTION, MESSAGE), errandEntity);
		if (isEmailNotificationToBeSent(reporterStakeholder)) {
			sendEmailNotification(municipalityId, namespace, errandEntity, reporterStakeholder, departmentName);
		}
	}

	/**
	 * Method determins if email notification is to be sent or not. If no identifier is present or if identifier is present
	 * and identifier is of any other type than ad account, or if type is ad account and value of
	 * stakeholder is not equal to identifier value (i.e. a user that is not same as the stakeholder is creating the
	 * message) an email should be sent.
	 *
	 * @param  stakeholderEntity the stakehlolder to evaluate against
	 * @return                   true if email notification is to be sent, false otherwise
	 */
	boolean isEmailNotificationToBeSent(final StakeholderEntity stakeholderEntity) {
		return ofNullable(Identifier.get())
			.map(identifier -> notEqual(identifier.getType(), Type.AD_ACCOUNT) ||
				notEqual(identifier.getValue(), ofNullable(stakeholderEntity)
					.map(StakeholderEntity::getAdAccount)
					.orElse(identifier.getValue()))) // If no stakeholder is present mail should not be sent, hence we use identifier value to compare to in that case
			.orElse(true);
	}

	private void sendMessageNotification(final ErrandEntity errandEntity, final generated.se.sundsvall.messaging.MessageRequest request) {
		final var partyId = ofNullable(request.getMessages())
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
		final var request = toEmailRequest(errandEntity, messagingSettings, stakeholderEntity, TYPE_REPORTER_SUPPORT_TEXT);
		messagingClient.sendEmail(errandEntity.getMunicipalityId(), request);
	}

	private StakeholderEntity getReporterStakeholder(final ErrandEntity errandEntity) {
		return ofNullable(errandEntity.getStakeholders())
			.orElse(emptyList())
			.stream()
			.filter(stakeholder -> ofNullable(stakeholder.getRoles()).orElse(emptyList()).contains(REPORTER.name()))
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

	private ErrandEntity fetchErrand(final String municipalityId, final String namespace, final Long errandId) {
		return errandRepository.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERRAND_ENTITY_NOT_FOUND.formatted(errandId, namespace, municipalityId)));
	}
}
