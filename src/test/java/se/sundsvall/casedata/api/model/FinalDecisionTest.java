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

class FinalDecisionTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(FinalDecision.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builderTest() {
		// Arrange
		final var errandId = 123L;
		final var errandNumber = "SGP-2022-000001";
		final var decision = Decision.builder().build();

		// Act
		final var bean = FinalDecision.builder()
			.withErrandId(errandId)
			.withErrandNumber(errandNumber)
			.withDecision(decision)
			.build();

		// Assert
		assertThat(bean.getErrandId()).isEqualTo(errandId);
		assertThat(bean.getErrandNumber()).isEqualTo(errandNumber);
		assertThat(bean.getDecision()).isEqualTo(decision);
	}

	@Test
	void testNoDirtOnEmptyBean() {
		assertThat(FinalDecision.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new FinalDecision()).hasAllNullFieldsOrProperties();
	}

}
