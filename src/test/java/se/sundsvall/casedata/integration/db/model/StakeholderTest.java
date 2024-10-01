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
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;

class StakeholderTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(Stakeholder.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCodeExcluding("errand"),
			hasValidBeanEqualsExcluding("errand"),
			hasValidBeanToStringExcluding("errand")));
	}


	@Test
	void testBuilder() {
		// Arrange
		var id = 1L;
		var errand = new Errand();
		var type = StakeholderType.PERSON;
		var municipalityId = "municipalityId";
		var namespace = "namespace";
		var firstName = "firstName";
		var lastName = "lastName";
		var personId = "personId";
		var organizationName = "organizationName";
		var organizationNumber = "organizationNumber";
		var authorizedSignatory = "authorizedSignatory";
		var adAccount = "adAccount";
		var roles = List.of("role");
		var addresses = List.of(new Address());
		var contactInformation = List.of(new ContactInformation());
		var extraParameters = Map.of("key", "value");
		var version = 1;
		var created = now();
		var updated = now();

		// Act
		var bean = Stakeholder.builder()
			.withId(id)
			.withErrand(errand)
			.withType(type)
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withFirstName(firstName)
			.withLastName(lastName)
			.withPersonId(personId)
			.withOrganizationName(organizationName)
			.withOrganizationNumber(organizationNumber)
			.withAuthorizedSignatory(authorizedSignatory)
			.withAdAccount(adAccount)
			.withRoles(roles)
			.withAddresses(addresses)
			.withContactInformation(contactInformation)
			.withExtraParameters(extraParameters)
			.withVersion(version)
			.withCreated(created)
			.withUpdated(updated)
			.build();

		// Assert
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getErrand()).isEqualTo(errand);
		assertThat(bean.getType()).isEqualTo(type);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(bean.getNamespace()).isEqualTo(namespace);
		assertThat(bean.getFirstName()).isEqualTo(firstName);
		assertThat(bean.getLastName()).isEqualTo(lastName);
		assertThat(bean.getPersonId()).isEqualTo(personId);
		assertThat(bean.getOrganizationName()).isEqualTo(organizationName);
		assertThat(bean.getOrganizationNumber()).isEqualTo(organizationNumber);
		assertThat(bean.getAuthorizedSignatory()).isEqualTo(authorizedSignatory);
		assertThat(bean.getAdAccount()).isEqualTo(adAccount);
		assertThat(bean.getRoles()).isEqualTo(roles);
		assertThat(bean.getAddresses()).isEqualTo(addresses);
		assertThat(bean.getContactInformation()).isEqualTo(contactInformation);
		assertThat(bean.getExtraParameters()).isEqualTo(extraParameters);
		assertThat(bean.getVersion()).isEqualTo(version);
		assertThat(bean.getCreated()).isEqualTo(created);
		assertThat(bean.getUpdated()).isEqualTo(updated);
	}


	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Stakeholder.builder().build()).hasAllNullFieldsOrPropertiesExcept("roles", "addresses", "contactInformation", "extraParameters", "version")
			.satisfies(bean -> {
				assertThat(bean.getRoles()).isEmpty();
				assertThat(bean.getAddresses()).isEmpty();
				assertThat(bean.getContactInformation()).isEmpty();
				assertThat(bean.getExtraParameters()).isEmpty();
				assertThat(bean.getVersion()).isZero();
			});
		assertThat(new Stakeholder()).hasAllNullFieldsOrPropertiesExcept("roles", "addresses", "contactInformation", "extraParameters", "version")
			.satisfies(bean -> {
				assertThat(bean.getRoles()).isEmpty();
				assertThat(bean.getAddresses()).isEmpty();
				assertThat(bean.getContactInformation()).isEmpty();
				assertThat(bean.getExtraParameters()).isEmpty();
				assertThat(bean.getVersion()).isZero();
			});

	}

}
