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
import org.junit.jupiter.params.provider.ValueSource;

import se.sundsvall.casedata.api.model.validation.enums.MessageType;
import se.sundsvall.casedata.api.model.validation.impl.ValidMessageTypeConstraintValidator;

class ValidMessageTypeConstraintValidatorTest {

	private ValidMessageTypeConstraintValidator validator;

	private ConstraintValidatorContext context;

	@BeforeEach
	void setUp() {
		validator = new ValidMessageTypeConstraintValidator();
		context = mock(ConstraintValidatorContext.class);

		final var validMessageType = mock(ValidMessageType.class);
		when(validMessageType.nullable()).thenReturn(true);

		validator.initialize(validMessageType);
	}

	@ParameterizedTest
	@EnumSource(MessageType.class)
	void isValid_withValidMessageType(final MessageType type) {
		final var validMessageType = type.name();
		assertThat(validator.isValid(validMessageType, context)).isTrue();
	}

	@Test
	void isValid_withInvalidMessageType() {
		final var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
		when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

		final var invalidMessageType = "INVALID_CATEGORY";
		assertThat(validator.isValid(invalidMessageType, context)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(booleans = {
		true, false
	})
	void isValid_Nullable(final Boolean nullable) {
		final var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
		when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
		validator.setNullable(nullable);
		assertThat(validator.isValid(null, context)).isEqualTo(nullable);
	}

	@Test
	void isValid_withEmptyMessageType() {
		final var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
		when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

		final var emptyType = "";
		assertThat(validator.isValid(emptyType, context)).isFalse();
	}

}
