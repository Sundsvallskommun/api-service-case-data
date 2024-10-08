package se.sundsvall.casedata.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builderTest() {
		// Arrange
		final var type = StakeholderType.PERSON;
		final var firstName = "firstName";
		final var lastName = "lastName";
		final var personId = "personId";
		final var organizationName = "organizationName";
		final var organizationNumber = "organizationNumber";
		final var authorizedSignatory = "authorizedSignatory";
		final var adAccount = "adAccount";
		final var roles = new ArrayList<String>();
		final var addresses = new ArrayList<Address>();
		final var contactInformation = new ArrayList<ContactInformation>();
		final var extraParameters = new HashMap<String, String>();

		// Act
		final var bean = Stakeholder.builder()
			.withType(type)
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
			.build();

		// Assert
		assertThat(bean.getType()).isEqualTo(type);
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
	}

	@Test
	void testNoDirtOnEmptyBean() {
		assertThat(Stakeholder.builder().build()).hasAllNullFieldsOrPropertiesExcept("id", "extraParameters", "version")
			.satisfies(bean -> {
				assertThat(bean.getId()).isZero();
				assertThat(bean.getExtraParameters()).isEmpty();
				assertThat(bean.getVersion()).isZero();
			});
		assertThat(new Stakeholder()).hasAllNullFieldsOrPropertiesExcept("id", "extraParameters", "version")
			.satisfies(bean -> {
				assertThat(bean.getId()).isZero();
				assertThat(bean.getExtraParameters()).isEmpty();
				assertThat(bean.getVersion()).isZero();
			});
	}

}
