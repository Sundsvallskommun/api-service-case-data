package se.sundsvall.casedata.api.model.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import se.sundsvall.casedata.integration.db.model.enums.AppealStatus;

import java.util.Arrays;

public class ValidAppealStatusConstraintValidator implements ConstraintValidator<ValidAppealStatus, String> {

	@Override
	public void initialize(final ValidAppealStatus constraintAnnotation) {
		ConstraintValidator.super.initialize(constraintAnnotation);
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {

		if(value == null || value.isBlank()) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Appeal status cannot be null or empty. Valid values are: " + Arrays.toString(AppealStatus.values()))
				.addConstraintViolation();
			return false;
		}

		final var isValid = Arrays.stream(AppealStatus.values())
			.anyMatch(appealStatus -> appealStatus.toString().equals(value));

		if(!isValid) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Invalid appeal status. Valid values are: " + Arrays.toString(AppealStatus.values()))
				.addConstraintViolation();
		}

		return isValid;
	}
}
