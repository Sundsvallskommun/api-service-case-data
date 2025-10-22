package se.sundsvall.casedata.api.model;

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

class ExtraParameterTest {
	@Test
	void testBean() {
		MatcherAssert.assertThat(ExtraParameter.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {
		// Arrange
		final var id = "1aefbbb8-de82-414b-b5d7-ba7c5bbe4506";
		final var key = "key1";
		final var displayName = "displayName1";
		final var values = List.of("value1", "value2");

		// Act
		final var result = ExtraParameter.builder()
			.withId(id)
			.withKey(key)
			.withDisplayName(displayName)
			.withValues(values)
			.build();

		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getKey()).isEqualTo(key);
		assertThat(result.getDisplayName()).isEqualTo(displayName);
		assertThat(result.getValues()).isEqualTo(values);

	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(ExtraParameter.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new ExtraParameter()).hasAllNullFieldsOrProperties();
	}

}
