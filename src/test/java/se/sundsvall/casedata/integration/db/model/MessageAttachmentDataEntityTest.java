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

class MessageAttachmentDataEntityTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(MessageAttachmentDataEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() throws Exception {
		// Arrange
		final var content = "content";
		final var file = new MariaDbBlob(content.getBytes());
		final var id = new Random().nextInt();

		// Act
		final var bean = MessageAttachmentDataEntity.builder()
			.withFile(file)
			.withId(id)
			.build();

		// Assert
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getFile()).isEqualTo(file);
		assertThat(bean.getFile().getBinaryStream().readAllBytes()).isEqualTo(content.getBytes());
		assertThat(bean.getId()).isEqualTo(id);
	}

	@Test
	void noDirtOnEmptyBean() {
		assertThat(MessageAttachmentDataEntity.builder().build()).hasAllNullFieldsOrPropertiesExcept("id")
			.satisfies(bean -> assertThat(bean.getId()).isZero());
		assertThat(new MessageAttachmentDataEntity()).hasAllNullFieldsOrPropertiesExcept("id")
			.satisfies(bean -> assertThat(bean.getId()).isZero());
	}

}
