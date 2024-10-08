package se.sundsvall.casedata.api.model.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import jakarta.validation.ConstraintValidatorContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import se.sundsvall.casedata.integration.db.model.enums.AppealStatus;

class ValidAppealStatusConstraintValidatorTest {

	private ValidAppealStatusConstraintValidator validator;

	private ConstraintValidatorContext contextMock;

	@BeforeEach
	void setUp() {
		validator = new ValidAppealStatusConstraintValidator();
		contextMock = mock(ConstraintValidatorContext.class);
	}

	@ParameterizedTest
	@EnumSource(AppealStatus.class)
	void isValid_withValidStatus(final AppealStatus status) {
		final var validStatus = status.toString();
		assertThat(validator.isValid(validStatus, contextMock)).isTrue();
	}

	@Test
	void isNotValid_withInvalidStatus() {
		final var builderMock = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
		when(contextMock.buildConstraintViolationWithTemplate(anyString())).thenReturn(builderMock);

		assertThat(validator.isValid("INVALID_STATUS", contextMock)).isFalse();

		verify(contextMock).disableDefaultConstraintViolation();
		// If verification fails remember to step API version
		verify(contextMock).buildConstraintViolationWithTemplate("Invalid appeal status. Valid values are: [NEW, REJECTED, SENT_TO_COURT, COMPLETED]");
		verify(builderMock).addConstraintViolation();
	}

	@ParameterizedTest
	@NullAndEmptySource
	void isNotValid_withNullOrEmptyStatus(String value) {
		final var builderMock = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
		when(contextMock.buildConstraintViolationWithTemplate(anyString())).thenReturn(builderMock);

		assertThat(validator.isValid(value, contextMock)).isFalse();

		verify(contextMock).disableDefaultConstraintViolation();
		verify(contextMock).buildConstraintViolationWithTemplate("Appeal status cannot be null or empty. Valid values are: " + Arrays.toString(AppealStatus.values()));
		verify(builderMock).addConstraintViolation();
	}

}
