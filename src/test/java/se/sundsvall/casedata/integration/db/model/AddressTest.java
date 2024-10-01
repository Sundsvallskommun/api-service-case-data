package se.sundsvall.casedata.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import se.sundsvall.casedata.integration.db.model.enums.AddressCategory;

class AddressTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(Address.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	
	@Test
	void builder() {
		// Arrange
		var addressCategory = AddressCategory.VISITING_ADDRESS;
		var street = "street";
		var houseNumber = "houseNumber";
		var postalCode = "postalCode";
		var city = "city";
		var country = "country";
		var careOf = "careOf";
		var attention = "attention";
		var propertyDesignation = "propertyDesignation";
		var apartmentNumber = "apartmentNumber";
		var isZoningPlanArea = true;
		var invoiceMarking = "invoiceMarking";
		var location = new Coordinates();

		// Act
		var bean = Address.builder()
			.withAddressCategory(addressCategory)
			.withStreet(street)
			.withHouseNumber(houseNumber)
			.withPostalCode(postalCode)
			.withCity(city)
			.withCountry(country)
			.withCareOf(careOf)
			.withAttention(attention)
			.withPropertyDesignation(propertyDesignation)
			.withApartmentNumber(apartmentNumber)
			.withIsZoningPlanArea(isZoningPlanArea)
			.withInvoiceMarking(invoiceMarking)
			.withLocation(location)
			.build();

		// Assert
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getAddressCategory()).isEqualTo(addressCategory);
		assertThat(bean.getStreet()).isEqualTo(street);
		assertThat(bean.getHouseNumber()).isEqualTo(houseNumber);
		assertThat(bean.getPostalCode()).isEqualTo(postalCode);
		assertThat(bean.getCity()).isEqualTo(city);
		assertThat(bean.getCountry()).isEqualTo(country);
		assertThat(bean.getCareOf()).isEqualTo(careOf);
		assertThat(bean.getAttention()).isEqualTo(attention);
		assertThat(bean.getPropertyDesignation()).isEqualTo(propertyDesignation);
		assertThat(bean.getApartmentNumber()).isEqualTo(apartmentNumber);
		assertThat(bean.getIsZoningPlanArea()).isEqualTo(isZoningPlanArea);
		assertThat(bean.getInvoiceMarking()).isEqualTo(invoiceMarking);
		assertThat(bean.getLocation()).isEqualTo(location);

	}


	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Address.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new Address()).hasAllNullFieldsOrProperties();
	}

}
