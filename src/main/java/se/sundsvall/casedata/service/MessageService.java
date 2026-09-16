package se.sundsvall.casedata.service;

import generated.se.sundsvall.messaging.DeliveryResult;
import generated.se.sundsvall.messaging.MessageBatchResult;
import generated.se.sundsvall.messaging.MessageStatus;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.casedata.api.model.BulkEmailRequest;
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
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.support.Identifier;
import se.sundsvall.dept44.support.Identifier.Type;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.notEqual;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.APPLICANT;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.REPORTER;
import static se.sundsvall.casedata.integration.db.model.enums.NotificationSubType.MESSAGE;
import static se.sundsvall.casedata.integration.messaging.MessagingMapper.TYPE_OWNER_SUPPORT_TEXT;
import static se.sundsvall.casedata.integration.messaging.MessagingMapper.TYPE_REPORTER_SUPPORT_TEXT;
import static se.sundsvall.casedata.integration.messaging.MessagingMapper.findStakeholderEmail;
import static se.sundsvall.casedata.integration.messaging.MessagingMapper.toEmailAttachments;
import static se.sundsvall.casedata.integration.messaging.MessagingMapper.toEmailBatchRequest;
import static se.sundsvall.casedata.integration.messaging.MessagingMapper.toEmailBatchRequests;
import static se.sundsvall.casedata.integration.messaging.MessagingMapper.toMessagingMessageRequest;
import static se.sundsvall.casedata.integration.messaging.MessagingMapper.toUuidOrNull;
import static se.sundsvall.casedata.service.model.Constants.DEPARTMENT_NAME_PARATRANSIT;
import static se.sundsvall.casedata.service.util.Constants.ERRAND_ENTITY_NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.MESSAGE_ATTACHMENT_ENTITY_NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.MESSAGE_ENTITY_NOT_FOUND;
import static se.sundsvall.casedata.service.util.ResponseStreamer.streamBlob;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNotification;
import static se.sundsvall.dept44.util.LogUtils.sanitizeForLogging;

@Service
@Transactional
public class MessageService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MessageService.class);
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

		final var notifiableReporters = getReporterStakeholders(errandEntity).stream()
			.filter(reporter -> notEqual(reporter.getAdAccount(), request.getUsername()))
			.toList();

		if (doesCaseTypeExist(municipalityId, namespace, errandEntity.getCaseType()) && !notifiableReporters.isEmpty()) {
			sendEmailNotification(municipalityId, namespace, errandEntity, notifiableReporters, DEPARTMENT_NAME_PARATRANSIT);
		}

		return mapper.toMessageResponse(messageEntity, true);
	}

	/**
	 * Sends a manually composed email to a list of recipients - one individual email per recipient - via Messaging's
	 * batch endpoint. Unlike {@link #create(Long, MessageRequest, String, String)}, the email is actually transmitted
	 * via Messaging rather than merely recorded on the errand.
	 *
	 * @param errandId       of the errand the email is sent in the context of
	 * @param request        the recipients, subject, message and attachments to send
	 * @param municipalityId of the errand the email is sent in the context of
	 * @param namespace      of the errand the email is sent in the context of
	 */
	public void sendBulkEmail(final Long errandId, final BulkEmailRequest request, final String municipalityId, final String namespace) {
		verifyErrandExists(errandId, municipalityId, namespace);

		final var messagingSettings = messagingSettingsIntegration.getMessagingsettings(municipalityId, namespace, request.getDepartmentName());
		final var attachments = toEmailAttachments(request.getAttachments());
		final var emailBatchRequest = toEmailBatchRequest(request, messagingSettings, attachments);

		final var result = messagingClient.sendEmailBatch(municipalityId, emailBatchRequest);
		if ((result == null) || hasFailedDelivery(result)) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to send bulk email");
		}
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
		final var attachmentEntity = messageAttachmentRepository
			.findByAttachmentIdAndMunicipalityIdAndNamespace(attachmentId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, MESSAGE_ATTACHMENT_ENTITY_NOT_FOUND.formatted(attachmentId, namespace, municipalityId)));

		streamBlob(response, attachmentEntity.getName(), attachmentEntity.getContentType(), attachmentEntity.getAttachmentData().getFile(), attachmentId);
	}

	private void verifyErrandExists(final Long errandId, final String municipalityId, final String namespace) {
		if (!errandRepository.existsByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)) {
			throw Problem.valueOf(NOT_FOUND, ERRAND_ENTITY_NOT_FOUND.formatted(errandId, namespace, municipalityId));
		}
	}

	/**
	 * Method for sending notification message to external applicant stakeholder (called from
	 * ConversationService.createMessage). The applicant's own email address on the stakeholder is used in first hand.
	 * Only if no email is present on the applicant but a partyId (personId) is, the call is delegated to messaging
	 * /messages so that ContactSettings can be consulted to determine a recipient address.
	 *
	 * @param municipalityId of the errand that the message belongs to
	 * @param namespace      of the errand that the message belongs to
	 * @param errandId       of the errand that the message belongs to
	 * @param departmentName the department name to use when retreiving which messaging settings to use
	 */
	public void sendMessageNotification(final String municipalityId, final String namespace, final Long errandId, final String departmentName) {
		final var errandEntity = fetchErrand(municipalityId, namespace, errandId);
		final var applicantStakeholders = getApplicantStakeholders(errandEntity);
		final var notifiableApplicants = applicantStakeholders.stream()
			.filter(applicant -> !creatorIsSameAsApplicant(applicant))
			.toList();

		if (!applicantStakeholders.isEmpty() && notifiableApplicants.isEmpty()) {
			return;
		}

		final var messagingSettings = messagingSettingsIntegration.getMessagingsettings(municipalityId, namespace, departmentName);
		final var caseType = metadataService.getCaseType(municipalityId, namespace, errandEntity.getCaseType());

		final var hasApplicantEmail = notifiableApplicants.stream().anyMatch(applicant -> isNotBlank(findStakeholderEmail(applicant)));
		if (hasApplicantEmail) {
			toEmailBatchRequests(errandEntity, messagingSettings, notifiableApplicants, TYPE_OWNER_SUPPORT_TEXT, caseType)
				.forEach(emailBatchRequest -> {
					final var result = messagingClient.sendEmailBatch(municipalityId, emailBatchRequest);
					if ((result == null) || hasFailedDelivery(result)) {
						LOGGER.warn("Failed to send applicant notification email for errand '{}' in namespace '{}' for municipality '{}'", errandId, sanitizeForLogging(namespace), sanitizeForLogging(municipalityId));
					}
				});
			return;
		}

		final var applicantsWithPersonId = notifiableApplicants.stream()
			.filter(applicant -> toUuidOrNull(applicant.getPersonId()) != null)
			.toList();
		if (!applicantsWithPersonId.isEmpty()) {
			final var messageRequest = toMessagingMessageRequest(errandEntity, messagingSettings, applicantsWithPersonId, caseType);
			final var result = messagingClient.sendMessage(municipalityId, messageRequest);
			if (result == null) {
				throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to create message notification");
			}
			return;
		}

		LOGGER.warn("Cannot send applicant notification for errand '{}' in namespace '{}' for municipality '{}': applicant stakeholder has neither email nor partyId", errandId, sanitizeForLogging(namespace), sanitizeForLogging(municipalityId));
	}

	/**
	 * Method for sending email notification to reporter stakeholder (called from ConversationService.createMessage).
	 *
	 * @param municipalityId of the errand that the message belongs to
	 * @param namespace      of the errand that the message belongs to
	 * @param errandId       of the errand that the message belongs to
	 * @param departmentName the department name to use when retreiving which messaging settings to use
	 */
	public void sendEmailNotification(final String municipalityId, final String namespace, final Long errandId, final String departmentName) {
		final var errandEntity = fetchErrand(municipalityId, namespace, errandId);
		final var notifiableReporters = getReporterStakeholders(errandEntity).stream()
			.filter(this::isEmailNotificationToBeSent)
			.toList();

		// Send an individually personalized email to each reporter if logic determins that mail should be sent
		if (!notifiableReporters.isEmpty()) {
			sendEmailNotification(municipalityId, namespace, errandEntity, notifiableReporters, departmentName);
		}
	}

	/**
	 * Method determins if email notification is to be sent or not. If no identifier is present or if identifier is present
	 * and identifier is of any other type than ad account, or if type is ad account and value of stakeholder is not equal
	 * to identifier
	 * value (i.e. a user that is not same as the stakeholder is creating the message) an email should be sent.
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

	private void sendEmailNotification(final String municipalityId, final String namespace, final ErrandEntity errandEntity, final List<StakeholderEntity> stakeholderEntities, final String departmentName) {
		final var hasEmailRecipient = stakeholderEntities.stream().anyMatch(stakeholder -> isNotBlank(findStakeholderEmail(stakeholder)));
		if (!hasEmailRecipient) {
			LOGGER.warn("Cannot send reporter notification for errand '{}' in namespace '{}' for municipality '{}': reporter stakeholder has no email", errandEntity.getId(), sanitizeForLogging(namespace), sanitizeForLogging(municipalityId));
			return;
		}

		final var messagingSettings = messagingSettingsIntegration.getMessagingsettings(municipalityId, namespace, departmentName);
		final var caseType = metadataService.getCaseType(municipalityId, namespace, errandEntity.getCaseType());
		toEmailBatchRequests(errandEntity, messagingSettings, stakeholderEntities, TYPE_REPORTER_SUPPORT_TEXT, caseType)
			.forEach(emailBatchRequest -> {
				final var result = messagingClient.sendEmailBatch(errandEntity.getMunicipalityId(), emailBatchRequest);
				if ((result == null) || hasFailedDelivery(result)) {
					LOGGER.warn("Failed to send reporter notification email for errand '{}' in namespace '{}' for municipality '{}'", errandEntity.getId(), sanitizeForLogging(namespace), sanitizeForLogging(municipalityId));
				}
			});
	}

	/**
	 * Checks whether any individual delivery within a batch result failed, since {@link MessageBatchResult} itself
	 * being non-null only means Messaging accepted the request - each recipient's own delivery can still have failed.
	 *
	 * @param  result the batch result to inspect
	 * @return        true if any delivery within the batch has a failed status
	 */
	private static boolean hasFailedDelivery(final MessageBatchResult result) {
		return ofNullable(result.getMessages())
			.orElse(emptyList())
			.stream()
			.flatMap(message -> ofNullable(message.getDeliveries()).orElse(emptyList()).stream())
			.map(DeliveryResult::getStatus)
			.anyMatch(status -> (status == MessageStatus.FAILED) || (status == MessageStatus.NOT_SENT));
	}

	private List<StakeholderEntity> getReporterStakeholders(final ErrandEntity errandEntity) {
		return getStakeholdersByRole(errandEntity, REPORTER.name());
	}

	private List<StakeholderEntity> getApplicantStakeholders(final ErrandEntity errandEntity) {
		return getStakeholdersByRole(errandEntity, APPLICANT.name());
	}

	private List<StakeholderEntity> getStakeholdersByRole(final ErrandEntity errandEntity, final String role) {
		return ofNullable(errandEntity.getStakeholders())
			.orElse(emptyList())
			.stream()
			.filter(stakeholder -> ofNullable(stakeholder.getRoles()).orElse(emptyList()).contains(role))
			.toList();
	}

	/**
	 * Determines if the current Identifier (i.e. the user creating the message) is the same as the provided applicant
	 * stakeholder. Only Identifiers of type {@link Type#PARTY_ID} are evaluated against the applicant's
	 * {@code personId} — for any other Identifier type the creator is assumed to be different from the applicant (an
	 * internal user with an adAccount Identifier is never the external applicant). If both have a partyId and they
	 * match, the creator is considered to be the applicant and no notification should be sent (a user does not need to
	 * be notified of their own message).
	 *
	 * @param  applicantStakeholder the applicant stakeholder of the errand, or null if none was found
	 * @return                      true if the creator is the same as the applicant, false otherwise
	 */
	boolean creatorIsSameAsApplicant(final StakeholderEntity applicantStakeholder) {
		final var identifier = Identifier.get();
		if (identifier == null || applicantStakeholder == null || identifier.getType() != Type.PARTY_ID) {
			return false;
		}
		final var applicantPartyId = applicantStakeholder.getPersonId();
		return isNotBlank(applicantPartyId) && identifier.getValue().equalsIgnoreCase(applicantPartyId);
	}

	private boolean doesCaseTypeExist(final String municpalityId, final String namespace, final String type) {
		try {
			metadataService.getCaseType(municpalityId, namespace, type);
			return true;
		} catch (final Exception _) {
			return false;
		}
	}

	private ErrandEntity fetchErrand(final String municipalityId, final String namespace, final Long errandId) {
		return errandRepository.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERRAND_ENTITY_NOT_FOUND.formatted(errandId, namespace, municipalityId)));
	}
}
