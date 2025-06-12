package se.sundsvall.casedata.service.scheduler.messageexchange;

import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static se.sundsvall.casedata.service.util.mappers.ConversationMapper.RELATION_ID_KEY;

import generated.se.sundsvall.messageexchange.Conversation;
import generated.se.sundsvall.relation.Relation;
import generated.se.sundsvall.relation.ResourceIdentifier;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;
import se.sundsvall.casedata.integration.db.ConversationRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.MessageExchangeSyncRepository;
import se.sundsvall.casedata.integration.db.model.ConversationEntity;
import se.sundsvall.casedata.integration.db.model.MessageExchangeSyncEntity;
import se.sundsvall.casedata.integration.messageexchange.MessageExchangeClient;
import se.sundsvall.casedata.integration.relation.RelationClient;
import se.sundsvall.casedata.service.ConversationService;

@Component
public class MessageExchangeWorker {

	private final MessageExchangeClient messageExchangeClient;
	private final MessageExchangeSyncRepository messageExchangeSyncRepository;
	private final ConversationRepository conversationRepository;
	private final ConversationService conversationService;
	private final RelationClient relationClient;
	private final ErrandRepository errandRepository;

	@Value("${integration.message-exchange.namespace:casedata}")
	private String messageExchangeNamespace;

	public MessageExchangeWorker(final MessageExchangeClient messageExchangeClient, final MessageExchangeSyncRepository messageExchangeSyncRepository,
		final ConversationRepository conversationRepository, final ConversationService conversationService,
		final RelationClient relationClient, final ErrandRepository errandRepository) {

		this.messageExchangeClient = messageExchangeClient;
		this.messageExchangeSyncRepository = messageExchangeSyncRepository;
		this.conversationRepository = conversationRepository;
		this.conversationService = conversationService;
		this.relationClient = relationClient;
		this.errandRepository = errandRepository;
	}

	public List<MessageExchangeSyncEntity> getActiveSyncEntities() {
		return messageExchangeSyncRepository.findByActive(true);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveSyncEntity(MessageExchangeSyncEntity syncEntity) {
		messageExchangeSyncRepository.save(syncEntity);
	}

	public Page<Conversation> getConversations(MessageExchangeSyncEntity syncEntity, Pageable pageable) {
		return messageExchangeClient.getConversations(syncEntity.getMunicipalityId(), syncEntity.getNamespace(), "messages.sequenceNumber.id > ".concat(syncEntity.getLatestSyncedSequenceNumber().toString()), pageable).getBody();
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Conversation processConversation(Conversation conversation) {
		addNewUnsyncedConversationsToList(conversation, conversationRepository.findByMessageExchangeId(conversation.getId()))
			.forEach(conversationEntity -> conversationService.syncConversation(conversationEntity, conversation));
		return conversation;
	}

	private List<ConversationEntity> addNewUnsyncedConversationsToList(Conversation conversation, List<ConversationEntity> conversationEntities) {
		conversation.getExternalReferences().stream()
			.filter(keyValues -> keyValues.getKey() != null && keyValues.getKey().equals(RELATION_ID_KEY))
			.flatMap(keyValues -> keyValues.getValues().stream())
			.filter(isNotPresentInConversationRelations(conversationEntities))
			.map(relationId -> relationClient.getRelation(conversation.getMunicipalityId(), relationId))
			.filter(response -> response.getStatusCode().is2xxSuccessful())
			.map(HttpEntity::getBody)
			.filter(relationConnectedToCaseDataErrand())
			.map(createConversation(conversation))
			.forEach(conversationEntities::add);

		return conversationEntities;
	}

	private Predicate<String> isNotPresentInConversationRelations(List<ConversationEntity> conversationEntities) {
		return relationId -> conversationEntities.stream().flatMap(conversationEntity -> conversationEntity.getRelationIds().stream())
			.noneMatch(relationId::equals);
	}

	private Predicate<Relation> relationConnectedToCaseDataErrand() {
		return relation -> resourceIdentifierMatchesErrand(relation.getTarget()) || resourceIdentifierMatchesErrand(relation.getSource());
	}

	private boolean resourceIdentifierMatchesErrand(ResourceIdentifier resourceIdentifier) {
		return errandRepository.findByErrandNumber(resourceIdentifier.getResourceId()).isPresent();
	}

	private Function<Relation, ConversationEntity> createConversation(Conversation conversation) {
		return relation -> {
			var errand = errandRepository.findByErrandNumber(relation.getTarget().getResourceId())
				.or(() -> errandRepository.findByErrandNumber(relation.getSource().getResourceId()))
				.orElseThrow(() -> Problem.valueOf(INTERNAL_SERVER_ERROR, "Bug in relation filter"));
			return ConversationEntity.builder().withErrandId(errand.getId().toString())
				.withMessageExchangeId(conversation.getId())
				.withNamespace(messageExchangeNamespace)
				.withType("INTERNAL")
				.build();
		};
	}
}
