package se.sundsvall.casedata.api.model.conversation;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class ReadByPartCountTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(ReadByPartCount.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {
		// Arrange
		final var part = "ERRAND-NUMBER-1";
		final var count = 5L;

		// Act
		final var result = ReadByPartCount.builder()
			.withPart(part)
			.withCount(count)
			.build();

		// Assert
		assertThat(result.getPart()).isEqualTo(part);
		assertThat(result.getCount()).isEqualTo(count);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(ReadByPartCount.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new ReadByPartCount()).hasAllNullFieldsOrProperties();
	}

}
