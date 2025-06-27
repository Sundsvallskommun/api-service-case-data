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
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ConversationAttachmentTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(ConversationAttachment.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {
		// Arrange

		final var id = "cb20c51f-fcf3-42c0-b613-de563634a8ec";
		final var fileName = "test-file.txt";
		final var fileSize = 1024;
		final var mimeType = "text/plain";
		final var created = "2023-01-01T00:00:00+01:00";

		// Act
		final var result = ConversationAttachment.builder()
			.withId(id)
			.withFileName(fileName)
			.withFileSize(fileSize)
			.withMimeType(mimeType)
			.withCreated(OffsetDateTime.parse(created))
			.build();
		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getFileName()).isEqualTo(fileName);
		assertThat(result.getFileSize()).isEqualTo(fileSize);
		assertThat(result.getMimeType()).isEqualTo(mimeType);
		assertThat(result.getCreated()).isEqualTo(OffsetDateTime.parse(created));

	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(ConversationAttachment.builder().build()).hasAllNullFieldsOrPropertiesExcept("fileSize");
		assertThat(new ConversationAttachment()).hasAllNullFieldsOrPropertiesExcept("fileSize");
	}

}
