package se.sundsvall.casedata.api.model.validation;

import jakarta.validation.ConstraintValidatorContext;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.sundsvall.casedata.api.model.validation.impl.ValidMapValueSizeValidator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ValidMapValueSizeValidatorTest {

	private ValidMapValueSizeValidator validator;

	private ConstraintValidatorContext context;

	@BeforeEach
	void setUp() {
		validator = new ValidMapValueSizeValidator();
		context = mock(ConstraintValidatorContext.class);

		final var validMapValueSize = mock(ValidMapValueSize.class);
		when(validMapValueSize.nullable()).thenReturn(true);
		when(validMapValueSize.max()).thenReturn(10);

		validator.initialize(validMapValueSize);
	}

	@Test
	void isValidWithValidMap() {
		final var validMap = new HashMap<String, String>();
		validMap.put("key1", "value1");
		validMap.put("key2", "value2");

		assertThat(validator.isValid(validMap, context)).isTrue();
	}

	@Test
	void isValidWithInvalidMap() {
		final var invalidMap = new HashMap<String, String>();
		invalidMap.put("key1", "value1");
		invalidMap.put("key2", "value with more than 10 characters");

		assertThat(validator.isValid(invalidMap, context)).isFalse();
	}

	@Test
	void isValidWithNullMap() {
		validator.setNullable(false);
		assertThat(validator.isValid(null, context)).isFalse();
	}
}
