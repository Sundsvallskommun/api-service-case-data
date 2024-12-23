package se.sundsvall.casedata.api.model;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import se.sundsvall.casedata.api.model.MessageResponse.AttachmentResponse;
import se.sundsvall.casedata.api.model.validation.enums.MessageType;
import se.sundsvall.casedata.integration.db.model.enums.Classification;
import se.sundsvall.casedata.integration.db.model.enums.Direction;
import se.sundsvall.casedata.integration.db.model.enums.Header;

import java.util.List;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

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
		final var errandNumber = "errandNumber";
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

		// Act
		final var bean = MessageResponse.builder()
			.withAttachments(attachments)
			.withDirection(direction)
			.withEmail(email)
			.withErrandNumber(errandNumber)
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
			.build();

		// Assert
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getAttachments()).isEqualTo(attachments);
		assertThat(bean.getDirection()).isEqualTo(direction);
		assertThat(bean.getEmail()).isEqualTo(email);
		assertThat(bean.getErrandNumber()).isEqualTo(errandNumber);
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
			.withContentType(contentType)
			.build();

		// Assert
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getAttachmentId()).isEqualTo(attachmentId);
		assertThat(bean.getContentType()).isEqualTo(contentType);
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
