package se.sundsvall.casedata.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class CaseTypeEntityTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(CaseTypeEntity.class, allOf(
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
		final var namespace = "namespace";
		final var municipalityId = "1234";

		// Act
		final var result = CaseTypeEntity.builder()
			.withType(type)
			.withDisplayName(displayName)
			.withNamespace(namespace)
			.withMunicipalityId(municipalityId)
			.build();
		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getType()).isEqualTo(type);
		assertThat(result.getDisplayName()).isEqualTo(displayName);
		assertThat(result.getNamespace()).isEqualTo(namespace);
		assertThat(result.getMunicipalityId()).isEqualTo(municipalityId);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(CaseTypeEntity.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new CaseTypeEntity()).hasAllNullFieldsOrProperties();
	}
}
