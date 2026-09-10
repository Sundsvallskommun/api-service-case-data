package se.sundsvall.casedata.api.model.conversation;

import java.util.List;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class ConversationReadByCountTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(ConversationReadByCount.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {
		// Arrange
		final var conversationId = "896a44d8-724b-11ed-a840-0242ac110002";
		final var messageCount = 5L;
		final var readByCount = List.of(ReadByCount.builder().withCount(3L).build());
		final var readByPartCount = List.of(ReadByPartCount.builder().withPart("ERRAND-NUMBER-1").withCount(5L).build());

		// Act
		final var result = ConversationReadByCount.builder()
			.withConversationId(conversationId)
			.withMessageCount(messageCount)
			.withReadByCount(readByCount)
			.withReadByPartCount(readByPartCount)
			.build();

		// Assert
		assertThat(result.getConversationId()).isEqualTo(conversationId);
		assertThat(result.getMessageCount()).isEqualTo(messageCount);
		assertThat(result.getReadByCount()).isEqualTo(readByCount);
		assertThat(result.getReadByPartCount()).isEqualTo(readByPartCount);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(ConversationReadByCount.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new ConversationReadByCount()).hasAllNullFieldsOrProperties();
	}

}
