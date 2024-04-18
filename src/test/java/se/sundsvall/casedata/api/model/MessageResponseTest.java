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
import se.sundsvall.casedata.integration.db.model.enums.Classification;
import se.sundsvall.casedata.integration.db.model.enums.Direction;
import se.sundsvall.casedata.integration.db.model.enums.Header;
import se.sundsvall.casedata.integration.db.model.enums.MessageType;

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
		final var attachments = List.of(AttachmentResponse.builder().build());
		final var direction = Direction.INBOUND;
		final var email = "email";
		final var errandNumber = "errandNumber";
		final var externalCaseID = "externalCaseID";
		final var familyID = "familyID";
		final var firstName = "firstName";
		final var lastName = "lastName";
		final var message = "message";
		final var messageID = "messageID";
		final var messageType = MessageType.EMAIL;
		final var mobileNumber = "mobileNumber";
		final var sent = "sent";
		final var subject = "subject";
		final var userID = "userID";
		final var userName = "userName";
		final var viewed = true;
		final var classification = Classification.INFORMATION;
		final var headers = List.of(
			EmailHeaderDTO.builder()
				.withHeader(Header.MESSAGE_ID)
				.withValues(List.of("<test@test>"))
				.build());

		final var bean = MessageResponse.builder()
			.withAttachments(attachments)
			.withDirection(direction)
			.withEmail(email)
			.withErrandNumber(errandNumber)
			.withExternalCaseID(externalCaseID)
			.withFamilyID(familyID)
			.withFirstName(firstName)
			.withLastName(lastName)
			.withMessage(message)
			.withMessageID(messageID)
			.withMessageType(messageType.name())
			.withMobileNumber(mobileNumber)
			.withSent(sent)
			.withSubject(subject)
			.withUserID(userID)
			.withUsername(userName)
			.withViewed(viewed)
			.withClassification(classification)
			.withEmailHeaders(headers)
			.build();

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getAttachments()).isEqualTo(attachments);
		assertThat(bean.getDirection()).isEqualTo(direction);
		assertThat(bean.getEmail()).isEqualTo(email);
		assertThat(bean.getErrandNumber()).isEqualTo(errandNumber);
		assertThat(bean.getExternalCaseID()).isEqualTo(externalCaseID);
		assertThat(bean.getFamilyID()).isEqualTo(familyID);
		assertThat(bean.getFirstName()).isEqualTo(firstName);
		assertThat(bean.getLastName()).isEqualTo(lastName);
		assertThat(bean.getMessage()).isEqualTo(message);
		assertThat(bean.getMessageID()).isEqualTo(messageID);
		assertThat(bean.getMessageType()).isEqualTo(messageType.name());
		assertThat(bean.getMobileNumber()).isEqualTo(mobileNumber);
		assertThat(bean.getSent()).isEqualTo(sent);
		assertThat(bean.getSubject()).isEqualTo(subject);
		assertThat(bean.getUserID()).isEqualTo(userID);
		assertThat(bean.getUsername()).isEqualTo(userName);
		assertThat(bean.isViewed()).isEqualTo(viewed);
		assertThat(bean.getClassification()).isEqualTo(classification);
		assertThat(bean.getEmailHeaders()).isEqualTo(headers);
	}

	@Test
	void testAttachmentResponseFields() {
		final var attachmentID = "attachmentID";
		final var name = "name";
		final var contentType = "contentType";

		final var bean = AttachmentResponse.builder()
			.withAttachmentID(attachmentID)
			.withName(name)
			.withContentType(contentType)
			.build();

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getAttachmentID()).isEqualTo(attachmentID);
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
