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
import org.springframework.transaction.annotation.Transactional;

import se.sundsvall.casedata.api.filter.IncomingRequestFilter;
import se.sundsvall.casedata.integration.db.config.JaversConfiguration;
import se.sundsvall.casedata.integration.db.listeners.ErrandListener;

/**
 * MessageAttachmentRepository tests.
 *
 * @see /src/test/resources/db/testdata-junit.sql for data setup.
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
class MessageAttachmentRepositoryTest {

	@Autowired
	private MessageAttachmentRepository messageAttachmentRepository;

	@Test
	void findById() {

		// Arrange
		final var id = "05b29c30-4512-46c0-9d82-d0f11cb04bae";

		// Act
		final var result = messageAttachmentRepository.findById(id).orElseThrow();

		// Assert
		assertThat(result.getContentType()).isEqualTo("image/png");
		assertThat(result.getName()).isEqualTo("test_image.png");
		assertThat(result.getAttachmentData().getFile()).isNotNull();
	}

	@Test
	void findByIdNothingFound() {

		// Arrange
		final var id = "NON-EXISTING";

		// Act
		final var result = messageAttachmentRepository.findById(id);

		// Assert
		assertThat(result).isNotNull().isEmpty();
	}
}
