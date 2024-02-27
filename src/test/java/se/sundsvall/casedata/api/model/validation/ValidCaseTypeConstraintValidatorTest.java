package se.sundsvall.casedata.api.model.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintValidatorContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import se.sundsvall.casedata.api.model.validation.enums.CaseType;

class ValidCaseTypeConstraintValidatorTest {

	private ValidCaseTypeConstraintValidator validator;

	private ConstraintValidatorContext context;

	@BeforeEach
	void setUp() {
		validator = new ValidCaseTypeConstraintValidator();
		context = mock(ConstraintValidatorContext.class);
	}

	@ParameterizedTest
	@EnumSource(CaseType.class)
	void isValid_withValidCaseType(final CaseType type) {
		final var validCaseType = type.name();
		assertThat(validator.isValid(validCaseType, context)).isTrue();
	}

	@Test
	void isValid_withInvalidCaseType() {
		final var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
		when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

		final var invalidCaseType = "INVALID_CATEGORY";
		assertThat(validator.isValid(invalidCaseType, context)).isFalse();
	}

	@Test
	void isValid_withNullCaseType() {
		final var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
		when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

		assertThat(validator.isValid(null, context)).isFalse();
	}

	@Test
	void isValid_withEmptyCaseType() {
		final var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
		when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

		final var emptyType = "";
		assertThat(validator.isValid(emptyType, context)).isFalse();
	}

}
