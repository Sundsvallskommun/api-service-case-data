package se.sundsvall.casedata.api.model.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import se.sundsvall.casedata.api.model.validation.impl.ValidTimelinessReviewValueConstraintValidator;
import se.sundsvall.casedata.integration.db.model.enums.TimelinessReview;

class ValidTimelinessReviewValueConstraintValidatorTest {

	private ValidTimelinessReviewValueConstraintValidator validator;

	private ConstraintValidatorContext contextMock;

	@BeforeEach
	void setUp() {
		validator = new ValidTimelinessReviewValueConstraintValidator();
		contextMock = mock(ConstraintValidatorContext.class);
	}

	@ParameterizedTest
	@EnumSource(TimelinessReview.class)
	void isValidWithValidValue(final TimelinessReview status) {
		final var validValue = status.toString();
		assertThat(validator.isValid(validValue, contextMock)).isTrue();
	}

	@Test
	void isNotValidWithInvalidValue() {
		final var builderMock = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
		when(contextMock.buildConstraintViolationWithTemplate(anyString())).thenReturn(builderMock);

		assertThat(validator.isValid("INVALID_VALUE", contextMock)).isFalse();

		verify(contextMock).disableDefaultConstraintViolation();
		// If verification fails remember to step API version
		verify(contextMock).buildConstraintViolationWithTemplate("Invalid timeliness review value. Valid values are: [NOT_CONDUCTED, NOT_RELEVANT, APPROVED, REJECTED]");
		verify(builderMock).addConstraintViolation();
	}

	@ParameterizedTest
	@NullAndEmptySource
	void isNotValidWithNullOrEmptyStatus(String value) {
		final var builderMock = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
		when(contextMock.buildConstraintViolationWithTemplate(anyString())).thenReturn(builderMock);

		assertThat(validator.isValid(value, contextMock)).isFalse();

		verify(contextMock).disableDefaultConstraintViolation();
		verify(contextMock).buildConstraintViolationWithTemplate("Timeliness review value cannot be null or empty. Valid values are: " + Arrays.toString(TimelinessReview.values()));
		verify(builderMock).addConstraintViolation();
	}

}
