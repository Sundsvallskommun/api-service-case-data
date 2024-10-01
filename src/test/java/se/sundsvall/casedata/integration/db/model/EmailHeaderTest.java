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

import se.sundsvall.casedata.integration.db.model.enums.Header;

class EmailHeaderTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(EmailHeader.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var id = 1L;
		final var header = Header.MESSAGE_ID;
		final var values = List.of("value1", "value2", "value3");

		final var bean = EmailHeader.builder()
			.withId(id)
			.withHeader(header)
			.withValues(values)
			.build();

		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getHeader()).isEqualTo(header);
		assertThat(bean.getValues()).isEqualTo(values);
	}

	@Test
	void noDirtOnEmptyBean() {
		assertThat(EmailHeader.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new EmailHeader()).hasAllNullFieldsOrProperties();
	}

}
