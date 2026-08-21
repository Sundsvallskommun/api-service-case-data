package se.sundsvall.casedata.service;

import generated.se.sundsvall.messageexchange.Message;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import se.sundsvall.casedata.integration.db.AttachmentRepository;
import se.sundsvall.casedata.integration.db.ConversationRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.ConversationEntity;
import se.sundsvall.casedata.integration.db.model.enums.Channel;
import se.sundsvall.casedata.integration.messageexchange.MessageExchangeClient;
import se.sundsvall.casedata.service.util.Base64MultipartFile;
import se.sundsvall.dept44.problem.Problem;

import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static se.sundsvall.casedata.integration.db.model.enums.NotificationSubType.MESSAGE;
import static se.sundsvall.casedata.service.util.mappers.ConversationMapper.toAttachment;
import static se.sundsvall.casedata.service.util.mappers.ConversationMapper.updateConversationEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNotification;

@Service
public class MessageExchangeSyncService {

	private static final Logger LOG = LoggerFactory.getLogger(MessageExchangeSyncService.class);
	private static final String NOTIFICATION_TYPE_UPDATE = "UPDATE";
	private static final String NOTIFICATION_DESCRIPTION = "Ny händelse för %s";

	private final MessageExchangeClient messageExchangeClient;
	private final AttachmentService attachmentService;
	private final AttachmentRepository attachmentRepository;
	private final ConversationRepository conversationRepository;
	private final ErrandRepository errandRepository;
	private final NotificationService notificationService;

	@Value("${integration.message-exchange.namespace:casedata}")
	private String messageExchangeNamespace;

	public MessageExchangeSyncService(
		final MessageExchangeClient messageExchangeClient,
		final AttachmentService attachmentService,
		final AttachmentRepository attachmentRepository,
		final ConversationRepository conversationRepository,
		final ErrandRepository errandRepository,
		final NotificationService notificationService) {

		this.messageExchangeClient = messageExchangeClient;
		this.attachmentService = attachmentService;
		this.attachmentRepository = attachmentRepository;
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
		final var errandId = Long.valueOf(conversationEntity.getErrandId());

		if (attachment.getHash() != null && attachmentRepository.existsByErrandIdAndMunicipalityIdAndNamespaceAndHash(errandId, conversationEntity.getMunicipalityId(), conversationEntity.getNamespace(), attachment.getHash())) {
			return;
		}

		final var file = messageExchangeClient.readErrandAttachment(conversationEntity.getMunicipalityId(), messageExchangeNamespace, conversationEntity.getMessageExchangeId(), message.getId(), attachment.getId());
		saveAttachment(errandId, conversationEntity.getMunicipalityId(), conversationEntity.getNamespace(), file, resolveChannel(message.getCreatedBy()));
	}

	static Channel resolveChannel(final generated.se.sundsvall.messageexchange.Identifier createdBy) {
		return ofNullable(createdBy)
			.map(generated.se.sundsvall.messageexchange.Identifier::getType)
			.map(type -> switch (type)
			{
				case "adAccount" -> Channel.WEB_UI;
				case "partyId" -> Channel.MY_PAGES;
				default -> null;
			})
			.orElse(null);
	}

	void saveAttachment(final Long errandId, final String municipalityId, final String namespace, final ResponseEntity<InputStreamResource> file, final Channel channel) {
		if (file == null || file.getBody() == null || file.getHeaders().getContentType() == null) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to retrieve attachment from Message Exchange");
		}
		final var filename = file.getHeaders().getContentDisposition().getFilename();
		final var mimeType = file.getHeaders().getContentType().toString();

		final byte[] content;
		try {
			content = file.getBody().getContentAsByteArray();
		} catch (final IOException _) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to convert attachment from Message Exchange");
		}

		// An attachment without content is indistinguishable from an empty file once stored and is served as a broken
		// download, so it is skipped rather than persisted. Skipping instead of failing keeps the sync moving: an
		// exception here would leave latestSyncedSequenceNumber untouched and retry the same attachment forever.
		if (content.length == 0) {
			LOG.warn("Message Exchange returned no content for attachment '{}' on errand {} - no attachment created", filename, errandId);
			return;
		}

		final var attachment = toAttachment(filename, mimeType, errandId, municipalityId, namespace, channel);
		final var multipartFile = new Base64MultipartFile(filename, filename, mimeType, content);
		attachmentService.create(errandId, attachment, multipartFile, municipalityId, namespace);
	}
}
