package se.sundsvall.casedata.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class MessageAttachmentTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(MessageAttachment.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testFields() {
		final var attachmentData = MessageAttachmentData.builder().build();
		final var attachmentID = "attachmentID";
		final var contentType = "contentType";
		final var messageID = "messageID";
		final var name = "name";

		final var bean = MessageAttachment.builder()
			.withAttachmentData(attachmentData)
			.withAttachmentID(attachmentID)
			.withContentType(contentType)
			.withMessageID(messageID)
			.withName(name)
			.build();

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getAttachmentData()).isEqualTo(attachmentData);
		assertThat(bean.getAttachmentID()).isEqualTo(attachmentID);
		assertThat(bean.getContentType()).isEqualTo(contentType);
		assertThat(bean.getMessageID()).isEqualTo(messageID);
		assertThat(bean.getName()).isEqualTo(name);
	}

	@Test
	void noDirtOnEmptyBean() {
		assertThat(MessageAttachment.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new MessageAttachment()).hasAllNullFieldsOrProperties();
	}
}
