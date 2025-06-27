package se.sundsvall.casedata.api.model.conversation;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MessageTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(Message.class, allOf(
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
		final var inReplyToMessageId = "1aefbbb8-de82-414b-b5d7-ba7c5bbe4506";
		final var created = now();
		final var createdBy = Identifier.builder().build();
		final var content = "Hello, how can I help you?";
		final var readBy = List.of(ReadBy.builder().build());
		final var attachments = List.of(ConversationAttachment.builder().build());
		// Act
		final var result = Message.builder()
			.withId(id)
			.withInReplyToMessageId(inReplyToMessageId)
			.withCreated(created)
			.withCreatedBy(createdBy)
			.withContent(content)
			.withReadBy(readBy)
			.withAttachments(attachments)

			.build();
		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getInReplyToMessageId()).isEqualTo(inReplyToMessageId);
		assertThat(result.getCreated()).isEqualTo(created);
		assertThat(result.getCreatedBy()).isEqualTo(createdBy);
		assertThat(result.getContent()).isEqualTo(content);
		assertThat(result.getReadBy()).isEqualTo(readBy);
		assertThat(result.getAttachments()).isEqualTo(attachments);

	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Message.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new Message()).hasAllNullFieldsOrProperties();
	}

}
