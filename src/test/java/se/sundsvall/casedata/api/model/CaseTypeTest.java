package se.sundsvall.casedata.api.model;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class CaseTypeTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(CaseType.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {
		// Arrange
		final var type = "type";
		final var displayName = "displayName";
		final var startProcess = true;

		// Act
		final var result = CaseType.builder()
			.withType(type)
			.withDisplayName(displayName)
			.withStartProcess(startProcess)
			.build();

		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getType()).isEqualTo(type);
		assertThat(result.getDisplayName()).isEqualTo(displayName);
		assertThat(result.isStartProcess()).isEqualTo(startProcess);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(CaseType.builder().build()).hasAllNullFieldsOrPropertiesExcept("startProcess");
		assertThat(new CaseType()).hasAllNullFieldsOrPropertiesExcept("startProcess");
	}

}
