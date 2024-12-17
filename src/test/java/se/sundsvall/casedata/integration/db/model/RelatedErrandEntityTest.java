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

class RelatedErrandEntityTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(RelatedErrandEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {
		// Arrange
		final var id = "id";
		final var errandId = 1L;
		final var relatedErrandId = 2L;
		final var relatedErrandNumber = "relatedErrandNumber";
		final var relationReason = "relationReason";

		// Act
		final var result = RelatedErrandEntity.builder()
			.withId(id)
			.withErrandId(errandId)
			.withRelatedErrandNumber(relatedErrandNumber)
			.withRelationReason(relationReason)
			.withRelatedErrandId(relatedErrandId)
			.build();

		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getErrandId()).isEqualTo(errandId);
		assertThat(result.getRelatedErrandNumber()).isEqualTo(relatedErrandNumber);
		assertThat(result.getRelatedErrandId()).isEqualTo(relatedErrandId);
		assertThat(result.getRelationReason()).isEqualTo(relationReason);

	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(RelatedErrandEntity.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new RelatedErrandEntity()).hasAllNullFieldsOrProperties();
	}
}
