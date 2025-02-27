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
import se.sundsvall.casedata.api.model.MessageResponse.AttachmentResponse;
import se.sundsvall.casedata.api.model.validation.enums.MessageType;
import se.sundsvall.casedata.integration.db.model.enums.Classification;
import se.sundsvall.casedata.integration.db.model.enums.Direction;
import se.sundsvall.casedata.integration.db.model.enums.Header;

class MessageResponseTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(MessageResponse.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));

		MatcherAssert.assertThat(MessageResponse.AttachmentResponse.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testMessageResponseFields() {
		// Arrange
		final var attachments = List.of(AttachmentResponse.builder().build());
		final var direction = Direction.INBOUND;
		final var email = "email";
		final var errandId = 1L;
		final var externalCaseId = "externalCaseId";
		final var familyId = "familyId";
		final var firstName = "firstName";
		final var lastName = "lastName";
		final var message = "message";
		final var messageId = "messageId";
		final var messageType = MessageType.EMAIL;
		final var mobileNumber = "mobileNumber";
		final var sent = "sent";
		final var subject = "subject";
		final var userId = "userId";
		final var userName = "userName";
		final var viewed = true;
		final var classification = Classification.INFORMATION;
		final var headers = List.of(
			EmailHeader.builder()
				.withHeader(Header.MESSAGE_ID)
				.withValues(List.of("<test@test>"))
				.build());
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var recipients = List.of("recipient");

		// Act
		final var bean = MessageResponse.builder()
			.withAttachments(attachments)
			.withDirection(direction)
			.withEmail(email)
			.withErrandId(errandId)
			.withExternalCaseId(externalCaseId)
			.withFamilyId(familyId)
			.withFirstName(firstName)
			.withLastName(lastName)
			.withMessage(message)
			.withMessageId(messageId)
			.withMessageType(messageType.name())
			.withMobileNumber(mobileNumber)
			.withSent(sent)
			.withSubject(subject)
			.withUserId(userId)
			.withUsername(userName)
			.withViewed(viewed)
			.withClassification(classification)
			.withEmailHeaders(headers)
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withRecipients(recipients)
			.build();

		// Assert
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getAttachments()).isEqualTo(attachments);
		assertThat(bean.getDirection()).isEqualTo(direction);
		assertThat(bean.getEmail()).isEqualTo(email);
		assertThat(bean.getErrandId()).isEqualTo(errandId);
		assertThat(bean.getExternalCaseId()).isEqualTo(externalCaseId);
		assertThat(bean.getFamilyId()).isEqualTo(familyId);
		assertThat(bean.getFirstName()).isEqualTo(firstName);
		assertThat(bean.getLastName()).isEqualTo(lastName);
		assertThat(bean.getMessage()).isEqualTo(message);
		assertThat(bean.getMessageId()).isEqualTo(messageId);
		assertThat(bean.getMessageType()).isEqualTo(messageType.name());
		assertThat(bean.getMobileNumber()).isEqualTo(mobileNumber);
		assertThat(bean.getSent()).isEqualTo(sent);
		assertThat(bean.getSubject()).isEqualTo(subject);
		assertThat(bean.getUserId()).isEqualTo(userId);
		assertThat(bean.getUsername()).isEqualTo(userName);
		assertThat(bean.isViewed()).isEqualTo(viewed);
		assertThat(bean.getClassification()).isEqualTo(classification);
		assertThat(bean.getEmailHeaders()).isEqualTo(headers);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(bean.getNamespace()).isEqualTo(namespace);
		assertThat(bean.getRecipients()).isEqualTo(recipients);
	}

	@Test
	void testAttachmentResponseFields() {
		// Arrange
		final var attachmentId = "attachmentId";
		final var name = "name";
		final var contentType = "contentType";

		// Act
		final var bean = AttachmentResponse.builder()
			.withAttachmentId(attachmentId)
			.withName(name)
			.withMimeType(contentType)
			.build();

		// Assert
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getAttachmentId()).isEqualTo(attachmentId);
		assertThat(bean.getMimeType()).isEqualTo(contentType);
		assertThat(bean.getName()).isEqualTo(name);
	}

	@Test
	void testNoDirtOnEmptyBean() {
		assertThat(MessageResponse.builder().build()).hasAllNullFieldsOrPropertiesExcept("viewed").extracting(MessageResponse::isViewed).isEqualTo(false);
		assertThat(new MessageResponse()).hasAllNullFieldsOrPropertiesExcept("viewed").extracting(MessageResponse::isViewed).isEqualTo(false);

		assertThat(AttachmentResponse.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new AttachmentResponse()).hasAllNullFieldsOrProperties();
	}

}
