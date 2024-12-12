package se.sundsvall.casedata.api.model.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
import se.sundsvall.casedata.api.model.validation.ValidStakeholderRole;
import se.sundsvall.casedata.api.model.validation.enums.StakeholderRole;

public class ValidStakeholderRoleConstraintValidator implements ConstraintValidator<ValidStakeholderRole, List<String>> {

	@Override
	public void initialize(final ValidStakeholderRole constraintAnnotation) {
		ConstraintValidator.super.initialize(constraintAnnotation);
	}

	@Override
	public boolean isValid(final List<String> value, final ConstraintValidatorContext context) {
		if ((value == null) || value.isEmpty()) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Stakeholder role cannot be null or empty. Valid roles are: " + Arrays.toString(Arrays.stream(StakeholderRole.values()).filter(role -> !StakeholderRole.INVOICE_RECIPENT.equals(role)).toArray()))
				.addConstraintViolation();
			return false;
		}

		final boolean isValid = value.stream().allMatch(role -> Arrays.stream(StakeholderRole.values()).anyMatch(stakeholderRole -> stakeholderRole.name().equals(role)));
		if (!isValid) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Invalid stakeholder role. Valid roles are: " + Arrays.toString(Arrays.stream(StakeholderRole.values()).filter(role -> !StakeholderRole.INVOICE_RECIPENT.equals(role)).toArray()))
				.addConstraintViolation();
		}
		return isValid;
	}
}
