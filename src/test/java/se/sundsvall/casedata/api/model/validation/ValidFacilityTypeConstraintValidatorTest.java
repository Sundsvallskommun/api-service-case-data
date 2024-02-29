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

import se.sundsvall.casedata.api.model.validation.enums.FacilityType;

class ValidFacilityTypeConstraintValidatorTest {

	private ValidFacilityTypeConstraintValidator validator;

	private ConstraintValidatorContext context;

	@BeforeEach
	void setUp() {
		validator = new ValidFacilityTypeConstraintValidator();
		context = mock(ConstraintValidatorContext.class);
	}

	@ParameterizedTest
	@EnumSource(FacilityType.class)
	void isValid_withValidFacilityType(final FacilityType type) {
		final var validFacilityType = type.name();
		assertThat(validator.isValid(validFacilityType, context)).isTrue();
	}

	@Test
	void isValid_withInvalidFacilityType() {
		final var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
		when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

		final var invalidFacilityType = "INVALID_CATEGORY";
		assertThat(validator.isValid(invalidFacilityType, context)).isFalse();
	}

	@Test
	void isValid_withNullFacilityType() {
		final var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
		when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

		assertThat(validator.isValid(null, context)).isFalse();
	}

	@Test
	void isValid_withEmptyFacilityType() {
		final var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
		when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

		final var emptyType = "";
		assertThat(validator.isValid(emptyType, context)).isFalse();
	}

}
