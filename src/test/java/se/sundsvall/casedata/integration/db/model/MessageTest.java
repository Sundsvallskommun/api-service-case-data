package se.sundsvall.casedata.integration.db.model;

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
import se.sundsvall.casedata.integration.db.model.enums.Header;
import se.sundsvall.casedata.integration.db.model.enums.MessageType;

class MessageTest {

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
	void testBuilderMethods() {
		final var attachments = List.of(MessageAttachment.builder().build());
		final var direction = Direction.OUTBOUND;
		final var email = "email";
		final var errandNumber = "errandNumber";
		final var externalCaseID = "externalCaseID";
		final var familyID = "familyID";
		final var firstName = "firstName";
		final var lastName = "lastName";
		final var messageID = "messageID";
		final var messageType = MessageType.EMAIL;
		final var mobileNumber = "mobileNumber";
		final var sent = "sent";
		final var subject = "subject";
		final var textmessage = "textmessage";
		final var userID = "userID";
		final var username = "username";
		final var viewed = true;
		final var classification = Classification.INFORMATION;
		final var headers = List.of(EmailHeader.builder()
			.withHeader(Header.MESSAGE_ID)
			.withValues(List.of("<messageID@test.com>"))
			.build());

		final var bean = Message.builder()
			.withAttachments(attachments)
			.withDirection(direction)
			.withEmail(email)
			.withErrandNumber(errandNumber)
			.withExternalCaseID(externalCaseID)
			.withFamilyID(familyID)
			.withFirstName(firstName)
			.withLastName(lastName)
			.withMessageID(messageID)
			.withMessageType(messageType)
			.withMobileNumber(mobileNumber)
			.withSent(sent)
			.withSubject(subject)
			.withTextmessage(textmessage)
			.withUserID(userID)
			.withUsername(username)
			.withViewed(viewed)
			.withClassification(classification)
			.withHeaders(headers)
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
		assertThat(bean.getMessageID()).isEqualTo(messageID);
		assertThat(bean.getMessageType()).isEqualTo(messageType);
		assertThat(bean.getMobileNumber()).isEqualTo(mobileNumber);
		assertThat(bean.getSent()).isEqualTo(sent);
		assertThat(bean.getSubject()).isEqualTo(subject);
		assertThat(bean.getTextmessage()).isEqualTo(textmessage);
		assertThat(bean.getUserID()).isEqualTo(userID);
		assertThat(bean.getUsername()).isEqualTo(username);
		assertThat(bean.isViewed()).isEqualTo(viewed);
		assertThat(bean.getClassification()).isEqualTo(classification);
		assertThat(bean.getHeaders()).isEqualTo(headers);
	}

	@Test
	void noDirtOnEmptyBean() {
		assertThat(Message.builder().build()).hasAllNullFieldsOrPropertiesExcept("viewed");
		assertThat(new Message()).hasAllNullFieldsOrPropertiesExcept("viewed");
	}
}
