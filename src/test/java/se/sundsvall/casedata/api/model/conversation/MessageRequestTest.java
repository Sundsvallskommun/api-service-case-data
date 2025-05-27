package se.sundsvall.casedata.api.model.conversation;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class MessageRequestTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(MessageRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {
		// Arrange

		final var inReplyToMessageId = "cb20c51f-fcf3-42c0-b613-de563634a8ec";

		final var content = "Hello, how can I help you?";

		// Act
		final var result = MessageRequest.builder()
			.withInReplyToMessageId(inReplyToMessageId)
			.withContent(content)
			.build();

		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getInReplyToMessageId()).isEqualTo(inReplyToMessageId);
		assertThat(result.getContent()).isEqualTo(content);

	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(MessageRequest.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new MessageRequest()).hasAllNullFieldsOrProperties();
	}

}
