package se.sundsvall.casedata.integration.db;

import static java.time.OffsetDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;

import java.time.OffsetDateTime;
import java.util.Map;

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
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;

/**
 * AttachmentRepository tests.
 *
 * @see /src/test/resources/db/testdata-junit.sql for data setup.
 */
@DataJpaTest
@Import(value = {JaversConfiguration.class, ErrandListener.class, IncomingRequestFilter.class})
@Transactional
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-junit.sql"
})
class AttachmentRepositoryTest {

	@Autowired
	private AttachmentRepository attachmentRepository;

	@Test
	void findAllByErrandNumberAndMunicipalityId() {

		// Arrange
		final var errandNumber = "ERRAND-NUMBER-2";

		// Act
		final var result = attachmentRepository.findAllByErrandNumberAndMunicipalityIdAndNamespace(errandNumber, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(result)
			.isNotNull()
			.extracting(AttachmentEntity::getId, AttachmentEntity::getErrandNumber, AttachmentEntity::getCategory, AttachmentEntity::getExtension, AttachmentEntity::getName, AttachmentEntity::getNote)
			.containsExactly(
				tuple(2L, "ERRAND-NUMBER-2", "PASSPORT_PHOTO", ".pdf", "test2.pdf", "NOTE-2"),
				tuple(3L, "ERRAND-NUMBER-2", "POLICE_REPORT", ".pdf", "test3.pdf", "NOTE-3"),
				tuple(4L, "ERRAND-NUMBER-2", "ANSUPA", ".pdf", "test4.pdf", "NOTE-4"));
	}

	@Test
	void findAllByErrandNumberAndMunicipalityIdNothingFound() {

		// Arrange
		final var errandNumber = "NON-EXISTING";

		// Act
		final var result = attachmentRepository.findAllByErrandNumberAndMunicipalityIdAndNamespace(errandNumber, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	void findById() {

		// Arrange
		final var id = 1L;

		// Act
		final var result = attachmentRepository.findByIdAndMunicipalityIdAndNamespace(id, MUNICIPALITY_ID, NAMESPACE).orElseThrow();

		// Assert
		assertThat(result.getCategory()).isEqualTo("MEDICAL_CONFIRMATION");
		assertThat(result.getErrandNumber()).isEqualTo("ERRAND-NUMBER-1");
		assertThat(result.getExtension()).isEqualTo(".pdf");
		assertThat(result.getFile()).isEqualTo("FILE-1");
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getMimeType()).isEqualTo("application/pdf");
		assertThat(result.getName()).isEqualTo("test1.pdf");
		assertThat(result.getNote()).isEqualTo("NOTE-1");
		assertThat(result.getCreated()).isEqualTo(OffsetDateTime.parse("2022-12-02T15:13:45.363+01:00", ISO_DATE_TIME));
		assertThat(result.getUpdated()).isEqualTo(OffsetDateTime.parse("2022-12-02T15:15:01.563+01:00", ISO_DATE_TIME));
	}

	@Test
	void findByIdNothingFound() {

		// Arrange
		final var id = 666L;

		// Act
		final var result = attachmentRepository.findByIdAndMunicipalityIdAndNamespace(id, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	void deleteById() {

		// Arrange
		final var id = 1L;
		assertThat(attachmentRepository.existsByIdAndMunicipalityIdAndNamespace(id, MUNICIPALITY_ID, NAMESPACE)).isTrue();

		// Act
		attachmentRepository.deleteByIdAndMunicipalityIdAndNamespace(id, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(attachmentRepository.existsByIdAndMunicipalityIdAndNamespace(id, MUNICIPALITY_ID, NAMESPACE)).isFalse();
	}

	@Test
	void create() {

		// Arrange
		final var version = 1;
		final var category = "CategoryX";
		final var name = "file.pdf";
		final var note = "note";
		final var extension = ".pdf";
		final var mimeType = "application/pdf";
		final var file = "file";
		final var errandNumber = "PRH-2022-000029";
		final var municipalityId = "2281";
		final var extraParameters = Map.of("key", "value");
		final var entity = AttachmentEntity.builder()
			.withVersion(version)
			.withCategory(category)
			.withName(name)
			.withNote(note)
			.withMunicipalityId(municipalityId)
			.withExtension(extension)
			.withErrandNumber(errandNumber)
			.withMimeType(mimeType)
			.withFile(file)
			.withExtraParameters(extraParameters)
			.build();

		// Act
		final var result = attachmentRepository.save(entity);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getCategory()).isEqualTo(category);
		assertThat(result.getCreated()).isCloseTo(now(), within(2, SECONDS));
		assertThat(result.getErrandNumber()).isEqualTo(errandNumber);
		assertThat(result.getExtension()).isEqualTo(extension);
		assertThat(result.getExtraParameters()).isEqualTo(extraParameters);
		assertThat(result.getFile()).isEqualTo(file);
		assertThat(result.getId()).isPositive();
		assertThat(result.getMimeType()).isEqualTo(mimeType);
		assertThat(result.getName()).isEqualTo(name);
		assertThat(result.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(result.getNote()).isEqualTo(note);
		assertThat(result.getUpdated()).isCloseTo(now(), within(2, SECONDS));
		assertThat(result.getVersion()).isEqualTo(version);
	}

}
