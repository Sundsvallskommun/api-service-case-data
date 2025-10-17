package se.sundsvall.casedata.api.model.validation.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.ADMINISTRATOR;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.ALTERNATE;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.APPLICANT;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.ASSOCIATION_REPRESENTATIVE;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.BUYER;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.CASHIER;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.CHAIRMAN;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.COMPANY_SIGNATORY;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.CONTACT_PERSON;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.CONTROL_OFFICIAL;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.DELEGATE;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.DEPARTMENT_HEAD;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.DEVELOPER;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.DOCTOR;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.DRIVER;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.FELLOW_APPLICANT;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.GRANTOR;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.HEALTHCARE_PERSONNEL;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.INVOICE_RECIPIENT;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.LAND_RIGHT_OWNER;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.LEASEHOLDER;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.LEGAL_GUARDIAN;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.LEGAL_REPRESENTATIVE;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.OPERATOR;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.ORGANIZATION;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.OTHER;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.PASSENGER;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.PAYMENT_PERSON;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.PERSON;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.PROPERTY_OWNER;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.RELATIVE;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.REPORTER;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.RIGHTS_HOLDER;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.SECRETARY;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.SECTION_HEAD;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.SELLER;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.USUFRUCTUARY;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.values;

import org.junit.jupiter.api.Test;

class StakeholderRoleTest {

	@Test
	void enumValues() {

		assertThat(values()).hasSize(37);

		assertThat(values()).containsExactlyInAnyOrder(
			CONTROL_OFFICIAL,
			APPLICANT,
			PROPERTY_OWNER,
			PAYMENT_PERSON,
			INVOICE_RECIPIENT,
			OPERATOR,
			CONTACT_PERSON,
			ADMINISTRATOR,
			FELLOW_APPLICANT,
			DRIVER,
			PASSENGER,
			DOCTOR,
			GRANTOR,
			SELLER,
			BUYER,
			LEASEHOLDER,
			COMPANY_SIGNATORY,
			ASSOCIATION_REPRESENTATIVE,
			CASHIER,
			CHAIRMAN,
			LAND_RIGHT_OWNER,
			DELEGATE,
			USUFRUCTUARY,
			PERSON,
			ORGANIZATION,
			RIGHTS_HOLDER,
			DEPARTMENT_HEAD,
			DEVELOPER,
			SECTION_HEAD,
			ALTERNATE,
			SECRETARY,
			REPORTER,
			RELATIVE,
			LEGAL_GUARDIAN,
			LEGAL_REPRESENTATIVE,
			HEALTHCARE_PERSONNEL,
			OTHER);
	}
}
