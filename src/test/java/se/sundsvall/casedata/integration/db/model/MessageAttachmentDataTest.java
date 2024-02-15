package se.sundsvall.casedata.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.util.Random;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.mariadb.jdbc.MariaDbBlob;

class MessageAttachmentDataTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(MessageAttachmentData.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testFields() throws Exception {
		final var content = "content";
		final var file = new MariaDbBlob(content.getBytes());
		final var id = new Random().nextInt();

		final var bean = MessageAttachmentData.builder()
			.withFile(file)
			.withId(id)
			.build();

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getFile()).isEqualTo(file);
		assertThat(bean.getFile().getBinaryStream().readAllBytes()).isEqualTo(content.getBytes());
		assertThat(bean.getId()).isEqualTo(id);
	}

	@Test
	void noDirtOnEmptyBean() {
		assertThat(MessageAttachment.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new MessageAttachment()).hasAllNullFieldsOrProperties();
	}
}
