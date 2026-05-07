package se.sundsvall.casedata.service.scheduler.messageexchange;

import com.google.common.primitives.Longs;
import generated.se.sundsvall.messageexchange.Conversation;
import generated.se.sundsvall.relation.ResourceIdentifier;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.casedata.integration.db.ConversationRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.MessageExchangeSyncRepository;
import se.sundsvall.casedata.integration.db.model.ConversationEntity;
import se.sundsvall.casedata.integration.db.model.MessageExchangeSyncEntity;
import se.sundsvall.casedata.integration.messageexchange.MessageExchangeClient;
import se.sundsvall.casedata.integration.relation.RelationClient;
import se.sundsvall.casedata.service.MessageExchangeSyncService;
import se.sundsvall.dept44.problem.Problem;

import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static se.sundsvall.casedata.service.util.mappers.ConversationMapper.RELATION_ID_KEY;

@Component
public class MessageExchangeWorker {

	private final MessageExchangeClient messageExchangeClient;
	private final MessageExchangeSyncRepository messageExchangeSyncRepository;
	private final ConversationRepository conversationRepository;
	private final RelationClient relationClient;
	private final ErrandRepository errandRepository;
	private final MessageExchangeSyncService messageExchangeSyncService;
	private static final String THIS_SERVICE = "casedata";

	public MessageExchangeWorker(final MessageExchangeClient messageExchangeClient, final MessageExchangeSyncRepository messageExchangeSyncRepository,
		final ConversationRepository conversationRepository,
		final RelationClient relationClient, final ErrandRepository errandRepository, final MessageExchangeSyncService messageExchangeSyncService) {

		this.messageExchangeClient = messageExchangeClient;
		this.messageExchangeSyncRepository = messageExchangeSyncRepository;
		this.conversationRepository = conversationRepository;
		this.relationClient = relationClient;
		this.errandRepository = errandRepository;
		this.messageExchangeSyncService = messageExchangeSyncService;
	}

	public List<MessageExchangeSyncEntity> getActiveSyncEntities() {
		return messageExchangeSyncRepository.findByActive(true);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveSyncEntity(final MessageExchangeSyncEntity syncEntity) {
		messageExchangeSyncRepository.save(syncEntity);
	}

	public Page<Conversation> getConversations(final MessageExchangeSyncEntity syncEntity, final Pageable pageable) {
		return messageExchangeClient.getConversations(syncEntity.getMunicipalityId(), syncEntity.getNamespace(), "messages.sequenceNumber.id > ".concat(syncEntity.getLatestSyncedSequenceNumber().toString()), pageable).getBody();
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Conversation processConversation(final Conversation conversation) {
		addNewUnsyncedConversationsToList(conversation, conversationRepository.findByMessageExchangeId(conversation.getId()))
			.forEach(conversationEntity -> messageExchangeSyncService.syncConversation(conversationEntity, conversation));
		return conversation;
	}

	/**
	 * Appends conversation to conversationEntities for conversation where target relation points to case-data errand and is
	 * not created.
	 *
	 * @param  conversation         Conversation that is processed
	 * @param  conversationEntities List of existing conversations
	 * @return                      conversationEntities with possible added conversations
	 */
	private List<ConversationEntity> addNewUnsyncedConversationsToList(final Conversation conversation, final List<ConversationEntity> conversationEntities) {
		conversation.getExternalReferences().stream()
			.filter(keyValues -> keyValues.getKey() != null && keyValues.getKey().equals(RELATION_ID_KEY))
			.flatMap(keyValues -> keyValues.getValues().stream())
			.map(relationId -> relationClient.getRelation(conversation.getMunicipalityId(), relationId))
			.filter(response -> response.getStatusCode().is2xxSuccessful())
			.map(HttpEntity::getBody)
			.filter(Objects::nonNull)
			.flatMap(relation -> Stream.of(relation.getTarget(), relation.getSource()))
			.filter(Objects::nonNull)
			.filter(resourceIdentifierMatchesErrand())
			.filter(resourceIdentifierDoesNotExistInEntityList(conversationEntities))
			.map(createConversation(conversation))
			.forEach(conversationEntities::add);

		return conversationEntities;
	}

	private Predicate<ResourceIdentifier> resourceIdentifierDoesNotExistInEntityList(final List<ConversationEntity> conversationEntities) {
		// Check if conversation is already present in the list
		return identifier -> conversationEntities.stream().map(ConversationEntity::getErrandId).noneMatch(errandId -> errandId.equals(identifier.getResourceId()));
	}

	private Predicate<ResourceIdentifier> resourceIdentifierMatchesErrand() {
		return resourceIdentifier -> {
			final var service = ofNullable(resourceIdentifier.getService()).orElse("").replace("-", "").replace("_", "");
			return THIS_SERVICE.equalsIgnoreCase(service) && Longs.tryParse(resourceIdentifier.getResourceId()) != null && errandRepository.findById(Long.parseLong(resourceIdentifier.getResourceId())).isPresent();
		};
	}

	private Function<ResourceIdentifier, ConversationEntity> createConversation(final Conversation conversation) {
		return identifier -> {
			final var errand = errandRepository.findById(Long.parseLong(identifier.getResourceId()))
				.orElseThrow(() -> Problem.valueOf(INTERNAL_SERVER_ERROR, "Bug in relation filter"));
			return ConversationEntity.builder().withErrandId(errand.getId().toString())
				.withMessageExchangeId(conversation.getId())
				.withNamespace(errand.getNamespace())
				.withMunicipalityId(errand.getMunicipalityId())
				.withType("INTERNAL")
				.build();
		};
	}
}
