package se.sundsvall.casedata.api.model;

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
import se.sundsvall.casedata.integration.db.model.enums.Classification;
import se.sundsvall.casedata.integration.db.model.enums.Direction;

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
		final var messageId = "12";
		final var direction = Direction.INBOUND;
		final var familyId = "12";
		final var externalCaseId = "12";
		final var message = "Hello world";
		final var htmlMessage = "Hello world";
		final var sent = "2020-01-01 12:00:00";
		final var subject = "Hello world";
		final var username = "username";
		final var firstName = "Kalle";
		final var lastName = "Anka";
		final var messageType = "EMAIL";
		final var mobileNumber = "+46701740605";
		final var email = "email@sundsvall.se";
		final var recipients = List.of("email@sundsvall.se");
		final var userId = "12";
		final var classification = Classification.INFORMATION;
		final var attachments = List.of(MessageRequest.AttachmentRequest.builder().build());
		final var emailHeaders = List.of(EmailHeader.builder().build());
		final var internal = true;
		// Act
		final var result = MessageRequest.builder()
			.withMessageId(messageId)
			.withDirection(direction)
			.withFamilyId(familyId)
			.withExternalCaseId(externalCaseId)
			.withMessage(message)
			.withHtmlMessage(htmlMessage)
			.withSent(sent)
			.withSubject(subject)
			.withUsername(username)
			.withFirstName(firstName)
			.withLastName(lastName)
			.withMessageType(messageType)
			.withMobileNumber(mobileNumber)
			.withEmail(email)
			.withRecipients(recipients)
			.withUserId(userId)
			.withClassification(classification)
			.withAttachments(attachments)
			.withEmailHeaders(emailHeaders)
			.withInternal(internal)
			.build();
		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getMessageId()).isEqualTo(messageId);
		assertThat(result.getDirection()).isEqualTo(direction);
		assertThat(result.getFamilyId()).isEqualTo(familyId);
		assertThat(result.getExternalCaseId()).isEqualTo(externalCaseId);
		assertThat(result.getMessage()).isEqualTo(message);
		assertThat(result.getHtmlMessage()).isEqualTo(htmlMessage);
		assertThat(result.getSent()).isEqualTo(sent);
		assertThat(result.getSubject()).isEqualTo(subject);
		assertThat(result.getUsername()).isEqualTo(username);
		assertThat(result.getFirstName()).isEqualTo(firstName);
		assertThat(result.getLastName()).isEqualTo(lastName);
		assertThat(result.getMessageType()).isEqualTo(messageType);
		assertThat(result.getMobileNumber()).isEqualTo(mobileNumber);
		assertThat(result.getEmail()).isEqualTo(email);
		assertThat(result.getRecipients()).isEqualTo(recipients);
		assertThat(result.getUserId()).isEqualTo(userId);
		assertThat(result.getClassification()).isEqualTo(classification);
		assertThat(result.getAttachments()).isEqualTo(attachments);
		assertThat(result.getEmailHeaders()).isEqualTo(emailHeaders);
		assertThat(result.getInternal()).isEqualTo(internal);

	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(MessageRequest.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new MessageRequest()).hasAllNullFieldsOrProperties();
	}
}
