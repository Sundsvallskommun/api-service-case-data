package se.sundsvall.casedata.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Random;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import se.sundsvall.casedata.integration.db.model.enums.DecisionOutcome;
import se.sundsvall.casedata.integration.db.model.enums.DecisionType;

class DecisionTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(Decision.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builderTest() {
		// Arrange
		final var decisionType = DecisionType.FINAL;
		final var decisionOutcome = DecisionOutcome.APPROVAL;
		final var description = "description";
		final var decidedBy = Stakeholder.builder().build();
		final var decidedAt = OffsetDateTime.now();
		final var validFrom = OffsetDateTime.now();
		final var validTo = OffsetDateTime.now();
		final var extraParameters = new HashMap<String, String>();

		// Act
		final var bean = Decision.builder()
			.withDecisionType(decisionType)
			.withDecisionOutcome(decisionOutcome)
			.withDescription(description)
			.withDecidedBy(decidedBy)
			.withDecidedAt(decidedAt)
			.withValidFrom(validFrom)
			.withValidTo(validTo)
			.withExtraParameters(extraParameters)
			.build();

		// Assert
		assertThat(bean.getDecisionType()).isEqualTo(decisionType);
		assertThat(bean.getDecisionOutcome()).isEqualTo(decisionOutcome);
		assertThat(bean.getDescription()).isEqualTo(description);
		assertThat(bean.getDecidedBy()).isEqualTo(decidedBy);
		assertThat(bean.getDecidedAt()).isEqualTo(decidedAt);
		assertThat(bean.getValidFrom()).isEqualTo(validFrom);
		assertThat(bean.getValidTo()).isEqualTo(validTo);
		assertThat(bean.getExtraParameters()).isEqualTo(extraParameters);
	}

	@Test
	void testNoDirtOnEmptyBean() {
		assertThat(Decision.builder().build()).hasAllNullFieldsOrPropertiesExcept("extraParameters", "version")
			.satisfies(bean -> {
				assertThat(bean.getExtraParameters()).isEmpty();
				assertThat(bean.getVersion()).isZero();
			});
		assertThat(new Decision()).hasAllNullFieldsOrPropertiesExcept("extraParameters", "version")
			.satisfies(bean -> {
				assertThat(bean.getExtraParameters()).isEmpty();
				assertThat(bean.getVersion()).isZero();
			});
	}

}
