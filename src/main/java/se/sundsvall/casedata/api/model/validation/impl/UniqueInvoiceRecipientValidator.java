package se.sundsvall.casedata.api.model.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.function.Predicate;
import se.sundsvall.casedata.api.model.Stakeholder;
import se.sundsvall.casedata.api.model.validation.UniqueInvoiceRecipient;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.INVOICE_RECIPIENT;

/**
 * Validator validating that at most only one of provided stakeholders has the role INVOICE_RECIPIENT. If more than one
 * stakeholder has the role assigned the request is considered to be faulty.
 */
public class UniqueInvoiceRecipientValidator implements ConstraintValidator<UniqueInvoiceRecipient, List<Stakeholder>> {

	@Override
	public boolean isValid(final List<Stakeholder> stakeholders, final ConstraintValidatorContext context) {
		return ofNullable(stakeholders).orElse(emptyList()).stream()
			.filter(hasInvoiceRecipientRole())
			.count() <= 1; // An errand can at most have one stakeholder with role INVOICE_RECIPIENT
	}

	private static Predicate<? super Stakeholder> hasInvoiceRecipientRole() {
		return stakeholder -> ofNullable(stakeholder.getRoles()).orElse(emptyList())
			.contains(INVOICE_RECIPIENT.name());
	}
}
