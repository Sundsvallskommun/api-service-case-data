package se.sundsvall.casedata.service;

import static java.util.Optional.ofNullable;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static se.sundsvall.casedata.integration.db.model.enums.NotificationSubType.MESSAGE;
import static se.sundsvall.casedata.service.util.mappers.ConversationMapper.toAttachment;
import static se.sundsvall.casedata.service.util.mappers.ConversationMapper.updateConversationEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNotification;

import generated.se.sundsvall.messageexchange.Message;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.casedata.api.model.Attachment;
import se.sundsvall.casedata.integration.db.ConversationRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.ConversationEntity;
import se.sundsvall.casedata.integration.messageexchange.MessageExchangeClient;

@Service
public class MessageExchangeSyncService {

	private static final String NOTIFICATION_TYPE_UPDATE = "UPDATE";
	private static final String NOTIFICATION_DESCRIPTION = "Ny händelse för %s";

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

	public void syncConversation(final ConversationEntity conversationEntity, final generated.se.sundsvall.messageexchange.Conversation conversation) {
		if (ofNullable(conversationEntity.getLatestSyncedSequenceNumber()).orElse(0L) < ofNullable(conversation.getLatestSequenceNumber()).orElse(0L)) {
			final var errandEntity = errandRepository.getReferenceById(Long.parseLong(conversationEntity.getErrandId()));
			final var notification = toNotification(errandEntity, NOTIFICATION_TYPE_UPDATE, NOTIFICATION_DESCRIPTION.formatted(conversation.getTopic()), MESSAGE);
			final var acknowledgeNotification = syncMessages(conversationEntity, notification.getOwnerId());
			if (acknowledgeNotification) {
				notification.setAcknowledged(true);
			}
			notificationService.create(errandEntity.getMunicipalityId(), errandEntity.getNamespace(), notification, errandEntity);
		}

		conversationRepository.save(updateConversationEntity(conversationEntity, conversation));
	}

	boolean syncMessages(final ConversationEntity conversationEntity, String errandAdministratorOwnerId) {

		final var filter = "sequenceNumber.id >" + ofNullable(conversationEntity.getLatestSyncedSequenceNumber()).orElse(0L);

		final var response = messageExchangeClient.getMessages(conversationEntity.getMunicipalityId(), messageExchangeNamespace, conversationEntity.getMessageExchangeId(), filter, Pageable.unpaged());

		if (response == null || !response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to retrieve messages from Message Exchange");
		}

		response.getBody().forEach(message -> message.getAttachments().forEach(attachment -> syncAttachment(conversationEntity, message, attachment)));

		return response.getBody().stream()
			.allMatch(message -> message.getCreatedBy() != null && message.getCreatedBy().getValue().equals(errandAdministratorOwnerId));
	}

	void syncAttachment(final ConversationEntity conversationEntity, final Message message, final generated.se.sundsvall.messageexchange.Attachment attachment) {
		final var file = messageExchangeClient.readErrandAttachment(conversationEntity.getMunicipalityId(), messageExchangeNamespace, conversationEntity.getMessageExchangeId(), message.getId(), attachment.getId());
		saveAttachment(Long.valueOf(conversationEntity.getErrandId()), conversationEntity.getMunicipalityId(), conversationEntity.getNamespace(), file);
	}

	void saveAttachment(final Long errandId, final String municipalityId, final String namespace, final ResponseEntity<InputStreamResource> file) {
		if (file == null || file.getBody() == null || file.getHeaders().getContentType() == null) {
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
