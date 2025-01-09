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
class AttachmentRepositoryTest {

	@Autowired
	private AttachmentRepository attachmentRepository;

	@Test
	void findAllByErrandIdAndMunicipalityIdAndNamespace() {

		// Arrange
		final var errandId = 2L;

		// Act
		final var result = attachmentRepository.findAllByErrandIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(result)
			.isNotNull()
			.extracting(AttachmentEntity::getId, AttachmentEntity::getErrandId, AttachmentEntity::getCategory, AttachmentEntity::getExtension, AttachmentEntity::getName, AttachmentEntity::getNote)
			.containsExactly(
				tuple(2L, 2L, "PASSPORT_PHOTO", ".pdf", "test2.pdf", "NOTE-2"),
				tuple(3L, 2L, "POLICE_REPORT", ".pdf", "test3.pdf", "NOTE-3"),
				tuple(4L, 2L, "ANSUPA", ".pdf", "test4.pdf", "NOTE-4"));
	}

	@Test
	void findAllByErrandIdAndMunicipalityIdAndNamespaceNothingFound() {

		// Arrange
		final var errandId = 666L; // NON-EXISTING

		// Act
		final var result = attachmentRepository.findAllByErrandIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	void findByIdAndErrandIdAndMunicipalityIdAndNamespace() {

		// Arrange
		final var id = 1L;
		final var errandId = 1L;

		// Act
		final var result = attachmentRepository.findByIdAndErrandIdAndMunicipalityIdAndNamespace(id, errandId, MUNICIPALITY_ID, NAMESPACE).orElseThrow();

		// Assert
		assertThat(result.getCategory()).isEqualTo("MEDICAL_CONFIRMATION");
		assertThat(result.getErrandId()).isEqualTo(errandId);
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
	void findByIdAndErrandIdAndMunicipalityIdAndNamespaceNothingFound() {

		// Arrange
		final var id = 666L;
		final var errandId = 999L;

		// Act
		final var result = attachmentRepository.findByIdAndErrandIdAndMunicipalityIdAndNamespace(id, errandId, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(result).isNotNull().isEmpty();
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
		final var errandId = 3L;
		final var municipalityId = "2281";
		final var extraParameters = Map.of("key", "value");
		final var entity = AttachmentEntity.builder()
			.withVersion(version)
			.withCategory(category)
			.withName(name)
			.withNote(note)
			.withMunicipalityId(municipalityId)
			.withExtension(extension)
			.withErrandId(errandId)
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
		assertThat(result.getErrandId()).isEqualTo(errandId);
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
