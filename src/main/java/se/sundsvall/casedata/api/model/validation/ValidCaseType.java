package se.sundsvall.casedata.api.model.validation;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import se.sundsvall.casedata.api.model.validation.enums.CaseType;
import se.sundsvall.casedata.api.model.validation.impl.ValidCaseTypeConstraintValidator;

/**
 * The annotated element must be a valid {@link CaseType}.
 * Not allowed to be null or empty.
 */
@Documented
@Target({
	FIELD, CONSTRUCTOR, PARAMETER
})
@Retention(RUNTIME)
@Constraint(validatedBy = ValidCaseTypeConstraintValidator.class)
public @interface ValidCaseType {

	String message() default "Invalid case type";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
