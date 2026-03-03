package se.sundsvall.casedata.integration.db;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.casedata.api.filter.IncomingRequestFilter;
import se.sundsvall.casedata.integration.db.config.JaversConfiguration;
import se.sundsvall.casedata.integration.db.listeners.ErrandListener;
import se.sundsvall.casedata.integration.db.model.AddressEntity;
import se.sundsvall.casedata.integration.db.model.ContactInformationEntity;
import se.sundsvall.casedata.integration.db.model.StakeholderEntity;

import static java.time.OffsetDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.integration.db.model.enums.AddressCategory.POSTAL_ADDRESS;
import static se.sundsvall.casedata.integration.db.model.enums.ContactType.EMAIL;
import static se.sundsvall.casedata.integration.db.model.enums.StakeholderType.PERSON;

/**
 * StakeholderRepository tests.
 *
 * @see <a href="/src/test/resources/db/testdata-junit.sql">/src/test/resources/db/testdata-junit.sql</a> for data
 *      setup.
 */
@DataJpaTest
@Import(value = {
	JaversConfiguration.class, ErrandListener.class, IncomingRequestFilter.class
})
@Transactional
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-junit.sql"
})
class StakeholderRepositoryTest {

	@Autowired
	private StakeholderRepository stakeholderRepository;

	@Test
	void findByIdAndErrandIdAndMunicipalityIdAndNamespace() {

		// Arrange
		final var id = 2L;
		final var errandId = 1L;

		// Act
		final var result = stakeholderRepository.findByIdAndErrandIdAndMunicipalityIdAndNamespace(id, errandId, MUNICIPALITY_ID, NAMESPACE).orElseThrow();

		// Assert
		assertThat(result.getCreated()).isEqualTo(OffsetDateTime.parse("2022-12-02T15:13:45.363+01:00", ISO_DATE_TIME));
		assertThat(result.getUpdated()).isEqualTo(OffsetDateTime.parse("2022-12-02T15:15:01.563+01:00", ISO_DATE_TIME));
		assertThat(result.getAdAccount()).isEqualTo("AD-1");
		assertThat(result.getFirstName()).isEqualTo("FIRST-NAME-1");
		assertThat(result.getLastName()).isEqualTo("LAST-NAME-1");
		assertThat(result.getPersonId()).isEqualTo("d7af5f83-166a-468b-ab86-da8ca30ea97c");
		assertThat(result.getType()).isEqualTo(PERSON);
		assertThat(result.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(result.getNamespace()).isEqualTo(NAMESPACE);
	}

	@Test
	void findByIdNothingFound() {

		// Arrange
		final var id = 666L;

		// Act
		final var result = stakeholderRepository.findById(id);

		// Assert
		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	void create() {

		final var firstName = "firstName";
		final var lastName = "lastName";
		final var personId = UUID.randomUUID().toString();
		final var role = "ADMINISTRATOR";
		final var version = 10;
		final var address = AddressEntity.builder()
			.withAddressCategory(POSTAL_ADDRESS)
			.withApartmentNumber("apartmentNumber")
			.withCareOf("careOf")
			.withCity("city")
			.withCountry("country")
			.withHouseNumber("houseNumber")
			.withIsZoningPlanArea(true)
			.withPostalCode("12345")
			.withPropertyDesignation("propertyDesignation")
			.withStreet("street")
			.build();
		final var contactInformation = ContactInformationEntity.builder()
			.withContactType(EMAIL)
			.withValue("contact@contact.com")
			.build();
		final var entity = StakeholderEntity.builder()
			.withAdAccount(null)
			.withAddresses(List.of(address))
			.withContactInformation(List.of(contactInformation))
			.withFirstName(firstName)
			.withLastName(lastName)
			.withPersonId(personId)
			.withRoles(List.of(role))
			.withType(PERSON)
			.withVersion(version)
			.withMunicipalityId(MUNICIPALITY_ID)
			.withNamespace(NAMESPACE)
			.build();

		// Act
		final var result = stakeholderRepository.saveAndFlush(entity);

		// Assert
		assertThat(result).isNotNull();

		assertThat(result.getId()).isPositive();
		assertThat(result.getAddresses()).isEqualTo(List.of(address));
		assertThat(result.getContactInformation()).isEqualTo(List.of(contactInformation));
		assertThat(result.getCreated()).isCloseTo(now(), within(2, SECONDS));
		assertThat(result.getFirstName()).isEqualTo(firstName);
		assertThat(result.getLastName()).isEqualTo(lastName);
		assertThat(result.getPersonId()).isEqualTo(personId);
		assertThat(result.getRoles()).isEqualTo(List.of(role));
		assertThat(result.getType()).isEqualTo(PERSON);
		assertThat(result.getUpdated()).isCloseTo(now(), within(2, SECONDS));
		assertThat(result.getVersion()).isEqualTo(version);
		assertThat(result.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(result.getNamespace()).isEqualTo(NAMESPACE);
	}
}
