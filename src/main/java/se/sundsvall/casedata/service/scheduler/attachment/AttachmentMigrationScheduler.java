package se.sundsvall.casedata.service.scheduler.attachment;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;

/**
 * Schedules the one-off base64-to-binary attachment backfill (DRAKEN-4446). Disabled by default and activated
 * deliberately for the migration window; once every legacy row has been migrated the job is a cheap no-op and can be
 * switched off again ahead of the {@code BLOB} write-mode cutover and the later removal of the legacy {@code file}
 * column. ShedLock guarantees a single executing instance across the cluster.
 */
@Service
@ConditionalOnProperty(prefix = "scheduler.attachment-migration", name = "enabled", havingValue = "true")
public class AttachmentMigrationScheduler {

	private final AttachmentMigrationWorker attachmentMigrationWorker;

	public AttachmentMigrationScheduler(final AttachmentMigrationWorker attachmentMigrationWorker) {
		this.attachmentMigrationWorker = attachmentMigrationWorker;
	}

	@Dept44Scheduled(
		cron = "${scheduler.attachment-migration.cron}",
		name = "${scheduler.attachment-migration.name}",
		lockAtMostFor = "${scheduler.attachment-migration.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${scheduler.attachment-migration.maximum-execution-time}")
	void migrate() {
		attachmentMigrationWorker.migrateAttachments();
	}
}
