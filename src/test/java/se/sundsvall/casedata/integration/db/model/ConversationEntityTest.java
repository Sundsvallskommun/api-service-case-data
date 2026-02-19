package se.sundsvall.casedata.integration.db.model;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;

class ConversationEntityTest {

	@Test
	void testBean() {
		assertThat(ConversationEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void hasValidBuilderMethods() {

		final var id = "id";
		final var messageExchangeId = "messageExchangeId";
		final var errandId = "errandId";
		final var namespace = "namespace";
		final var municipalityId = "municipalityId";
		final var topic = "topic";
		final var type = "type";
		final var relationIds = List.of("relationId");
		final var latestSyncedSequenceNumber = 123L;
		final var targetRelationId = "targetRelationId";

		final var entity = ConversationEntity.builder()
			.withId(id)
			.withMessageExchangeId(messageExchangeId)
			.withErrandId(errandId)
			.withNamespace(namespace)
			.withMunicipalityId(municipalityId)
			.withTopic(topic)
			.withType(type)
			.withRelationIds(relationIds)
			.withLatestSyncedSequenceNumber(latestSyncedSequenceNumber)
			.withTargetRelationId(targetRelationId)
			.build();

		Assertions.assertThat(entity).hasNoNullFieldsOrProperties();
		Assertions.assertThat(entity.getId()).isEqualTo(id);
		Assertions.assertThat(entity.getMessageExchangeId()).isEqualTo(messageExchangeId);
		Assertions.assertThat(entity.getErrandId()).isEqualTo(errandId);
		Assertions.assertThat(entity.getNamespace()).isEqualTo(namespace);
		Assertions.assertThat(entity.getMunicipalityId()).isEqualTo(municipalityId);
		Assertions.assertThat(entity.getTopic()).isEqualTo(topic);
		Assertions.assertThat(entity.getType()).isEqualTo(type);
		Assertions.assertThat(entity.getRelationIds()).isEqualTo(relationIds);
		Assertions.assertThat(entity.getLatestSyncedSequenceNumber()).isEqualTo(latestSyncedSequenceNumber);
		Assertions.assertThat(entity.getTargetRelationId()).isEqualTo(targetRelationId);
	}
}
