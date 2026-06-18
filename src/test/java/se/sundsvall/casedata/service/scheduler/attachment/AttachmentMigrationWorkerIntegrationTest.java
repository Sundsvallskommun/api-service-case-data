package se.sundsvall.casedata.service.scheduler.attachment;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.integration.db.AttachmentRepository;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;
import se.sundsvall.casedata.service.util.BlobBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;

/**
 * End-to-end verification of the base64-to-binary backfill against a real MariaDB (TestContainers): the worker decodes
 * the legacy base64 {@code file} into the binary {@code content} column and computes a SHA-256 {@code hash} that
 * matches
 * both the JVM digest and the database's own {@code sha2(content, 256)} (DRAKEN-4446). It also proves the job is
 * idempotent and never overwrites an already-migrated row.
 *
 * <p>
 * The worker commits each row in its own {@code REQUIRES_NEW} transaction, so this test deliberately runs
 * non-transactionally and cleans the table around each case rather than relying on rollback.
 */
@SpringBootTest(classes = Application.class, webEnvironment = MOCK)
@ActiveProfiles("junit")
class AttachmentMigrationWorkerIntegrationTest {

	@Autowired
	private AttachmentMigrationWorker worker;

	@Autowired
	private AttachmentRepository attachmentRepository;

	@Autowired
	private BlobBuilder blobBuilder;

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	@BeforeEach
	@AfterEach
	void clean() {
		attachmentRepository.deleteAll();
	}

	private static byte[] binaryContent() {
		// Genuinely binary content: UTF-8 text plus embedded NUL/0xFF/control bytes.
		final var text = "Bilaga med abc 123 och text".getBytes(StandardCharsets.UTF_8);
		final var content = new byte[text.length + 4];
		System.arraycopy(text, 0, content, 0, text.length);
		content[text.length] = 0x00;
		content[text.length + 1] = 0x01;
		content[text.length + 2] = (byte) 0xFF;
		content[text.length + 3] = 0x02;
		return content;
	}

	private static String sha256Hex(final byte[] content) throws Exception {
		return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
	}

	private <T> T column(final Long id, final String sql, final Class<T> type) {
		return jdbcTemplate.queryForObject(sql, Map.of("id", id), type);
	}

	@Test
	void migratesLegacyBase64RowToBinaryWithMatchingHash() throws Exception {
		// Arrange - a legacy row: content base64-encoded in 'file', no binary 'content'/'hash' yet.
		final var raw = binaryContent();
		final var base64 = Base64.getEncoder().encodeToString(raw);
		final var expectedHash = sha256Hex(raw);
		final var id = attachmentRepository.save(AttachmentEntity.builder()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withNamespace(NAMESPACE)
			.withName("legacy.bin")
			.withMimeType("application/octet-stream")
			.withFile(base64)
			.build()).getId();

		// Act
		worker.migrateAttachments();

		// Assert - the binary 'content' holds the decoded bytes, 'hash' is the app SHA-256, the legacy 'file' is untouched.
		assertThat(column(id, "select content from attachment where id = :id", byte[].class)).isEqualTo(raw);
		assertThat(column(id, "select hash from attachment where id = :id", String.class)).isEqualTo(expectedHash);
		assertThat(column(id, "select file from attachment where id = :id", String.class)).isEqualTo(base64);

		// and the database computes the same hash over the stored bytes (java-side == db-side).
		assertThat(column(id, "select lower(sha2(content, 256)) from attachment where id = :id", String.class)).isEqualTo(expectedHash);
	}

	@Test
	void isIdempotentAcrossRepeatedRuns() throws Exception {
		// Arrange
		final var raw = binaryContent();
		final var expectedHash = sha256Hex(raw);
		final var id = attachmentRepository.save(AttachmentEntity.builder()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withNamespace(NAMESPACE)
			.withName("legacy.bin")
			.withFile(Base64.getEncoder().encodeToString(raw))
			.build()).getId();

		// Act - run the migration twice; the second run must find nothing pending and change nothing.
		worker.migrateAttachments();
		worker.migrateAttachments();

		// Assert
		assertThat(column(id, "select content from attachment where id = :id", byte[].class)).isEqualTo(raw);
		assertThat(column(id, "select hash from attachment where id = :id", String.class)).isEqualTo(expectedHash);
	}

	@Test
	void doesNotOverwriteAlreadyMigratedRow() {
		// Arrange - an already-migrated row (binary 'content' present) carrying an intentionally wrong hash. The guarded
		// update must not touch it, proving migrated rows are never rewritten.
		final var raw = binaryContent();
		final var staleHash = "0".repeat(64);
		final var id = attachmentRepository.save(AttachmentEntity.builder()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withNamespace(NAMESPACE)
			.withName("already-migrated.bin")
			.withContent(blobBuilder.createBlob(raw))
			.withHash(staleHash)
			.build()).getId();

		// Act
		worker.migrateAttachments();

		// Assert - hash is left exactly as it was.
		assertThat(column(id, "select hash from attachment where id = :id", String.class)).isEqualTo(staleHash);
	}
}
