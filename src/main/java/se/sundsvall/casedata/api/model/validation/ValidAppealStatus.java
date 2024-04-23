package se.sundsvall.casedata.api.model.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target({FIELD, CONSTRUCTOR, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = ValidAppealStatusConstraintValidator.class)
public @interface ValidAppealStatus {

	String message() default "Invalid appeal status";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
