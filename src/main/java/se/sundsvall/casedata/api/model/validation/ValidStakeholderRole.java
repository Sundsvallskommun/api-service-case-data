package se.sundsvall.casedata.api.model.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import se.sundsvall.casedata.api.model.validation.enums.StakeholderRole;
import se.sundsvall.casedata.api.model.validation.impl.ValidStakeholderRoleConstraintValidator;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The annotated element must be a valid {@link StakeholderRole}.
 * Not allowed to be null or empty.
 */
@Documented
@Target({
	FIELD, CONSTRUCTOR, PARAMETER
})
@Retention(RUNTIME)
@Constraint(validatedBy = ValidStakeholderRoleConstraintValidator.class)
public @interface ValidStakeholderRole {

	String message() default "Invalid stakeholder role";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
