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

class MessageAttachmentEntityTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(MessageAttachmentEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Arrange
		final var attachmentData = MessageAttachmentDataEntity.builder().build();
		final var attachmentId = "attachmentId";
		final var contentType = "contentType";
		final var messageId = "messageID";
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var name = "name";

		// Act
		final var bean = MessageAttachmentEntity.builder()
			.withAttachmentData(attachmentData)
			.withAttachmentId(attachmentId)
			.withContentType(contentType)
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withMessageID(messageId)
			.withName(name)
			.build();

		// Assert
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getAttachmentData()).isEqualTo(attachmentData);
		assertThat(bean.getAttachmentId()).isEqualTo(attachmentId);
		assertThat(bean.getContentType()).isEqualTo(contentType);
		assertThat(bean.getMessageID()).isEqualTo(messageId);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(bean.getName()).isEqualTo(name);
		assertThat(bean.getNamespace()).isEqualTo(namespace);
	}

	@Test
	void noDirtOnEmptyBean() {
		assertThat(MessageAttachmentEntity.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new MessageAttachmentEntity()).hasAllNullFieldsOrProperties();
	}

}
