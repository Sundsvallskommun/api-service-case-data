package se.sundsvall.casedata.api.model;

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

class BulkEmailRequestTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(BulkEmailRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {
		// Arrange
		final var recipients = List.of("first@example.com", "second@example.com");
		final var subject = "Subject";
		final var message = "Message in plain text";
		final var htmlMessage = "<p>Message in html</p>";
		final var departmentName = "CONVERSATION";
		final var attachments = List.of(MessageRequest.AttachmentRequest.builder().build());

		// Act
		final var result = BulkEmailRequest.builder()
			.withRecipients(recipients)
			.withSubject(subject)
			.withMessage(message)
			.withHtmlMessage(htmlMessage)
			.withDepartmentName(departmentName)
			.withAttachments(attachments)
			.build();

		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getRecipients()).isEqualTo(recipients);
		assertThat(result.getSubject()).isEqualTo(subject);
		assertThat(result.getMessage()).isEqualTo(message);
		assertThat(result.getHtmlMessage()).isEqualTo(htmlMessage);
		assertThat(result.getDepartmentName()).isEqualTo(departmentName);
		assertThat(result.getAttachments()).isEqualTo(attachments);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(BulkEmailRequest.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new BulkEmailRequest()).hasAllNullFieldsOrProperties();
	}
}
