package se.sundsvall.casedata.service;

import static java.util.Collections.emptyList;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.mappers.ConversationMapper.toAttachment;
import static se.sundsvall.casedata.service.util.mappers.ConversationMapper.toConversation;
import static se.sundsvall.casedata.service.util.mappers.ConversationMapper.toConversationEntity;
import static se.sundsvall.casedata.service.util.mappers.ConversationMapper.toConversationList;
import static se.sundsvall.casedata.service.util.mappers.ConversationMapper.toMessageExchangeConversation;
import static se.sundsvall.casedata.service.util.mappers.ConversationMapper.toMessagePage;
import static se.sundsvall.casedata.service.util.mappers.ConversationMapper.toMessageRequest;
import static se.sundsvall.casedata.service.util.mappers.ConversationMapper.updateConversationEntity;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.zalando.problem.Problem;
import se.sundsvall.casedata.api.model.conversation.Conversation;
import se.sundsvall.casedata.api.model.conversation.Message;
import se.sundsvall.casedata.integration.db.ConversationRepository;
import se.sundsvall.casedata.integration.db.model.ConversationEntity;
import se.sundsvall.casedata.integration.messageexchange.MessageExchangeClient;

@Service
public class ConversationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConversationService.class);
	private final ConversationRepository conversationRepository;
	private final MessageExchangeClient messageExchangeClient;
	private final AttachmentService attachmentService;
	private final MessageService messageService;
	private final MessageExchangeSyncService messageExchangeSyncService;
	private final ApplicationEventPublisher applicationEventPublisher;

	@Value("${integration.message-exchange.namespace:casedata}")
	private String messageExchangeNamespace;

	public ConversationService(final ConversationRepository conversationRepository, final MessageExchangeClient messageExchangeClient, final AttachmentService attachmentService, final MessageService messageService,
		final MessageExchangeSyncService messageExchangeSyncService, final ApplicationEventPublisher applicationEventPublisher) {
		this.conversationRepository = conversationRepository;
		this.messageExchangeClient = messageExchangeClient;
		this.attachmentService = attachmentService;
		this.messageService = messageService;
		this.messageExchangeSyncService = messageExchangeSyncService;
		this.applicationEventPublisher = applicationEventPublisher;
	}

	public String createConversation(final String municipalityId, final String namespace, final Long errandId, final Conversation conversation) {

		final var response = messageExchangeClient.createConversation(municipalityId, messageExchangeNamespace, toMessageExchangeConversation(conversation));

		if (!response.getStatusCode().is2xxSuccessful()) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to create conversation in Message Exchange");
		}

		final var messageExchangeId = Optional.ofNullable(response.getHeaders().getLocation())
			.map(location -> location.getPath().substring(location.getPath().lastIndexOf('/') + 1))
			.orElseThrow(() -> Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to create conversation in Message Exchange"));

		// TODO: Create notification
		final var conversationEntity = toConversationEntity(conversation, municipalityId, namespace, errandId, messageExchangeId);
		return conversationRepository.save(conversationEntity).getId();
	}

	public Conversation getConversation(final String municipalityId, final String namespace, final Long errandId, final String conversationId) {

		final var entity = getConversationEntity(municipalityId, namespace, errandId, conversationId);

		final var response = messageExchangeClient.getConversation(municipalityId, messageExchangeNamespace, entity.getMessageExchangeId());

		if (!response.getStatusCode().is2xxSuccessful()) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to retrieve conversation from Message Exchange");
		}

		if (response.getBody() == null) {
			throw Problem.valueOf(NOT_FOUND, "Conversation not found in Message Exchange");
		}

		return syncConversation(entity, response.getBody());
	}

	public Conversation syncConversation(final ConversationEntity conversationEntity, final generated.se.sundsvall.messageexchange.Conversation conversation) {
		// TODO: Create notification if sequence number is not the latest
		final var updatedConversation = toConversation(conversationEntity, conversation);
		conversationRepository.save(updateConversationEntity(conversationEntity, conversation));
		return updatedConversation;
	}

	public List<Conversation> getConversations(final String municipalityId, final String namespace, final Long errandId) {

		final var conversations = conversationRepository.findByMunicipalityIdAndNamespaceAndErrandId(municipalityId, namespace, errandId.toString());

		return toConversationList(conversations);
	}

	public Conversation updateConversation(final String municipalityId, final String namespace, final Long errandId, final String conversationId, final Conversation request) {

		final var entity = getConversationEntity(municipalityId, namespace, errandId, conversationId);

		final var response = messageExchangeClient.updateConversation(municipalityId, messageExchangeNamespace, entity.getMessageExchangeId(), toMessageExchangeConversation(request));

		if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to update conversation in Message Exchange");
		}
		// TODO: Create notification
		updateConversationEntity(entity, request);
		conversationRepository.save(entity);
		return toConversation(entity, response.getBody());
	}

	public void createMessage(final String municipalityId, final String namespace, final long errandId, final String conversationId, final Message messageRequest, final List<MultipartFile> attachments) {
		final var entity = getConversationEntity(municipalityId, namespace, errandId, conversationId);
		final var response = messageExchangeClient.createMessage(municipalityId, messageExchangeNamespace, entity.getMessageExchangeId(), toMessageRequest(messageRequest), attachments);

		if (!response.getStatusCode().is2xxSuccessful()) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to create message in Message Exchange");
		}
		Optional.ofNullable(attachments).orElse(emptyList()).forEach(attachment -> saveAttachment(errandId, municipalityId, namespace, attachment));

		try {
			messageService.sendMessageNotification(municipalityId, namespace, errandId);
		} catch (final Exception e) {
			LOGGER.error("Failed to send message notification", e);
		}
	}

	public Page<Message> getMessages(final String municipalityId, final String namespace, final Long errandId, final String conversationId, final Pageable pageable) {
		final var conversationEntity = getConversationEntity(municipalityId, namespace, errandId, conversationId);
		final var response = messageExchangeClient.getMessages(municipalityId, messageExchangeNamespace, conversationEntity.getMessageExchangeId(), null, pageable);
		if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to retrieve messages from Message Exchange");
		}
		applicationEventPublisher.publishEvent(conversationEntity);
		return toMessagePage(response.getBody());
	}

	private ConversationEntity getConversationEntity(final String municipalityId, final String namespace, final Long errandId, final String conversationId) {
		return conversationRepository.findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, errandId.toString(), conversationId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Conversation not found in local database"));
	}

	private void saveAttachment(final Long errandId, final String municipalityId, final String namespace, final MultipartFile attachment) {

		attachmentService.create(errandId, toAttachment(attachment, errandId, municipalityId, namespace), municipalityId, namespace);
	}

	public void getConversationMessageAttachment(
		final String municipalityId, final String namespace, final Long errandId,
		final String conversationId, final String messageId, final String attachmentId,
		final HttpServletResponse response) throws IOException {

		final var conversationEntity = getConversationEntity(municipalityId, namespace, errandId, conversationId);
		final var exchangeId = conversationEntity.getMessageExchangeId();
		if (exchangeId == null) {
			throw Problem.valueOf(NOT_FOUND, "Conversation not found in local database");
		}
		applicationEventPublisher.publishEvent(conversationEntity);

		final var attachmentResponse = messageExchangeClient.readErrandAttachment(
			municipalityId, messageExchangeNamespace, exchangeId, messageId, attachmentId);

		final var body = attachmentResponse.getBody();
		final var contentType = attachmentResponse.getHeaders().getContentType();

		if (!attachmentResponse.getStatusCode().is2xxSuccessful() || body == null || contentType == null) {
			throw Problem.valueOf(NOT_FOUND, "Attachment not found or invalid in Message Exchange");
		}

		response.setContentType(contentType.toString());

		response.setHeader("Content-Disposition", "attachment; filename=\"" + body.getFilename() + "\"");
		response.setContentLengthLong(attachmentResponse.getHeaders().getContentLength());

		try (final var in = body.getInputStream(); final var out = response.getOutputStream()) {
			in.transferTo(out);
			out.flush();
		}
	}

}
