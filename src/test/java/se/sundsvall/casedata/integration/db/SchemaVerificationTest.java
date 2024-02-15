package se.sundsvall.casedata.integration.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("junit")
class SchemaVerificationTest {

	private static final String STORED_SCHEMA_FILE = "db/schema.sql";

	@Value("${spring.jpa.properties.jakarta.persistence.schema-generation.scripts.create-target}")
	private String generatedSchemaFile;

	@Test
	void verifySchemaUpdates() {
		final var storedSchema = getResourceFile();
		final var generatedSchema = getFile(generatedSchemaFile);

		assertThat(storedSchema).as(String.format("Please reflect modifications to entities in file: %s", STORED_SCHEMA_FILE))
			.hasSameTextualContentAs(generatedSchema);
	}

	private File getResourceFile() {
		return new File(Objects.requireNonNull(getClass().getClassLoader().getResource(STORED_SCHEMA_FILE)).getFile());
	}

	private File getFile(final String fileName) {
		return new File(fileName);
	}

}