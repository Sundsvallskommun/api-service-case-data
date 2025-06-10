package se.sundsvall.casedata.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.casedata.api.filter.IncomingRequestFilter;
import se.sundsvall.casedata.integration.db.config.JaversConfiguration;
import se.sundsvall.casedata.integration.db.listeners.ErrandListener;
import se.sundsvall.casedata.integration.db.model.ConversationEntity;

@DataJpaTest
@Import(value = {
	JaversConfiguration.class, ErrandListener.class, IncomingRequestFilter.class
})
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-junit.sql"
})
class ConversationRepositoryTest {

	@Autowired
	private ConversationRepository repository;

	@Test
	void create() {
		final var messageExchangeId = "messageExchangeId";
		final var errandId = "errandId";
		final var namespace = "namespace";
		final var municipalityId = "1234";
		final var topic = "topic";
		final var type = "type";
		final var relationIds = List.of("relationId");
		final var latestSyncedSequenceNumber = 123L;

		final var entity = ConversationEntity.builder()
			.withMessageExchangeId(messageExchangeId)
			.withErrandId(errandId)
			.withNamespace(namespace)
			.withMunicipalityId(municipalityId)
			.withTopic(topic)
			.withType(type)
			.withRelationIds(relationIds)
			.withLatestSyncedSequenceNumber(latestSyncedSequenceNumber)
			.build();

		final var persistedEntity = repository.saveAndFlush(entity);

		assertThat(persistedEntity).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(persistedEntity.getMessageExchangeId()).isEqualTo(messageExchangeId);
		assertThat(persistedEntity.getErrandId()).isEqualTo(errandId);
		assertThat(persistedEntity.getNamespace()).isEqualTo(namespace);
		assertThat(persistedEntity.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(persistedEntity.getTopic()).isEqualTo(topic);
		assertThat(persistedEntity.getType()).isEqualTo(type);
		assertThat(persistedEntity.getRelationIds()).containsExactlyElementsOf(relationIds);
		assertThat(persistedEntity.getLatestSyncedSequenceNumber()).isEqualTo(latestSyncedSequenceNumber);
	}

	@Test
	void findByMunicipalityIdAndNamespaceAndErrandId() {
		var conversations = repository.findByMunicipalityIdAndNamespaceAndErrandId("2281", "namespace-1", "errand_id-1");

		assertThat(conversations)
			.hasSize(2)
			.extracting(ConversationEntity::getId, ConversationEntity::getMunicipalityId, ConversationEntity::getNamespace, ConversationEntity::getErrandId).containsExactly(
				tuple("1", "2281", "namespace-1", "errand_id-1"),
				tuple("2", "2281", "namespace-1", "errand_id-1"));
	}

	@Test
	void findByMessageExchangeId() {
		var optionalConversation = repository.findByMessageExchangeId("message_exchange_id-3");

		assertThat(optionalConversation).isNotEmpty().hasValueSatisfying(conversation -> {
			assertThat(conversation).extracting(
				ConversationEntity::getId,
				ConversationEntity::getMunicipalityId,
				ConversationEntity::getNamespace,
				ConversationEntity::getErrandId,
				ConversationEntity::getMessageExchangeId)
				.containsExactly("3", "2281", "namespace-1", "errand_id-3", "message_exchange_id-3");
		});
	}
}
