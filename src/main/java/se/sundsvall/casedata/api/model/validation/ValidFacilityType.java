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
import se.sundsvall.casedata.api.model.validation.enums.FacilityType;
import se.sundsvall.casedata.api.model.validation.impl.ValidFacilityTypeConstraintValidator;

/**
 * The annotated element must be a valid {@link FacilityType}.
 * Not allowed to be null or empty.
 */
@Documented
@Target({
	FIELD, CONSTRUCTOR, PARAMETER
})
@Retention(RUNTIME)
@Constraint(validatedBy = ValidFacilityTypeConstraintValidator.class)
public @interface ValidFacilityType {

	String message() default "Invalid facility type";

	boolean nullable() default true;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
