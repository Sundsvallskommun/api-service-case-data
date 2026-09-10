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

class ReadByCountTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(ReadByCount.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {
		// Arrange
		final var identifier = Identifier.builder().withType("adAccount").withValue("joe01doe").build();
		final var count = 3L;

		// Act
		final var result = ReadByCount.builder()
			.withIdentifier(identifier)
			.withCount(count)
			.build();

		// Assert
		assertThat(result.getIdentifier()).isEqualTo(identifier);
		assertThat(result.getCount()).isEqualTo(count);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(ReadByCount.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new ReadByCount()).hasAllNullFieldsOrProperties();
	}

}
