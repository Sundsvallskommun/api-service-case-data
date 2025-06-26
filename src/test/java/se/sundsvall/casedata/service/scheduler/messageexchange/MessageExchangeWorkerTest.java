package se.sundsvall.casedata.service.scheduler.messageexchange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.service.util.mappers.ConversationMapper.RELATION_ID_KEY;

import generated.se.sundsvall.messageexchange.Conversation;
import generated.se.sundsvall.messageexchange.KeyValues;
import generated.se.sundsvall.relation.Relation;
import generated.se.sundsvall.relation.ResourceIdentifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import se.sundsvall.casedata.integration.db.ConversationRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.MessageExchangeSyncRepository;
import se.sundsvall.casedata.integration.db.model.ConversationEntity;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.MessageExchangeSyncEntity;
import se.sundsvall.casedata.integration.messageexchange.MessageExchangeClient;
import se.sundsvall.casedata.integration.relation.RelationClient;
import se.sundsvall.casedata.service.MessageExchangeSyncService;

@ExtendWith(MockitoExtension.class)
class MessageExchangeWorkerTest {

	@Mock
	MessageExchangeSyncService messageExchangeSyncServiceMock;
	@Mock
	private MessageExchangeClient messageExchangeClientMock;
	@Mock
	private MessageExchangeSyncRepository messageExchangeSyncRepositoryMock;
	@Mock
	private ConversationRepository conversationRepositoryMock;
	@Mock
	private RelationClient relationClientMock;
	@Mock
	private ErrandRepository errandRepositoryMock;

	@Captor
	private ArgumentCaptor<ConversationEntity> conversationEntityArgumentCaptor;

	@InjectMocks
	private MessageExchangeWorker messageExchangeWorker;

	@Test
	void getActiveSyncEntities() {
		final var entity = MessageExchangeSyncEntity.builder().build();
		when(messageExchangeSyncRepositoryMock.findByActive(any())).thenReturn(List.of(entity));

		final var list = messageExchangeWorker.getActiveSyncEntities();

		verify(messageExchangeSyncRepositoryMock).findByActive(true);
		assertThat(list).hasSize(1).first().isSameAs(entity);
	}

	@Test
	void saveSyncEntity() {
		final var entity = MessageExchangeSyncEntity.builder().build();

		messageExchangeWorker.saveSyncEntity(entity);

		verify(messageExchangeSyncRepositoryMock).save(same(entity));
	}

	@Test
	void getConversation() {
		final var entity = MessageExchangeSyncEntity.builder()
			.withMunicipalityId("municipalityId")
			.withNamespace("namespace")
			.withLatestSyncedSequenceNumber(33L)
			.build();
		final var pageableMock = Mockito.mock(Pageable.class);
		final var conversationPage = new PageImpl<>(List.of(new Conversation()));
		when(messageExchangeClientMock.getConversations(any(), any(), any(), any())).thenReturn(ResponseEntity.ok(conversationPage));

		final var result = messageExchangeWorker.getConversations(entity, pageableMock);

		verify(messageExchangeClientMock).getConversations(eq("municipalityId"), eq("namespace"), eq("messages.sequenceNumber.id > 33"), same(pageableMock));
		assertThat(result).isSameAs(conversationPage);
	}

	@Test
	void processConversation() {
		final var conversation = new Conversation();
		conversation.setExternalReferences(List.of(new KeyValues().key(RELATION_ID_KEY).addValuesItem("1").addValuesItem("2")));
		conversation.setMunicipalityId("municipalityId");
		conversation.setId("conversationId");
		final var conversationEntities = new ArrayList<ConversationEntity>();
		conversationEntities.add(ConversationEntity.builder()
			.withMunicipalityId("municipalityId-existing")
			.withNamespace("case-data-namespace-existing")
			.withId("existingConversationEntityId")
			.withMessageExchangeId("existingMessageExchangeId")
			.withRelationIds(List.of("1"))
			.build());

		when(conversationRepositoryMock.findByMessageExchangeId(any())).thenReturn(conversationEntities);
		when(relationClientMock.getRelation(any(), any())).thenReturn(
			ResponseEntity.ok(new Relation(null, new ResourceIdentifier().resourceId("other-id"), new ResourceIdentifier().resourceId("123").service("case-data"))), // relation 1
			ResponseEntity.ok(new Relation(null, new ResourceIdentifier().resourceId("existingConversationEntityId"), new ResourceIdentifier().resourceId("other-id")))); // relation 2
		when(errandRepositoryMock.findById(123L)).thenReturn(Optional.of(ErrandEntity.builder().withMunicipalityId("municipalityId").withNamespace("case-data-namespace").withId(123L).build()));

		messageExchangeWorker.processConversation(conversation);

		verify(conversationRepositoryMock).findByMessageExchangeId("conversationId");
		verify(relationClientMock).getRelation("municipalityId", "1");
		verify(relationClientMock).getRelation("municipalityId", "2");
		verify(errandRepositoryMock, times(2)).findById(123L);
		verify(messageExchangeSyncServiceMock, times(2)).syncConversation(conversationEntityArgumentCaptor.capture(), same(conversation));
		assertThat(conversationEntityArgumentCaptor.getAllValues()).hasSize(2)
			.extracting(ConversationEntity::getMunicipalityId, ConversationEntity::getNamespace, ConversationEntity::getId, ConversationEntity::getMessageExchangeId)
			.containsExactly(
				tuple("municipalityId-existing", "case-data-namespace-existing", "existingConversationEntityId", "existingMessageExchangeId"),
				tuple("municipalityId", "case-data-namespace", null, "conversationId"));

		verifyNoMoreInteractions(conversationRepositoryMock, relationClientMock, errandRepositoryMock, messageExchangeSyncServiceMock, messageExchangeClientMock, messageExchangeSyncRepositoryMock);
	}
}
