package se.sundsvall.casedata.integration.db;

import static java.time.OffsetDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static se.sundsvall.casedata.integration.db.model.enums.NoteType.INTERNAL;

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
import se.sundsvall.casedata.integration.db.model.NoteEntity;

/**
 * NoteRepository tests.
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
class NoteRepositoryTest {

	@Autowired
	private NoteRepository noteRepository;

	@Test
	void delete() {

		// Arrange
		final var id = 1L;
		final var noteEntity = noteRepository.findById(id);
		assertThat(noteEntity).isPresent();

		// Act
		noteRepository.delete(noteEntity.get());

		// Assert
		assertThat(noteRepository.existsById(id)).isFalse();
	}

	@Test
	void findById() {

		// Arrange
		final var id = 1L;

		// Act
		final var result = noteRepository.findById(id).orElseThrow();

		// Assert
		assertThat(result.getCreated()).isEqualTo(OffsetDateTime.parse("2023-10-02T15:13:45.363+02:00", ISO_DATE_TIME));
		assertThat(result.getUpdated()).isEqualTo(OffsetDateTime.parse("2023-10-02T15:13:45.363+02:00", ISO_DATE_TIME));
		assertThat(result.getCreatedBy()).isEqualTo("UNKNOWN");
		assertThat(result.getText()).isEqualTo("TEXT");
		assertThat(result.getTitle()).isEqualTo("TITLE-1");
		assertThat(result.getUpdatedBy()).isEqualTo("testUser");
		assertThat(result.getErrand().getId()).isEqualTo(1);
	}

	@Test
	void findByIdNothingFound() {

		// Arrange
		final var id = 666L;

		// Act
		final var result = noteRepository.findById(id);

		// Assert
		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	void create() {

		// Arrange
		final var noteType = INTERNAL;
		final var text = "text";
		final var title = "title";
		final var version = 2;

		final var entity = NoteEntity.builder()
			.withNoteType(noteType)
			.withText(text)
			.withTitle(title)
			.withVersion(version)
			.build();

		// Act
		final var result = noteRepository.saveAndFlush(entity);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getCreated()).isCloseTo(now(), within(2, SECONDS));
		assertThat(result.getId()).isPositive();
		assertThat(result.getNoteType()).isEqualTo(noteType);
		assertThat(result.getText()).isEqualTo(text);
		assertThat(result.getTitle()).isEqualTo(title);
		assertThat(result.getUpdated()).isCloseTo(now(), within(2, SECONDS));
		assertThat(result.getVersion()).isEqualTo(version);
	}
}
