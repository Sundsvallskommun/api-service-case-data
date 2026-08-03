package se.sundsvall.casedata.service.scheduler.attachment;

import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import se.sundsvall.casedata.integration.db.AttachmentMigrationRepository;
import se.sundsvall.casedata.service.util.AttachmentContents;

/**
 * Backfills the binary {@code content} blob and SHA-256 {@code hash} of attachments created before the base64-to-binary
 * migration (DRAKEN-4446), one bounded batch per scheduled invocation.
 *
 * <p>
 * Each attachment is processed in its own {@link TransactionTemplate#setPropagationBehavior(int) REQUIRES_NEW}
 * transaction so a single failing row neither rolls back the rest of the batch nor blocks progress: it is logged and
 * left unchanged ({@code content} stays null) to be retried on a later run. The work is idempotent — the SQL update
 * only
 * touches rows whose {@code content} is still null, and a migrated row drops out of
 * {@link AttachmentMigrationRepository#findIdsForMigration}. Because each run's batch is the lowest-id rows still
 * pending, the
 * window slides forward as rows migrate, so a few scattered failures only waste their slot in a batch and never hold up
 * the rest; only a whole batch of permanently-failing rows could stall the run, which would signal a systemic data
 * problem worth investigating (and is surfaced as a WARN with the failing ids). The job converges to a no-op once every
 * legacy row has been migrated.
 *
 * <p>
 * Rows are processed one id at a time, reading only the base64 of the row being migrated; at most one attachment is in
 * memory at once (its base64 string plus the decoded bytes), so the ~6.4 GB backfill stays within heap. The per-row
 * update binds the decoded bytes as a single statement parameter, so the database's {@code max_allowed_packet} must
 * cover the largest attachment — the same requirement as creating an attachment of that size through the API.
 */
@Component
public class AttachmentMigrationWorker {

	private static final Logger LOG = LoggerFactory.getLogger(AttachmentMigrationWorker.class);

	private final AttachmentMigrationRepository attachmentMigrationRepository;
	private final TransactionTemplate transactionTemplate;
	private final int batchSize;

	public AttachmentMigrationWorker(final AttachmentMigrationRepository attachmentMigrationRepository,
		final PlatformTransactionManager transactionManager,
		@Value("${scheduler.attachment-migration.batch-size:100}") final int batchSize) {
		this.attachmentMigrationRepository = attachmentMigrationRepository;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
		this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		this.batchSize = batchSize;
	}

	public void migrateAttachments() {
		if (batchSize < 1) {
			LOG.error("Attachment migration is misconfigured: scheduler.attachment-migration.batch-size must be >= 1 but was {}; skipping run", batchSize);
			return;
		}

		final var ids = attachmentMigrationRepository.findIdsForMigration(PageRequest.ofSize(batchSize));
		if (ids.isEmpty()) {
			LOG.info("No attachments pending base64-to-binary migration");
			return;
		}

		LOG.info("Migrating a batch of {} attachment(s) from base64 to binary storage", ids.size());
		var migrated = 0;
		final var failedIds = new ArrayList<Long>();
		for (final var id : ids) {
			try {
				if (Boolean.TRUE.equals(transactionTemplate.execute(_ -> migrate(id)))) {
					migrated++;
				}
			} catch (final Exception e) {
				failedIds.add(id);
				LOG.error("Failed to migrate attachment with id '{}' from base64 to binary storage", id, e);
			}
		}

		if (failedIds.isEmpty()) {
			LOG.info("Attachment migration batch finished: {} migrated, 0 failed", migrated);
		} else {
			LOG.warn("Attachment migration batch finished: {} migrated, {} failed. Failed attachment ids (still pending, will be retried next run): {}", migrated, failedIds.size(), failedIds);
		}
	}

	/**
	 * Migrates a single attachment within the surrounding transaction: decodes the legacy base64 {@code file} into raw
	 * bytes, computes their SHA-256 hash and writes both the binary {@code content} and the hash. Returns {@code true}
	 * when a row was actually migrated, {@code false} when it was skipped (the row was deleted, has no content to migrate,
	 * or was already migrated — the update's {@code content is null} guard then affects no rows).
	 */
	private Boolean migrate(final Long id) {
		final var base64 = attachmentMigrationRepository.findFileById(id).orElse(null);
		if ((base64 == null) || base64.isBlank()) {
			return false;
		}

		final var content = AttachmentContents.decodeBase64(base64, id);
		final var hash = AttachmentContents.sha256Hex(content);
		return attachmentMigrationRepository.applyBinaryContent(id, content, hash) > 0;
	}
}
