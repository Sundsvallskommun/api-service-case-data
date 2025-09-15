package se.sundsvall.casedata.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.casedata.api.filter.IncomingRequestFilter;
import se.sundsvall.casedata.integration.db.config.JaversConfiguration;
import se.sundsvall.casedata.integration.db.listeners.ErrandListener;
import se.sundsvall.casedata.integration.db.model.CaseTypeEntity;

@DataJpaTest
@Import(value = {
	JaversConfiguration.class, ErrandListener.class, IncomingRequestFilter.class
})
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-junit.sql"
})
class CaseTypeRepositoryTest {

	@Autowired
	private CaseTypeRepository repository;

	@Test
	void create() {
		// Arrange
		final var type = "TYPE-1";
		final var displayName = "Display name 1";
		final var namespace = "namespace";
		final var municipalityId = "1234";

		final var entity = CaseTypeEntity.builder()
			.withType(type)
			.withDisplayName(displayName)
			.withNamespace(namespace)
			.withMunicipalityId(municipalityId)
			.build();

		// Act
		final var persisted = repository.saveAndFlush(entity);

		// Assert
		assertThat(persisted).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(persisted.getType()).isEqualTo(type);
		assertThat(persisted.getDisplayName()).isEqualTo(displayName);
		assertThat(persisted.getNamespace()).isEqualTo(namespace);
		assertThat(persisted.getMunicipalityId()).isEqualTo(municipalityId);
	}

	@Test
	void findById_notFound() {
		// Act
		final var result = repository.findById("NON-EXISTING");

		// Assert
		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	void updateDisplayName() {
		// Arrange
		final var type = "TYPE-2";
		final var initialName = "Initial";
		final var updatedName = "Updated";
		final var namespace = "namespace";
		final var municipalityId = "1234";

		var entity = CaseTypeEntity.builder()
			.withType(type)
			.withDisplayName(initialName)
			.withNamespace(namespace)
			.withMunicipalityId(municipalityId)
			.build();
		entity = repository.saveAndFlush(entity);

		// Act
		entity = repository.findById(entity.getId()).orElseThrow(() -> new IllegalStateException("Entity not found!"));
		entity.setDisplayName(updatedName);
		repository.saveAndFlush(entity);

		// Assert
		final var reloaded = repository.findById(entity.getId()).orElseThrow();
		assertThat(reloaded.getType()).isEqualTo(type);
		assertThat(reloaded.getDisplayName()).isEqualTo(updatedName);
		assertThat(reloaded.getNamespace()).isEqualTo(namespace);
		assertThat(reloaded.getMunicipalityId()).isEqualTo(municipalityId);
	}
}
