package se.sundsvall.casedata.api.model.validation;

import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import se.sundsvall.casedata.api.model.validation.enums.StakeholderRole;
import se.sundsvall.casedata.api.model.validation.impl.ValidStakeholderRoleConstraintValidator;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ValidStakeholderRoleConstraintValidatorTest {

	private ValidStakeholderRoleConstraintValidator validator;

	private ConstraintValidatorContext context;

	@BeforeEach
	void setUp() {
		validator = new ValidStakeholderRoleConstraintValidator();
		context = mock(ConstraintValidatorContext.class);
	}

	@ParameterizedTest
	@EnumSource(StakeholderRole.class)
	void isValidWithValidStakeholderRole(final StakeholderRole type) {
		final var validStakeholderRole = List.of(type.name());
		assertThat(validator.isValid(validStakeholderRole, context)).isTrue();
	}

	@Test
	void isValidWithValidStakeholderRoles() {
		final var validStakeholderRoles = Arrays.stream(StakeholderRole.values()).map(StakeholderRole::name).toList();
		assertThat(validator.isValid(validStakeholderRoles, context)).isTrue();
	}

	@Test
	void isValidWithValidAndInvalidStakeholderRole() {
		final var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
		when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

		final var invalidStakeholderRole = List.of(StakeholderRole.DELEGATE.name(), "INVALID_CATEGORY");
		assertThat(validator.isValid(invalidStakeholderRole, context)).isFalse();
	}

	@Test
	void isValidWithInvalidStakeholderRole() {
		final var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
		when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

		final var invalidStakeholderRole = List.of("INVALID_CATEGORY");
		assertThat(validator.isValid(invalidStakeholderRole, context)).isFalse();
	}

	@Test
	void isValidWithNullStakeholderRole() {
		final var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
		when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

		assertThat(validator.isValid(null, context)).isFalse();
	}

	@Test
	void isValidWithEmptyStakeholderRole() {
		final var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
		when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

		final List<String> emptyList = emptyList();
		assertThat(validator.isValid(emptyList, context)).isFalse();
	}

}
