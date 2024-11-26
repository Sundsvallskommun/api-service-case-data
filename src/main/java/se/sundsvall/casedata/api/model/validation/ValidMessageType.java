package se.sundsvall.casedata.api.model.validation;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import se.sundsvall.casedata.api.model.validation.impl.ValidMessageTypeConstraintValidator;

@Documented
@Target({
	FIELD, CONSTRUCTOR, PARAMETER
})
@Retention(RUNTIME)
@Constraint(validatedBy = ValidMessageTypeConstraintValidator.class)
public @interface ValidMessageType {

	String message() default "Invalid message type";

	boolean nullable() default true;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
