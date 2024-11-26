package se.sundsvall.casedata.integration.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.casedata.api.filter.IncomingRequestFilter;
import se.sundsvall.casedata.integration.db.config.JaversConfiguration;
import se.sundsvall.casedata.integration.db.listeners.ErrandListener;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.NoteEntity;
import se.sundsvall.casedata.integration.db.model.enums.Priority;

import java.time.OffsetDateTime;
import java.util.List;

import static java.time.OffsetDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.integration.db.model.enums.NoteType.INTERNAL;

/**
 * ErrandRepository tests.
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
class ErrandRepositoryTest {

	@Autowired
	private ErrandRepository errandRepository;

	@Test
	void findAllByIdIn() {

		// Arrange
		final var idList = List.of(2L, 3L);
		final var pageRequest = PageRequest.of(0, 10, Sort.by(ASC, "priority"));

		// Act
		final var result = errandRepository.findAllByIdInAndMunicipalityIdAndNamespace(idList, MUNICIPALITY_ID, NAMESPACE, pageRequest);

		// Assert
		assertThat(result)
			.isNotNull()
			.extracting(ErrandEntity::getId, ErrandEntity::getErrandNumber, ErrandEntity::getCaseTitleAddition, ErrandEntity::getCaseType)
			.containsExactly(
				tuple(3L, "PRH-2022-000029", "Nytt parkeringstillstånd", "PARKING_PERMIT"),
				tuple(2L, "ERRAND-NUMBER-2", "Nytt parkeringstillstånd", "PARKING_PERMIT"));
	}

	@Test
	void findAllByIdInNothingFound() {

		// Arrange
		final var idList = List.of(666L, 777L);
		final var pageRequest = PageRequest.of(0, 10, Sort.by(ASC, "priority"));

		// Act
		final var result = errandRepository.findAllByIdInAndMunicipalityIdAndNamespace(idList, MUNICIPALITY_ID, NAMESPACE, pageRequest);

		// Assert
		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	void findAllByErrandNumberStartingWith() {

		// Arrange
		final var caseTypeAbbreviation = "PRH";

		// Act
		final var result = errandRepository.findAllByErrandNumberStartingWith(caseTypeAbbreviation);

		// Assert
		assertThat(result)
			.isNotNull()
			.extracting(ErrandEntity::getId, ErrandEntity::getErrandNumber, ErrandEntity::getCaseTitleAddition, ErrandEntity::getCaseType)
			.containsExactly(tuple(3L, "PRH-2022-000029", "Nytt parkeringstillstånd", "PARKING_PERMIT"));
	}

	@Test
	void findAllByErrandNumberStartingWithNothingFound() {

		// Arrange
		final var caseTypeAbbreviation = "NON-EXISTING";

		// Act
		final var result = errandRepository.findAllByErrandNumberStartingWith(caseTypeAbbreviation);

		// Assert
		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	void findByExternalCaseId() {

		// Arrange
		final var externalCaseId = "ext-no-2";

		// Act
		final var result = errandRepository.findByExternalCaseId(externalCaseId).orElseThrow();

		// Assert
		assertThat(result)
			.extracting(ErrandEntity::getId, ErrandEntity::getErrandNumber, ErrandEntity::getCaseTitleAddition, ErrandEntity::getCaseType)
			.containsExactly(2L, "ERRAND-NUMBER-2", "Nytt parkeringstillstånd", "PARKING_PERMIT");
	}

	@Test
	void findByExternalCaseIdNothingFound() {

		// Arrange
		final var externalCaseId = "NON-EXISTING";

		// Act
		final var result = errandRepository.findByExternalCaseId(externalCaseId);

		// Assert
		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	void findByErrandNumber() {

		// Arrange
		final var errandNumber = "ERRAND-NUMBER-2";

		// Act
		final var result = errandRepository.findByErrandNumber(errandNumber).orElseThrow();

		// Assert
		assertThat(result)
			.extracting(ErrandEntity::getId, ErrandEntity::getErrandNumber, ErrandEntity::getCaseTitleAddition, ErrandEntity::getCaseType)
			.containsExactly(2L, "ERRAND-NUMBER-2", "Nytt parkeringstillstånd", "PARKING_PERMIT");
	}

	@Test
	void findByErrandNumberNothingFound() {

		// Arrange
		final var errandNumber = "NON-EXISTING";

		// Act
		final var result = errandRepository.findByExternalCaseId(errandNumber);

		// Assert
		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	void deleteById() {

		// Arrange
		final var id = 3L;
		assertThat(errandRepository.existsById(id)).isTrue();

		// Act
		errandRepository.deleteById(id);

		// Assert
		assertThat(errandRepository.existsById(id)).isFalse();
	}

	@Test
	void create() {

		// Arrange
		final var caseTitleAddition = "caseTitleAddition";
		final var caseType = "PARKING_PERMIT_RENEWAL";
		final var createdBy = "createdBy";
		final var description = "description";
		final var errandNumber = "errandNumber-123";
		final var noteText = "noteText";
		final var noteType = INTERNAL;
		final var namespace = "SBK_PARKINGPERMIT";

		final var entity = ErrandEntity.builder()
			.withCaseTitleAddition(caseTitleAddition)
			.withCaseType(caseType)
			.withCreatedByClient(createdBy)
			.withDescription(description)
			.withErrandNumber(errandNumber)
			.withNamespace(namespace)
			.withMunicipalityId(MUNICIPALITY_ID)
			.withNotes(List.of(NoteEntity.builder().withText(noteText).withNoteType(noteType).build()))
			.build();

		// Act
		final var result = errandRepository.saveAndFlush(entity);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getCaseTitleAddition()).isEqualTo(caseTitleAddition);
		assertThat(result.getCaseType()).isEqualTo(caseType);
		assertThat(result.getCreated()).isCloseTo(now(), within(2, SECONDS));
		assertThat(result.getErrandNumber()).startsWith("PRH");
		assertThat(result.getNotes())
			.extracting(NoteEntity::getText, NoteEntity::getNoteType)
			.containsExactly(tuple(noteText, noteType));
		assertThat(result.getUpdated()).isCloseTo(now(), within(2, SECONDS));
	}

	@Test
	void update() {

		// Arrange
		final var errandNumber = "PRH-2022-000029";

		final var entity = errandRepository.findByErrandNumber(errandNumber).orElseThrow();

		assertThat(entity.getId()).isEqualTo(3L);
		assertThat(entity.getErrandNumber()).isEqualTo("PRH-2022-000029");
		assertThat(entity.getCaseTitleAddition()).isEqualTo("Nytt parkeringstillstånd");
		assertThat(entity.getCaseType()).isEqualTo("PARKING_PERMIT");
		assertThat(entity.getPriority()).isEqualTo(Priority.HIGH);
		assertThat(entity.getDescription()).isEmpty();
		assertThat(entity.getPhase()).isEqualTo("Aktualisering");
		assertThat(entity.getCreated()).isEqualTo(OffsetDateTime.parse("2022-12-02T15:13:45.363+01:00", ISO_DATE_TIME));
		assertThat(entity.getUpdated()).isEqualTo(OffsetDateTime.parse("2022-12-02T15:15:01.563+01:00", ISO_DATE_TIME));

		// Act
		entity.setDescription("New description");
		entity.setPhase("Done");
		entity.setPriority(Priority.LOW);
		final var updatedErrand = errandRepository.saveAndFlush(entity);

		// Assert
		assertThat(updatedErrand).isNotNull();
		assertThat(updatedErrand.getId()).isEqualTo(3L);
		assertThat(updatedErrand.getErrandNumber()).isEqualTo("PRH-2022-000029");
		assertThat(updatedErrand.getCaseTitleAddition()).isEqualTo("Nytt parkeringstillstånd");
		assertThat(updatedErrand.getCaseType()).isEqualTo("PARKING_PERMIT");
		assertThat(updatedErrand.getPriority()).isEqualTo(Priority.LOW);
		assertThat(updatedErrand.getDescription()).isEqualTo("New description");
		assertThat(updatedErrand.getPhase()).isEqualTo("Done");
		assertThat(updatedErrand.getCreated()).isEqualTo(OffsetDateTime.parse("2022-12-02T15:13:45.363+01:00", ISO_DATE_TIME));
		assertThat(updatedErrand.getUpdated()).isCloseTo(now(), within(2, SECONDS));
	}

}
