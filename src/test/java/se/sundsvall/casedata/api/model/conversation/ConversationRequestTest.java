package se.sundsvall.casedata.api.model.conversation;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.util.List;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class ConversationRequestTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(ConversationRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {

		// Arrange
		final var topic = "The conversation topic";
		final var type = ConversationType.INTERNAL;
		final var relationIds = List.of("relation-id-1", "relation-id-2");
		final var participants = List.of(Identifier.builder().build());
		final var externalReferences = List.of(KeyValues.builder().build());
		final var metadata = List.of(KeyValues.builder().build());

		// Act
		final var result = ConversationRequest.builder()
			.withTopic(topic)
			.withType(type)
			.withRelationIds(relationIds)
			.withParticipants(participants)
			.withExternalReferences(externalReferences)
			.withMetadata(metadata)
			.build();

		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getTopic()).isEqualTo(topic);
		assertThat(result.getType()).isEqualTo(type);
		assertThat(result.getRelationIds()).isEqualTo(relationIds);
		assertThat(result.getParticipants()).isEqualTo(participants);
		assertThat(result.getExternalReferences()).isEqualTo(externalReferences);
		assertThat(result.getMetadata()).isEqualTo(metadata);

	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(ConversationRequest.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new ConversationRequest()).hasAllNullFieldsOrProperties();
	}

}
