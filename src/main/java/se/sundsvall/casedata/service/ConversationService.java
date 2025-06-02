package se.sundsvall.casedata.service;

import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.mappers.ConversationMapper.toConversation;
import static se.sundsvall.casedata.service.util.mappers.ConversationMapper.toConversationEntity;
import static se.sundsvall.casedata.service.util.mappers.ConversationMapper.toMessageExchangeConversation;
import static se.sundsvall.casedata.service.util.mappers.ConversationMapper.toMessagePage;
import static se.sundsvall.casedata.service.util.mappers.ConversationMapper.toMessageRequest;
import static se.sundsvall.casedata.service.util.mappers.ConversationMapper.updateConversationEntity;

import java.util.List;
import java.util.Optional;
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

	private final ConversationRepository conversationRepository;
	private final MessageExchangeClient messageExchangeClient;

	public ConversationService(final ConversationRepository conversationRepository, final MessageExchangeClient messageExchangeClient) {
		this.conversationRepository = conversationRepository;
		this.messageExchangeClient = messageExchangeClient;
	}

	public String createConversation(final String municipalityId, final String namespace, final Long errandId, final Conversation conversation) {

		final var response = messageExchangeClient.createConversation(municipalityId, namespace, toMessageExchangeConversation(conversation));

		if (!response.getStatusCode().is2xxSuccessful()) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to create conversation in Message Exchange");
		}

		final var messageExchangeId = Optional.ofNullable(response.getHeaders().getLocation())
			.map(location -> location.getPath().substring(location.getPath().lastIndexOf('/') + 1))
			.orElseThrow(() -> Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to create conversation in Message Exchange"));

		final var conversationEntity = toConversationEntity(conversation, municipalityId, namespace, errandId, messageExchangeId);
		return conversationRepository.save(conversationEntity).getId();
	}

	public Conversation getConversation(final String municipalityId, final String namespace, final Long errandId, final String conversationId) {

		final var entity = getConversationEntity(municipalityId, namespace, errandId, conversationId);

		final var response = messageExchangeClient.getConversation(municipalityId, namespace, entity.getMessageExchangeId());

		if (!response.getStatusCode().is2xxSuccessful()) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to retrieve conversation from Message Exchange");
		}

		if (response.getBody() == null) {
			throw Problem.valueOf(NOT_FOUND, "Conversation not found in Message Exchange");
		}

		return toConversation(entity, response.getBody());
	}

	public List<Conversation> getConversations(final String municipalityId, final String namespace, final Long errandId) {

		final var conversations = conversationRepository.findByMunicipalityIdAndNamespaceAndErrandId(municipalityId, namespace, errandId.toString());

		if (conversations.isEmpty()) {
			throw Problem.valueOf(NOT_FOUND, "No conversations found for the given parameters");
		}

		return conversations.stream()
			.map(conversationEntity -> {

				final var response = messageExchangeClient.getConversation(municipalityId, namespace, conversationEntity.getMessageExchangeId());

				if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
					throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to retrieve conversation from Message Exchange");
				}
				return toConversation(conversationEntity, response.getBody());
			})
			.toList();

	}

	public Conversation updateConversation(final String municipalityId, final String namespace, final Long errandId, final String conversationId, final Conversation request) {

		final var entity = getConversationEntity(municipalityId, namespace, errandId, conversationId);

		final var response = messageExchangeClient.updateConversation(municipalityId, namespace, entity.getMessageExchangeId(), toMessageExchangeConversation(request));

		if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to update conversation in Message Exchange");
		}

		updateConversationEntity(entity, request);
		conversationRepository.save(entity);
		return toConversation(entity, response.getBody());
	}

	public void createMessage(final String municipalityId, final String namespace, final long errandId, final String conversationId, final Message messageRequest, final List<MultipartFile> attachments) {
		final var entity = getConversationEntity(municipalityId, namespace, errandId, conversationId);

		final var response = messageExchangeClient.createMessage(municipalityId, namespace, entity.getMessageExchangeId(), toMessageRequest(messageRequest), attachments);

		if (!response.getStatusCode().is2xxSuccessful()) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to create message in Message Exchange");
		}
	}

	public Page<Message> getMessages(final String municipalityId, final String namespace, final Long errandId, final String conversationId, final Pageable pageable) {
		final var conversationEntity = getConversationEntity(municipalityId, namespace, errandId, conversationId);
		final var response = messageExchangeClient.getMessages(municipalityId, namespace, conversationEntity.getMessageExchangeId(), pageable);

		if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to retrieve messages from Message Exchange");
		}

		return toMessagePage(response.getBody());
	}

	private ConversationEntity getConversationEntity(final String municipalityId, final String namespace, final Long errandId, final String conversationId) {
		return conversationRepository.findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, errandId.toString(), conversationId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Conversation not found in local database"));
	}
}
