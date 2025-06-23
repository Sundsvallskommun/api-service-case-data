package se.sundsvall.casedata.service;

import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static se.sundsvall.casedata.service.util.mappers.ConversationMapper.toAttachment;
import static se.sundsvall.casedata.service.util.mappers.ConversationMapper.toConversation;
import static se.sundsvall.casedata.service.util.mappers.ConversationMapper.updateConversationEntity;

import generated.se.sundsvall.messageexchange.Message;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.zalando.problem.Problem;
import se.sundsvall.casedata.api.model.Attachment;
import se.sundsvall.casedata.api.model.conversation.Conversation;
import se.sundsvall.casedata.integration.db.ConversationRepository;
import se.sundsvall.casedata.integration.db.model.ConversationEntity;
import se.sundsvall.casedata.integration.messageexchange.MessageExchangeClient;
import se.sundsvall.casedata.service.util.ConversationEvent;
import se.sundsvall.dept44.requestid.RequestId;

@Service
public class MessageExchangeSyncService {

	private final MessageExchangeClient messageExchangeClient;
	private final AttachmentService attachmentService;
	private final ConversationRepository conversationRepository;
	private final ApplicationEventPublisher applicationEventPublisher;

	@Value("${integration.message-exchange.namespace:casedata}")
	private String messageExchangeNamespace;

	public MessageExchangeSyncService(final MessageExchangeClient messageExchangeClient, final AttachmentService attachmentService, final ConversationRepository conversationRepository, final ApplicationEventPublisher applicationEventPublisher) {
		this.messageExchangeClient = messageExchangeClient;
		this.attachmentService = attachmentService;
		this.conversationRepository = conversationRepository;
		this.applicationEventPublisher = applicationEventPublisher;
	}

	public Conversation syncConversation(final ConversationEntity conversationEntity, final generated.se.sundsvall.messageexchange.Conversation conversation) {
		// TODO: Create notification if sequence number is not the latest
		applicationEventPublisher.publishEvent(ConversationEvent.builder().withConversationEntity(conversationEntity).withRequestId(RequestId.get()).build());
		final var updatedConversation = toConversation(conversationEntity, conversation);
		conversationRepository.save(updateConversationEntity(conversationEntity, conversation));
		return updatedConversation;
	}

	@TransactionalEventListener
	void syncMessages(final ConversationEvent conversationEvent) {
		final var conversationEntity = conversationEvent.getConversationEntity();
		RequestId.init(conversationEvent.getRequestId());

		final var filter = "sequenceNumber >" + conversationEntity.getLatestSyncedSequenceNumber();

		final var response = messageExchangeClient.getMessages(conversationEntity.getMunicipalityId(), conversationEntity.getNamespace(), conversationEntity.getMessageExchangeId(), filter, Pageable.unpaged());

		if (response == null || !response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to retrieve messages from Message Exchange");
		}

		response.getBody().forEach(message -> message.getAttachments().forEach(attachment -> syncAttachment(conversationEntity, message, attachment)));
	}

	void syncAttachment(final ConversationEntity conversationEntity, final Message message, final generated.se.sundsvall.messageexchange.Attachment attachment) {
		final var file = messageExchangeClient.readErrandAttachment(conversationEntity.getMunicipalityId(), messageExchangeNamespace, conversationEntity.getMessageExchangeId(), message.getId(), attachment.getId());
		saveAttachment(Long.valueOf(conversationEntity.getErrandId()), conversationEntity.getMunicipalityId(), conversationEntity.getNamespace(), file);
	}

	void saveAttachment(final Long errandId, final String municipalityId, final String namespace, final ResponseEntity<InputStreamResource> file) {
		if (file.getBody() == null || file.getHeaders().getContentType() == null) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to retrieve attachment from Message Exchange");
		}
		final Attachment attachment;
		try {
			attachment = toAttachment(file.getBody().getContentAsByteArray(), file.getBody().getFilename(), file.getHeaders().getContentType().toString(), errandId, municipalityId, namespace);
		} catch (final IOException e) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to convert attachment from Message Exchange");
		}
		attachmentService.create(errandId, attachment, municipalityId, namespace);
	}
}
