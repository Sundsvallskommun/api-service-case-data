package se.sundsvall.casedata.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.util.Random;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import se.sundsvall.casedata.TestUtil;
import se.sundsvall.casedata.integration.db.model.enums.AddressCategory;

class AddressDTOTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(AddressDTO.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testFields() {
		final AddressDTO dto = TestUtil.createAddressDTO(AddressCategory.values()[new Random().nextInt(AddressCategory.values().length)]);

		assertThat(dto).isNotNull().hasNoNullFieldsOrProperties();
	}

}
