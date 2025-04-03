package se.sundsvall.casedata.api.model.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import lombok.Setter;
import se.sundsvall.casedata.api.model.validation.ValidCaseType;
import se.sundsvall.casedata.api.model.validation.enums.CaseType;

@Setter
public class ValidCaseTypeConstraintValidator implements ConstraintValidator<ValidCaseType, String> {

	private boolean nullable;

	@Override
	public void initialize(final ValidCaseType constraintAnnotation) {
		this.nullable = constraintAnnotation.nullable();
		ConstraintValidator.super.initialize(constraintAnnotation);
	}

	@Override
	public boolean isValid(final String value, final ConstraintValidatorContext context) {

		if (value == null) {
			return nullable;
		}

		if (value.isEmpty()) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Case type cannot be blank. Valid types are: " + Arrays.toString(CaseType.values()))
				.addConstraintViolation();
			return false;
		}

		final boolean isValid = Arrays.stream(CaseType.values()).anyMatch(caseType -> caseType.name().equals(value));
		if (!isValid) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Invalid case type. Valid types are: " + Arrays.toString(CaseType.values()))
				.addConstraintViolation();
		}
		return isValid;
	}

}
