package se.sundsvall.casedata.service;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.Strings.CI;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.ADMINISTRATOR;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.REPORTER;
import static se.sundsvall.casedata.integration.db.model.enums.NotificationSubType.MESSAGE;
import static se.sundsvall.casedata.service.util.mappers.ConversationMapper.toAttachment;
import static se.sundsvall.casedata.service.util.mappers.ConversationMapper.updateConversationEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNotification;

import generated.se.sundsvall.messageexchange.Message;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.casedata.api.model.Attachment;
import se.sundsvall.casedata.api.model.validation.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.ConversationRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.ConversationEntity;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.StakeholderEntity;
import se.sundsvall.casedata.integration.messageexchange.MessageExchangeClient;
import se.sundsvall.casedata.service.notification.processor.ErrandOwnerNotificationProcessor;
import se.sundsvall.casedata.service.notification.processor.ErrandReporterNotificationProcessor;

@Service
public class MessageExchangeSyncService {

	private static final String NOTIFICATION_TYPE_UPDATE = "UPDATE";
	private static final String NOTIFICATION_DESCRIPTION = "Ny händelse för %s";

	// Map with key/value-pair containing role in errand that should receive a notification and list containing full
	// classname for the processors to use
	private static final Map<StakeholderRole, List<String>> ROLES_TO_PROCESS = Map.of(
		ADMINISTRATOR, List.of(ErrandOwnerNotificationProcessor.class.getName()),
		REPORTER, List.of(ErrandReporterNotificationProcessor.class.getName()));

	private final MessageExchangeClient messageExchangeClient;
	private final AttachmentService attachmentService;
	private final ConversationRepository conversationRepository;
	private final ErrandRepository errandRepository;
	private final NotificationService notificationService;

	@Value("${integration.message-exchange.namespace:casedata}")
	private String messageExchangeNamespace;

	public MessageExchangeSyncService(
		final MessageExchangeClient messageExchangeClient,
		final AttachmentService attachmentService,
		final ConversationRepository conversationRepository,
		final ErrandRepository errandRepository,
		final NotificationService notificationService) {

		this.messageExchangeClient = messageExchangeClient;
		this.attachmentService = attachmentService;
		this.conversationRepository = conversationRepository;
		this.notificationService = notificationService;
		this.errandRepository = errandRepository;
	}

	public void syncConversation(ConversationEntity conversationEntity, generated.se.sundsvall.messageexchange.Conversation conversation) {
		final var errandEntity = errandRepository.getReferenceById(Long.parseLong(conversationEntity.getErrandId()));
		final var filter = "sequenceNumber.id >" + ofNullable(conversationEntity.getLatestSyncedSequenceNumber()).orElse(0L);
		final var messages = messageExchangeClient.getMessages(conversationEntity.getMunicipalityId(), messageExchangeNamespace, conversationEntity.getMessageExchangeId(), filter, Pageable.unpaged());

		if (isNull(messages) || isNull(messages.getBody()) || !messages.getStatusCode().is2xxSuccessful()) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to retrieve messages from Message Exchange");
		}

		// Synchronize if sequence number of last sync is lesser than sequence number of incoming converation
		if (ofNullable(conversationEntity.getLatestSyncedSequenceNumber()).orElse(0L) < ofNullable(conversation.getLatestSequenceNumber()).orElse(0L)) {
			messages.getBody().forEach(message -> message.getAttachments().forEach(attachment -> syncAttachment(conversationEntity, message, attachment)));

			conversationRepository.save(updateConversationEntity(conversationEntity, conversation));

			// Send notification to stakeholder with role (using defined processors for the role) if ad account of stakeholder is
			// not blank
			ROLES_TO_PROCESS
				.entrySet()
				.forEach(roleProcessorEntry -> {
					final var adAccount = getAdAccountForRole(errandEntity, roleProcessorEntry.getKey());
					if (isNotBlank(adAccount)) {
						createNotification(errandEntity, messages.getBody(), conversation, adAccount, roleProcessorEntry.getValue());
					}
				});
		}
	}

	private void createNotification(ErrandEntity errandEntity, Page<Message> messages, generated.se.sundsvall.messageexchange.Conversation conversation, String ownerAdAccount, List<String> processors) {
		final var notification = toNotification(errandEntity, NOTIFICATION_TYPE_UPDATE, NOTIFICATION_DESCRIPTION.formatted(conversation.getTopic()), MESSAGE, ownerAdAccount);
		notification.setAcknowledged(allMessagesCreatedBy(messages, notification.getOwnerId()));
		notificationService.create(errandEntity.getMunicipalityId(), errandEntity.getNamespace(), notification, errandEntity, processors);
	}

	boolean allMessagesCreatedBy(Page<Message> syncMessages, String ownerId) {
		return syncMessages.stream()
			.allMatch(message -> nonNull(message.getCreatedBy()) && CI.equals(ownerId, message.getCreatedBy().getValue()));
	}

	private String getAdAccountForRole(ErrandEntity errand, StakeholderRole role) {
		return ofNullable(errand.getStakeholders()).orElse(emptyList()).stream()
			.filter(stakeholderEntity -> ofNullable(stakeholderEntity.getRoles()).orElse(emptyList()).contains(role.name()))
			.map(StakeholderEntity::getAdAccount)
			.findFirst()
			.orElse(null);
	}

	void syncAttachment(final ConversationEntity conversationEntity, final Message message, final generated.se.sundsvall.messageexchange.Attachment attachment) {
		final var file = messageExchangeClient.readErrandAttachment(conversationEntity.getMunicipalityId(), messageExchangeNamespace, conversationEntity.getMessageExchangeId(), message.getId(), attachment.getId());
		saveAttachment(Long.valueOf(conversationEntity.getErrandId()), conversationEntity.getMunicipalityId(), conversationEntity.getNamespace(), file);
	}

	void saveAttachment(final Long errandId, final String municipalityId, final String namespace, final ResponseEntity<InputStreamResource> file) {
		if (isNull(file) || isNull(file.getBody()) || isNull(file.getHeaders().getContentType())) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to retrieve attachment from Message Exchange");
		}

		final Attachment attachment;
		try {
			attachment = toAttachment(file.getBody().getContentAsByteArray(), file.getHeaders().getContentDisposition().getFilename(), file.getHeaders().getContentType().toString(), errandId, municipalityId, namespace);
		} catch (final IOException e) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to convert attachment from Message Exchange");
		}
		attachmentService.create(errandId, attachment, municipalityId, namespace);
	}
}
