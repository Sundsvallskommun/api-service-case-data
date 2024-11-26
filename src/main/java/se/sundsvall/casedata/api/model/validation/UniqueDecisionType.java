package se.sundsvall.casedata.api.model.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import se.sundsvall.casedata.api.model.validation.impl.UniqueDecisionTypeValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = UniqueDecisionTypeValidator.class)
@Target({
	ElementType.FIELD
})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueDecisionType {
	String message() default "Errand can contain one decision of each DecisionType";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
