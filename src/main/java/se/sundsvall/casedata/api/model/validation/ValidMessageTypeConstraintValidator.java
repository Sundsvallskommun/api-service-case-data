package se.sundsvall.casedata.api.model.validation;

import java.util.Arrays;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import se.sundsvall.casedata.api.model.validation.enums.MessageType;

import lombok.Setter;

@Setter
public class ValidMessageTypeConstraintValidator implements ConstraintValidator<ValidMessageType, String> {

	private boolean nullable;

	@Override
	public void initialize(final ValidMessageType constraintAnnotation) {
		this.nullable = constraintAnnotation.nullable();
		ConstraintValidator.super.initialize(constraintAnnotation);
	}

	@Override
	public boolean isValid(final String value, final ConstraintValidatorContext context) {
		if (value == null) {
			return nullable;
		}

		if (value.isBlank()) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Message type cannot be blank. Valid types are: " + Arrays.toString(MessageType.values()))
				.addConstraintViolation();
			return false;
		}

		final boolean isValid = Arrays.stream(MessageType.values()).anyMatch(messageType -> messageType.name().equals(value));
		if (!isValid) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Invalid Message type. Valid types are: " + Arrays.toString(MessageType.values()))
				.addConstraintViolation();
		}
		return isValid;
	}

}
