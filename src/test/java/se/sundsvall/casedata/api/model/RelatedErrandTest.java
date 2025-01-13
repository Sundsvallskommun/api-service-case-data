package se.sundsvall.casedata.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class RelatedErrandTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(RelatedErrand.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {
		// Arrange
		final var errandId = 1L;
		final var errandNumber = "errandNumber";
		final var relationReason = "relationReason";

		// Act
		final var result = RelatedErrand.builder()
			.withErrandId(errandId)
			.withErrandNumber(errandNumber)
			.withRelationReason(relationReason)
			.build();

		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getErrandId()).isEqualTo(errandId);
		assertThat(result.getErrandNumber()).isEqualTo(errandNumber);
		assertThat(result.getRelationReason()).isEqualTo(relationReason);

	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(RelatedErrand.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new RelatedErrand()).hasAllNullFieldsOrProperties();
	}

}
