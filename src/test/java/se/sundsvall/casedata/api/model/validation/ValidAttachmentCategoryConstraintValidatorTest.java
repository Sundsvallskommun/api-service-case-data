package se.sundsvall.casedata.api.model.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import se.sundsvall.casedata.api.model.validation.enums.AttachmentCategory;
import se.sundsvall.casedata.api.model.validation.impl.ValidAttachmentCategoryConstraintValidator;

class ValidAttachmentCategoryConstraintValidatorTest {

	private ValidAttachmentCategoryConstraintValidator validator;

	private ConstraintValidatorContext context;

	@BeforeEach
	void setUp() {
		validator = new ValidAttachmentCategoryConstraintValidator();
		context = mock(ConstraintValidatorContext.class);
	}

	@ParameterizedTest
	@EnumSource(AttachmentCategory.class)
	void isValidWithValidCategory(final AttachmentCategory category) {
		final var validCategory = category.toString();
		assertThat(validator.isValid(validCategory, context)).isTrue();
	}

	@Test
	void isValidWithInvalidCategory() {
		final var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
		when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

		final var invalidCategory = "INVALID_CATEGORY";
		assertThat(validator.isValid(invalidCategory, context)).isFalse();
	}

	@Test
	void isValidWithNullCategory() {
		final var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
		when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

		assertThat(validator.isValid(null, context)).isFalse();
	}

	@Test
	void isValidWithEmptyCategory() {
		final var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
		when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

		final var emptyCategory = "";
		assertThat(validator.isValid(emptyCategory, context)).isFalse();
	}
}
