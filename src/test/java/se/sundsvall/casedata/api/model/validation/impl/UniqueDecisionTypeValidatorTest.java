package se.sundsvall.casedata.api.model.validation.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casedata.api.model.Decision;
import se.sundsvall.casedata.integration.db.model.enums.DecisionType;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UniqueDecisionTypeValidatorTest {

	private UniqueDecisionTypeValidator validator;

	@BeforeEach
	void setUp() {
		validator = new UniqueDecisionTypeValidator();
	}

	@Test
	void testValidDecisions() {
		final List<Decision> decisions = Arrays.asList(
			Decision.builder()
				.withDecisionType(DecisionType.FINAL)
				.build(),
			Decision.builder()
				.withDecisionType(DecisionType.RECOMMENDED)
				.build(),
			Decision.builder()
				.withDecisionType(DecisionType.PROPOSED)
				.build());
		assertThat(validator.isValid(decisions, null)).isTrue();
	}

	@Test
	void testInvalidDecisions() {
		final List<Decision> decisions = Arrays.asList(
			Decision.builder()
				.withDecisionType(DecisionType.FINAL)
				.build(),
			Decision.builder()
				.withDecisionType(DecisionType.RECOMMENDED)
				.build(),
			Decision.builder()
				.withDecisionType(DecisionType.FINAL)
				.build());
		assertThat(validator.isValid(decisions, null)).isFalse();
	}

	@Test
	void testEmptyDecisions() {
		final List<Decision> decisions = Collections.emptyList();
		assertThat(validator.isValid(decisions, null)).isTrue();
	}

	@Test
	void testNullDecisions() {
		assertThat(validator.isValid(null, null)).isTrue();
	}

}
