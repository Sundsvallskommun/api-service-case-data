package se.sundsvall.casedata.api.model.validation;

import java.util.Arrays;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import se.sundsvall.casedata.api.model.validation.enums.CaseType;

public class ValidCaseTypeConstraintValidator implements ConstraintValidator<ValidCaseType, String> {

	@Override
	public void initialize(final ValidCaseType constraintAnnotation) {
		ConstraintValidator.super.initialize(constraintAnnotation);
	}

	@Override
	public boolean isValid(final String value, final ConstraintValidatorContext context) {
		if (value == null || value.isEmpty()) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Case type cannot be null or empty. Valid types are: " + Arrays.toString(CaseType.values()))
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
