package se.sundsvall.casedata.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEqualsExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCodeExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToStringExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class FacilityEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(FacilityEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCodeExcluding("errand"),
			hasValidBeanEqualsExcluding("errand"),
			hasValidBeanToStringExcluding("errand")));
	}

	@Test
	void builder() {
		// Arrange
		var id = 1L;
		var version = 1;
		var errand = new ErrandEntity();
		var description = "Test Description";
		var municipalityId = "Test Municipality";
		var namespace = "Test Namespace";
		var addressEntity = new AddressEntity();
		var facilityCollectionName = "Test Collection";
		var mainFacility = true;
		var facilityType = "Test Type";
		var created = now();
		var updated = now();
		var extraParameters = Map.of("key1", "value1", "key2", "value2");

		// Act
		FacilityEntity facilityEntity = FacilityEntity.builder()
			.withId(id)
			.withVersion(version)
			.withErrand(errand)
			.withDescription(description)
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withAddress(addressEntity)
			.withFacilityCollectionName(facilityCollectionName)
			.withMainFacility(mainFacility)
			.withFacilityType(facilityType)
			.withCreated(created)
			.withUpdated(updated)
			.withExtraParameters(extraParameters)
			.build();

		// Assert
		assertThat(facilityEntity.getId()).isEqualTo(id);
		assertThat(facilityEntity.getVersion()).isEqualTo(version);
		assertThat(facilityEntity.getErrand()).isEqualTo(errand);
		assertThat(facilityEntity.getDescription()).isEqualTo(description);
		assertThat(facilityEntity.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(facilityEntity.getNamespace()).isEqualTo(namespace);
		assertThat(facilityEntity.getAddress()).isEqualTo(addressEntity);
		assertThat(facilityEntity.getFacilityCollectionName()).isEqualTo(facilityCollectionName);
		assertThat(facilityEntity.isMainFacility()).isEqualTo(mainFacility);
		assertThat(facilityEntity.getFacilityType()).isEqualTo(facilityType);
		assertThat(facilityEntity.getCreated()).isEqualTo(created);
		assertThat(facilityEntity.getUpdated()).isEqualTo(updated);
		assertThat(facilityEntity.getExtraParameters()).isEqualTo(extraParameters);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(FacilityEntity.builder().build()).hasAllNullFieldsOrPropertiesExcept("extraParameters", "version", "mainFacility")
			.satisfies(bean -> {
				assertThat(bean.getExtraParameters()).isEmpty();
				assertThat(bean.isMainFacility()).isFalse();
				assertThat(bean.getVersion()).isZero();
			});
		assertThat(new FacilityEntity()).hasAllNullFieldsOrPropertiesExcept("extraParameters", "version", "mainFacility")
			.satisfies(bean -> {
				assertThat(bean.getExtraParameters()).isEmpty();
				assertThat(bean.isMainFacility()).isFalse();
				assertThat(bean.getVersion()).isZero();
			});
	}

}
