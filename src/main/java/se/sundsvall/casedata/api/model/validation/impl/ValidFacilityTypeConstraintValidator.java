package se.sundsvall.casedata.api.model.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import lombok.Setter;
import se.sundsvall.casedata.api.model.validation.ValidFacilityType;
import se.sundsvall.casedata.api.model.validation.enums.FacilityType;

@Setter
public class ValidFacilityTypeConstraintValidator implements ConstraintValidator<ValidFacilityType, String> {

	private boolean nullable;

	@Override
	public void initialize(final ValidFacilityType constraintAnnotation) {
		this.nullable = constraintAnnotation.nullable();
		ConstraintValidator.super.initialize(constraintAnnotation);
	}

	@Override
	public boolean isValid(final String value, final ConstraintValidatorContext context) {
		if (value == null) {
			return nullable;
		}

		if (value.isBlank()) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Facility type cannot be blank. Valid types are: " + Arrays.toString(FacilityType.values()))
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
