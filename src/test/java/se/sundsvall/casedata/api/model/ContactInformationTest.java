package se.sundsvall.casedata.api.model;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import se.sundsvall.casedata.integration.db.model.enums.ContactType;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class ContactInformationTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(ContactInformation.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builderTest() {
		// Arrange
		final var contactType = ContactType.PHONE;
		final var phoneNumber = "1234567890";

		// Act
		final var bean = ContactInformation.builder()
			.withContactType(contactType)
			.withValue(phoneNumber)
			.build();

		// Assert
		assertThat(bean.getContactType()).isEqualTo(contactType);
		assertThat(bean.getValue()).isEqualTo(phoneNumber);
	}

	@Test
	void testNoDirtOnEmptyBean() {
		assertThat(ContactInformation.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new ContactInformation()).hasAllNullFieldsOrProperties();
	}

}
