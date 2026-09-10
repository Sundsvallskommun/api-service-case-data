package se.sundsvall.casedata.integration.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;
import se.sundsvall.casedata.service.util.BlobBuilder;
import se.sundsvall.dept44.util.HashUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;

/**
 * Verifies that the SHA-256 content hash computed in the application ({@link HashUtils}, lower-case hex) is identical
 * to the hash the database computes over the same stored bytes ({@code MariaDB SHA2(content, 256)}).
 *
 * <p>
 * This guards the migration assumption (DRAKEN-4446) that a database-side backfill of the {@code hash} column yields
 * the
 * same value the application stores, so the application and a SQL migration can be used interchangeably to compute it.
 * Combined with {@code AttachmentServiceTest}/{@code AttachmentIT} (which assert the service output equals the JVM
 * SHA-256), this transitively proves the service output matches the database hash.
 */
@SpringBootTest(classes = Application.class, webEnvironment = MOCK)
@ActiveProfiles("junit")
@Transactional
class AttachmentHashConsistencyTest {

	@Autowired
	private BlobBuilder blobBuilder;

	@PersistenceContext
	private EntityManager entityManager;

	@Test
	void applicationHashMatchesDatabaseSha2() {
		// Arrange - genuinely binary content (UTF-8 text plus embedded NUL/0xFF/control bytes), stored as a blob with the
		// hash computed exactly as the application does (SHA-256, lower-case hex).
		final var text = "Bilaga med abc 123 och text".getBytes(StandardCharsets.UTF_8);
		final var content = new byte[text.length + 4];
		System.arraycopy(text, 0, content, 0, text.length);
		content[text.length] = 0x00;
		content[text.length + 1] = 0x01;
		content[text.length + 2] = (byte) 0xFF;
		content[text.length + 3] = 0x02;
		final var applicationHash = HashUtils.sha256Hex(content);

		final var entity = AttachmentEntity.builder()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withNamespace(NAMESPACE)
			.withName("hash-check.bin")
			.withMimeType("application/octet-stream")
			.withContent(blobBuilder.createBlob(content))
			.withHash(applicationHash)
			.build();
		entityManager.persist(entity);
		entityManager.flush();

		// Act - let the database hash the same stored bytes.
		final var databaseHash = (String) entityManager
			.createNativeQuery("select lower(sha2(content, 256)) from attachment where id = :id")
			.setParameter("id", entity.getId())
			.getSingleResult();

		// Assert - application-side and database-side hashes are identical and a well-formed 64-char SHA-256 hex.
		assertThat(databaseHash)
			.isEqualTo(applicationHash)
			.hasSize(64)
			.matches("[0-9a-f]{64}");
	}
}
