package se.sundsvall.casedata.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class MessageAttachmentDTOTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(MessageAttachmentDTO.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builderTest() {
		final var attachmentID = "12345678-1234-1234-1234-123456789012";
		final var municipalityId = "municipalityId";
		final var name = "name";
		final var contentType = "text/plain";
		final var content = "aGVsbG8gd29ybGQK";

		final var bean = MessageAttachmentDTO.builder()
			.withAttachmentID(attachmentID)
			.withMunicipalityId(municipalityId)
			.withName(name)
			.withContentType(contentType)
			.withContent(content)
			.build();

		assertThat(bean.getAttachmentID()).isEqualTo(attachmentID);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(bean.getName()).isEqualTo(name);
		assertThat(bean.getContentType()).isEqualTo(contentType);
		assertThat(bean.getContent()).isEqualTo(content);
	}

	@Test
	void testNoDirtOnEmptyBean() {
		assertThat(MessageAttachmentDTO.builder().build()).hasAllNullFieldsOrProperties();
	}
}
