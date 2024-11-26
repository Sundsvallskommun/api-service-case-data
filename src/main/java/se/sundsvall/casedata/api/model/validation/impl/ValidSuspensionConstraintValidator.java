package se.sundsvall.casedata.api.model.validation.impl;

import static org.apache.commons.lang3.ObjectUtils.anyNull;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import se.sundsvall.casedata.api.model.Suspension;
import se.sundsvall.casedata.api.model.validation.ValidSuspension;

public class ValidSuspensionConstraintValidator implements ConstraintValidator<ValidSuspension, Suspension> {

	@Override
	public boolean isValid(final Suspension suspension, final ConstraintValidatorContext context) {
		if (suspension == null) {
			return true;
		}

		if (anyNull(suspension.getSuspendedFrom(), suspension.getSuspendedTo())) {
			return false;
		}

		return suspension.getSuspendedFrom().isBefore(suspension.getSuspendedTo());
	}

}
