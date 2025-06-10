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

class ConversationTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(Conversation.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {
		// Arrange
		final var id = "1aefbbb8-de82-414b-b5d7-ba7c5bbe4506";
		final var topic = "The conversation topic";
		final var type = ConversationType.INTERNAL;
		final var relationIds = List.of("relation-id-1", "relation-id-2");
		final var participants = List.of(Identifier.builder().build());
		final var metadata = List.of(KeyValues.builder().build());

		// Act
		final var result = Conversation.builder()
			.withId(id)
			.withTopic(topic)
			.withType(type)
			.withRelationIds(relationIds)
			.withParticipants(participants)
			.withMetadata(metadata)
			.build();

		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getTopic()).isEqualTo(topic);
		assertThat(result.getType()).isEqualTo(type);
		assertThat(result.getRelationIds()).isEqualTo(relationIds);
		assertThat(result.getParticipants()).isEqualTo(participants);
		assertThat(result.getMetadata()).isEqualTo(metadata);

	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Conversation.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new Conversation()).hasAllNullFieldsOrProperties();
	}
}
