package se.sundsvall.casedata.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import se.sundsvall.casedata.integration.db.model.enums.ContactType;

class ContactInformationDTOTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(ContactInformationDTO.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builderTest() {
		final var contactType = ContactType.PHONE;
		final var phoneNumber = "1234567890";

		final var bean = ContactInformationDTO.builder()
			.withContactType(contactType)
			.withValue(phoneNumber)
			.build();

		assertThat(bean.getContactType()).isEqualTo(contactType);
		assertThat(bean.getValue()).isEqualTo(phoneNumber);
	}

	@Test
	void testNoDirtOnEmptyBean() {
		assertThat(ContactInformationDTO.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new ContactInformationDTO()).hasAllNullFieldsOrProperties();
	}
}
