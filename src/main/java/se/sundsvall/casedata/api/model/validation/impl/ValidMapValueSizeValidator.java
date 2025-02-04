package se.sundsvall.casedata.api.model.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Map;
import lombok.Setter;
import se.sundsvall.casedata.api.model.validation.ValidMapValueSize;

@Setter
public class ValidMapValueSizeValidator implements ConstraintValidator<ValidMapValueSize, Map<String, String>> {

	private int max;

	private boolean nullable;

	@Override
	public void initialize(final ValidMapValueSize constraintAnnotation) {
		max = constraintAnnotation.max();
		nullable = constraintAnnotation.nullable();
		ConstraintValidator.super.initialize(constraintAnnotation);
	}

	@Override
	public boolean isValid(final Map<String, String> value, final ConstraintValidatorContext context) {
		if (value == null) {
			return nullable;
		}
		return value.entrySet().stream()
			.allMatch(entry -> entry.getValue().length() <= max);
	}

}
