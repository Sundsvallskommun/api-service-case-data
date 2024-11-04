package se.sundsvall.casedata.api.model.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import se.sundsvall.casedata.api.model.Decision;
import se.sundsvall.casedata.api.model.validation.UniqueDecisionType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UniqueDecisionTypeValidator implements ConstraintValidator<UniqueDecisionType, List<Decision>> {

	@Override
	public boolean isValid(final List<Decision> decisions, final ConstraintValidatorContext context) {
		if (decisions == null) {
			return true;
		}

		final var decisionTypes = new HashSet<>();
		return decisions.stream().allMatch(decision -> decisionTypes.add(decision.getDecisionType()));
	}
}
