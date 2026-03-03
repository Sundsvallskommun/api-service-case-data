package se.sundsvall.casedata.api.model.validation.impl;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casedata.api.model.Stakeholder;
import se.sundsvall.casedata.api.model.validation.enums.StakeholderRole;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UniqueInvoiceRecipientValidatorTest {

	@InjectMocks
	private UniqueInvoiceRecipientValidator validator;

	@ParameterizedTest
	@EnumSource(value = StakeholderRole.class, mode = Mode.EXCLUDE, names = "INVOICE_RECIPIENT")
	void testScenarioWithMultipleStakeholdersWithSameRole(StakeholderRole role) {
		final var stakeholders = List.of(
			createStakeholder(List.of(role.name())),
			createStakeholder(List.of(role.name())));

		assertThat(validator.isValid(stakeholders, null)).isTrue();
	}

	@ParameterizedTest
	@NullAndEmptySource
	void testScenarioWithRoleNullOrEmpty(List<Stakeholder> stakeholders) {

		assertThat(validator.isValid(stakeholders, null)).isTrue();
	}

	@Test
	void testSCenarioWhenListContainsExactlyOneInvoiceRecipient() {
		final var stakeholders = List.of(
			createStakeholder(List.of(StakeholderRole.APPLICANT.name())),
			createStakeholder(List.of(StakeholderRole.INVOICE_RECIPIENT.name())));

		assertThat(validator.isValid(stakeholders, null)).isTrue();
	}

	@Test
	void testSCenarioWhenListContainsMoreThanOneInvoiceRecipient() {
		final var stakeholders = List.of(
			createStakeholder(List.of(StakeholderRole.INVOICE_RECIPIENT.name())),
			createStakeholder(List.of(StakeholderRole.INVOICE_RECIPIENT.name())));

		assertThat(validator.isValid(stakeholders, null)).isFalse();
	}

	private static final Stakeholder createStakeholder(List<String> roles) {
		return Stakeholder.builder()
			.withRoles(roles)
			.build();
	}
}
