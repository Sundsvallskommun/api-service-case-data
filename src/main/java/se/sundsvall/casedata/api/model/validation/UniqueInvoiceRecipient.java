package se.sundsvall.casedata.api.model.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import se.sundsvall.casedata.api.model.validation.impl.UniqueInvoiceRecipientValidator;

@Constraint(validatedBy = UniqueInvoiceRecipientValidator.class)
@Target({
	ElementType.FIELD
})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueInvoiceRecipient {

	String message() default "Errand can only contain one stakeholder with role INVOICE_RECIPIENT";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
