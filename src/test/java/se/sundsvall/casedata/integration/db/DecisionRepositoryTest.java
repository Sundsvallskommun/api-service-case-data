package se.sundsvall.casedata.integration.db;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static se.sundsvall.casedata.integration.db.model.enums.DecisionOutcome.APPROVAL;
import static se.sundsvall.casedata.integration.db.model.enums.DecisionType.RECOMMENDED;

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
 * DecisionRepository tests.
 *
 * @see /src/test/resources/db/testdata-junit.sql for data setup.
 */
@DataJpaTest
@Import(value = { JaversConfiguration.class, ErrandListener.class, IncomingRequestFilter.class })
@Transactional
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-junit.sql"
})
class DecisionRepositoryTest {

	@Autowired
	private DecisionRepository decisionRepository;

	@Test
	void findById() {

		// Arrange
		final var id = 1L;

		// Act
		final var result = decisionRepository.findById(id).orElseThrow();

		// Assert
		assertThat(result.getCreated()).isEqualTo(OffsetDateTime.parse("2022-12-02T15:13:45.363+01:00", ISO_DATE_TIME));
		assertThat(result.getDecisionOutcome()).isEqualTo(APPROVAL);
		assertThat(result.getDecisionType()).isEqualTo(RECOMMENDED);
		assertThat(result.getDescription()).isEqualTo("Personen är boende i Sundsvalls kommun. Nuvarande kontroll ger rekommenderat beslut att godkänna ansökan.");
	}

	@Test
	void findByIdNothingFound() {

		// Arrange
		final var id = 666L;

		// Act
		final var result = decisionRepository.findById(id);

		// Assert
		assertThat(result).isNotNull().isEmpty();
	}
}
