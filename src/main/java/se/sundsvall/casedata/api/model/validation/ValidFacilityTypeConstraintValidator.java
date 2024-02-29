package se.sundsvall.casedata.api.model.validation;

import java.util.Arrays;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import se.sundsvall.casedata.api.model.validation.enums.FacilityType;

public class ValidFacilityTypeConstraintValidator implements ConstraintValidator<ValidFacilityType, String> {

	@Override
	public void initialize(final ValidFacilityType constraintAnnotation) {
		ConstraintValidator.super.initialize(constraintAnnotation);
	}

	@Override
	public boolean isValid(final String value, final ConstraintValidatorContext context) {

		if (value == null || value.isEmpty()) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Facility type cannot be null or empty. Valid types are: " + Arrays.toString(FacilityType.values()))
				.addConstraintViolation();
			return false;
		}

		final boolean isValid = Arrays.stream(FacilityType.values()).anyMatch(facilityType -> facilityType.name().equals(value));
		if (!isValid) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Invalid facility type. Valid types are: " + Arrays.toString(FacilityType.values()))
				.addConstraintViolation();
		}
		return isValid;
	}

}
