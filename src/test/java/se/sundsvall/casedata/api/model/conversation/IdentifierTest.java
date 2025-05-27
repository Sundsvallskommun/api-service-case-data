package se.sundsvall.casedata.api.model.conversation;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class IdentifierTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(Identifier.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {
		// Arrange
		final var type = "adAccount";
		final var value = "1234567890";

		// Act
		final var result = Identifier.builder()
			.withType(type)
			.withValue(value)

			.build();
		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getType()).isEqualTo(type);
		assertThat(result.getValue()).isEqualTo(value);

	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Identifier.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new Identifier()).hasAllNullFieldsOrProperties();
	}
}
