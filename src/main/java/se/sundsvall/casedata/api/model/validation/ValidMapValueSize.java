package se.sundsvall.casedata.api.model.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = ValidMapValueSizeValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMapValueSize {

	String message() default "Map value length is not valid";

	boolean nullable() default true;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	int max() default Integer.MAX_VALUE;

}
