package se.sundsvall.casedata.api.model.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import se.sundsvall.casedata.api.model.validation.ValidTimelinessReviewValue;
import se.sundsvall.casedata.integration.db.model.enums.TimelinessReview;

public class ValidTimelinessReviewValueConstraintValidator implements ConstraintValidator<ValidTimelinessReviewValue, String> {

	@Override
	public void initialize(ValidTimelinessReviewValue constraintAnnotation) {
		ConstraintValidator.super.initialize(constraintAnnotation);
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null || value.isBlank()) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Timeliness review value cannot be null or empty. Valid values are: " + Arrays.toString(TimelinessReview.values()))
				.addConstraintViolation();
			return false;
		}

		final var isValid = Arrays.stream(TimelinessReview.values())
			.anyMatch(appealStatus -> appealStatus.toString().equals(value));

		if (!isValid) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Invalid timeliness review value. Valid values are: " + Arrays.toString(TimelinessReview.values()))
				.addConstraintViolation();
		}

		return isValid;
	}

}
