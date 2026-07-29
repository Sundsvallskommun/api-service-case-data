package se.sundsvall.casedata.integration.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;
import se.sundsvall.casedata.service.util.AttachmentContents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;

/**
 * Verifies that {@code @DynamicUpdate} on {@link AttachmentEntity} keeps a metadata-only update from rewriting the
 * binary {@code content} blob and its {@code hash}.
 *
 * <p>
 * This reproduces the race the base64-to-binary backfill (DRAKEN-4446) is exposed to: a request loads an attachment
 * while {@code content} is still null, the backfill migrates that row underneath it, and the request then flushes its
 * metadata change. Without {@code @DynamicUpdate} Hibernate writes every mapped column, so the flush would put
 * {@code content} and {@code hash} back to null and silently undo the migration - and optimistic locking would not
 * catch it, because the backfill's native update deliberately leaves the {@code @Version} column untouched (see
 * {@link AttachmentMigrationRepository#applyBinaryContent}).
 *
 * <p>
 * The same behaviour also keeps an ordinary metadata patch from rewriting a multi-megabyte blob over the wire.
 */
@SpringBootTest(classes = Application.class, webEnvironment = MOCK)
@ActiveProfiles("junit")
@Transactional
class AttachmentDynamicUpdateTest {

	@PersistenceContext
	private EntityManager entityManager;

	@Test
	void metadataUpdateDoesNotOverwriteConcurrentlyMigratedContent() {
		final var content = "Bilaga med abc 123 och text".getBytes(StandardCharsets.UTF_8);
		final var contentHash = AttachmentContents.sha256Hex(content);

		// Arrange - an un-migrated attachment: base64 in the legacy 'file' column, no blob yet.
		final var entity = AttachmentEntity.builder()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withNamespace(NAMESPACE)
			.withName("before.bin")
			.withMimeType("application/octet-stream")
			.withFile(Base64.getEncoder().encodeToString(content))
			.build();
		entityManager.persist(entity);
		entityManager.flush();
		entityManager.clear();

		// A request loads the attachment while 'content' is still null, as a metadata patch would.
		final var loaded = entityManager.find(AttachmentEntity.class, entity.getId());
		assertThat(loaded.getContent()).isNull();
		assertThat(loaded.getHash()).isNull();

		// Act - the backfill migrates the row underneath it, leaving @Version untouched.
		final var migratedRows = entityManager.createNativeQuery(
			"update attachment set content = :content, hash = :hash where id = :id and content is null")
			.setParameter("content", content)
			.setParameter("hash", contentHash)
			.setParameter("id", entity.getId())
			.executeUpdate();
		assertThat(migratedRows).isEqualTo(1);

		// ...and only then does the request apply its metadata change and flush.
		loaded.setName("after.bin");
		entityManager.flush();
		entityManager.clear();

		// Assert - the metadata change was persisted, but content and hash survived untouched.
		assertThat(readColumn("name", entity.getId())).isEqualTo("after.bin");
		assertThat(readColumn("hash", entity.getId())).isEqualTo(contentHash);
		assertThat(readColumn("lower(sha2(content, 256))", entity.getId())).isEqualTo(contentHash);
	}

	private String readColumn(final String expression, final Long id) {
		return (String) entityManager
			.createNativeQuery("select %s from attachment where id = :id".formatted(expression))
			.setParameter("id", id)
			.getSingleResult();
	}
}
