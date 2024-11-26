package se.sundsvall.casedata.api.model.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import jakarta.validation.ConstraintValidatorContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.sundsvall.casedata.api.model.validation.impl.ValidMapValueSizeValidator;

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
	void isValid_withValidMap() {
		final var validMap = new HashMap<String, String>();
		validMap.put("key1", "value1");
		validMap.put("key2", "value2");

		assertThat(validator.isValid(validMap, context)).isTrue();
	}

	@Test
	void isValid_withInvalidMap() {
		final var invalidMap = new HashMap<String, String>();
		invalidMap.put("key1", "value1");
		invalidMap.put("key2", "value with more than 10 characters");

		assertThat(validator.isValid(invalidMap, context)).isFalse();
	}

	@Test
	void isValid_withNullMap() {
		validator.setNullable(false);
		assertThat(validator.isValid(null, context)).isFalse();
	}

}
