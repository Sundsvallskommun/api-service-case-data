package se.sundsvall.casedata.service.util;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import se.sundsvall.casedata.integration.db.model.ConversationEntity;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class ConversationEventTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(ConversationEvent.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {
		// Arrange
		final var conversationEntity = ConversationEntity.builder().build();
		final var requestId = "request-id";
		// Act
		final var result = ConversationEvent.builder()
			.withConversationEntity(conversationEntity)
			.withRequestId(requestId)
			.build();
		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getConversationEntity()).isEqualTo(conversationEntity);
		assertThat(result.getRequestId()).isEqualTo(requestId);

	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(ConversationEvent.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new ConversationEvent()).hasAllNullFieldsOrProperties();
	}

}
