package se.sundsvall.casedata.api.model.conversation;

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

class KeyValuesTest {
	@Test
	void testBean() {
		MatcherAssert.assertThat(KeyValues.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {
		// Arrange
		final var key = "key1";
		final var values = List.of("value1", "value2");

		// Act
		final var result = KeyValues.builder()
			.withKey(key)
			.withValues(values)

			.build();
		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getKey()).isEqualTo(key);
		assertThat(result.getValues()).isEqualTo(values);

	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(KeyValues.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new KeyValues()).hasAllNullFieldsOrProperties();
	}
}
