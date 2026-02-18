package se.sundsvall.casedata.api.model;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class FacilityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(Facility.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builderTest() {
		// Arrange
		final var description = "description";
		final var address = Address.builder().build();
		final var facilityCollectionName = "facilityCollectionName";
		final var mainFacility = true;
		final var facilityType = "facilityType";
		final var extraParameters = new HashMap<String, String>();
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";

		// Act
		final var bean = Facility.builder()
			.withDescription(description)
			.withAddress(address)
			.withFacilityCollectionName(facilityCollectionName)
			.withMainFacility(mainFacility)
			.withFacilityType(facilityType)
			.withExtraParameters(extraParameters)
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.build();

		// Assert
		assertThat(bean.getDescription()).isEqualTo(description);
		assertThat(bean.getAddress()).isEqualTo(address);
		assertThat(bean.getFacilityCollectionName()).isEqualTo(facilityCollectionName);
		assertThat(bean.isMainFacility()).isEqualTo(mainFacility);
		assertThat(bean.getFacilityType()).isEqualTo(facilityType);
		assertThat(bean.getExtraParameters()).isEqualTo(extraParameters);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(bean.getNamespace()).isEqualTo(namespace);
	}

	@Test
	void testNoDirtOnEmptyBean() {
		assertThat(Facility.builder().build()).hasAllNullFieldsOrPropertiesExcept("extraParameters", "version", "mainFacility")
			.satisfies(bean -> {
				assertThat(bean.getExtraParameters()).isEmpty();
				assertThat(bean.isMainFacility()).isFalse();
				assertThat(bean.getVersion()).isZero();
			});
		assertThat(new Facility()).hasAllNullFieldsOrPropertiesExcept("extraParameters", "version", "mainFacility")
			.satisfies(bean -> {
				assertThat(bean.getExtraParameters()).isEmpty();
				assertThat(bean.isMainFacility()).isFalse();
				assertThat(bean.getVersion()).isZero();
			});
	}

}
