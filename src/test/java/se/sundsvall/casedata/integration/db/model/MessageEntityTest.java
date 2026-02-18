package se.sundsvall.casedata.integration.db.model;

import java.util.List;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import se.sundsvall.casedata.api.model.validation.enums.MessageType;
import se.sundsvall.casedata.integration.db.model.enums.Classification;
import se.sundsvall.casedata.integration.db.model.enums.Direction;
import se.sundsvall.casedata.integration.db.model.enums.Header;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class MessageEntityTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(MessageEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Arrange
		final var attachments = List.of(MessageAttachmentEntity.builder().build());
		final var direction = Direction.OUTBOUND;
		final var email = "email";
		final var errandId = 123L;
		final var externalCaseId = "externalCaseID";
		final var familyId = "familyID";
		final var firstName = "firstName";
		final var lastName = "lastName";
		final var messageId = "messageID";
		final var messageType = MessageType.EMAIL;
		final var mobileNumber = "mobileNumber";
		final var sent = "sent";
		final var subject = "subject";
		final var textmessage = "textmessage";
		final var htmlmessage = "htmlmessage";
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var userId = "userId";
		final var username = "username";
		final var viewed = true;
		final var classification = Classification.INFORMATION;
		final var recipients = List.of("recipient@sundsvall.se");
		final var headers = List.of(EmailHeaderEntity.builder()
			.withHeader(Header.MESSAGE_ID)
			.withValues(List.of("<messageID@test.com>"))
			.build());
		final var internal = true;

		// Act
		final var bean = MessageEntity.builder()
			.withAttachments(attachments)
			.withDirection(direction)
			.withEmail(email)
			.withErrandId(errandId)
			.withExternalCaseId(externalCaseId)
			.withFamilyId(familyId)
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withFirstName(firstName)
			.withLastName(lastName)
			.withMessageId(messageId)
			.withMessageType(messageType.name())
			.withMobileNumber(mobileNumber)
			.withSent(sent)
			.withSubject(subject)
			.withTextmessage(textmessage)
			.withHtmlMessage(htmlmessage)
			.withUserId(userId)
			.withUsername(username)
			.withViewed(viewed)
			.withClassification(classification)
			.withRecipients(recipients)
			.withHeaders(headers)
			.withInternal(internal)
			.build();

		// Assert
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getAttachments()).isEqualTo(attachments);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(bean.getNamespace()).isEqualTo(namespace);
		assertThat(bean.getDirection()).isEqualTo(direction);
		assertThat(bean.getEmail()).isEqualTo(email);
		assertThat(bean.getErrandId()).isEqualTo(errandId);
		assertThat(bean.getExternalCaseId()).isEqualTo(externalCaseId);
		assertThat(bean.getFamilyId()).isEqualTo(familyId);
		assertThat(bean.getFirstName()).isEqualTo(firstName);
		assertThat(bean.getLastName()).isEqualTo(lastName);
		assertThat(bean.getMessageId()).isEqualTo(messageId);
		assertThat(bean.getMessageType()).isEqualTo(messageType.name());
		assertThat(bean.getMobileNumber()).isEqualTo(mobileNumber);
		assertThat(bean.getSent()).isEqualTo(sent);
		assertThat(bean.getSubject()).isEqualTo(subject);
		assertThat(bean.getTextmessage()).isEqualTo(textmessage);
		assertThat(bean.getHtmlMessage()).isEqualTo(htmlmessage);
		assertThat(bean.getUserId()).isEqualTo(userId);
		assertThat(bean.getUsername()).isEqualTo(username);
		assertThat(bean.isViewed()).isEqualTo(viewed);
		assertThat(bean.getClassification()).isEqualTo(classification);
		assertThat(bean.getHeaders()).isEqualTo(headers);
		assertThat(bean.getRecipients()).isEqualTo(recipients);
		assertThat(bean.getInternal()).isEqualTo(internal);
	}

	@Test
	void noDirtOnEmptyBean() {
		assertThat(MessageEntity.builder().build()).hasAllNullFieldsOrPropertiesExcept("viewed");
		assertThat(new MessageEntity()).hasAllNullFieldsOrPropertiesExcept("viewed");
	}

}
