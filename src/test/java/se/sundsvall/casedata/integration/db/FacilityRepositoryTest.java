package se.sundsvall.casedata.integration.db;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;

import java.time.OffsetDateTime;
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

/**
 * FacilityRepository tests.
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
class FacilityRepositoryTest {

	@Autowired
	private FacilityRepository facilityRepository;

	@Test
	void findByIdAndErrandIdAndMunicipalityIdAndNamespace() {

		// Arrange
		final var id = 1L;
		final var errandId = 1L;

		// Act
		final var result = facilityRepository.findByIdAndErrandIdAndMunicipalityIdAndNamespace(id, errandId, MUNICIPALITY_ID, NAMESPACE).orElseThrow();

		// Assert
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getUpdated()).isEqualTo(OffsetDateTime.parse("2022-12-02T15:15:01.563+01:00", ISO_DATE_TIME));
		assertThat(result.getCreated()).isEqualTo(OffsetDateTime.parse("2022-12-02T15:13:45.363+01:00", ISO_DATE_TIME));
	}

	@Test
	void findByIdAndErrandIdAndMunicipalityIdAndNamespaceNothingFound() {

		// Arrange
		final var id = 666L;
		final var errandId = 666L;

		// Act
		final var result = facilityRepository.findByIdAndErrandIdAndMunicipalityIdAndNamespace(id, errandId, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(result).isNotNull().isEmpty();
	}

}
