package se.sundsvall.casedata.api.model.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.sundsvall.casedata.api.model.Decision;
import se.sundsvall.casedata.api.model.validation.impl.UniqueDecisionTypeValidator;
import se.sundsvall.casedata.integration.db.model.enums.DecisionType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class UniqueDecisionTypeTest {

	private UniqueDecisionTypeValidator validator;
	private ConstraintValidatorContext contextMock;

	@BeforeEach
	void setUp() {
		validator = new UniqueDecisionTypeValidator();
		contextMock = mock(ConstraintValidatorContext.class);
	}

	@Test
	void testValidDecisions() {
		final var decisions = Arrays.asList(
			Decision.builder()
				.withDecisionType(DecisionType.FINAL)
				.build(),
			Decision.builder()
				.withDecisionType(DecisionType.RECOMMENDED)
				.build(),
			Decision.builder()
				.withDecisionType(DecisionType.PROPOSED)
				.build()
		);
		assertThat(validator.isValid(decisions, contextMock)).isTrue();
	}

	@Test
	void testInvalidDecisions() {
		final var decisions = Arrays.asList(
			Decision.builder()
				.withDecisionType(DecisionType.FINAL)
				.build(),
			Decision.builder()
				.withDecisionType(DecisionType.RECOMMENDED)
				.build(),
			Decision.builder()
				.withDecisionType(DecisionType.FINAL)
				.build()
		);
		assertThat(validator.isValid(decisions, contextMock)).isFalse();
	}

	@Test
	void testEmptyDecisions() {
		final List<Decision> decisions = Collections.emptyList();
		assertThat(validator.isValid(decisions, contextMock)).isTrue();
	}

	@Test
	void testNullDecisions() {
		assertThat(validator.isValid(null, contextMock)).isTrue();
	}
}
