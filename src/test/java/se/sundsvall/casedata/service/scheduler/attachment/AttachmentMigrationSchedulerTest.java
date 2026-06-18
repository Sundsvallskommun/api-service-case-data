package se.sundsvall.casedata.service.scheduler.attachment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class AttachmentMigrationSchedulerTest {

	@Mock
	private AttachmentMigrationWorker attachmentMigrationWorkerMock;

	@InjectMocks
	private AttachmentMigrationScheduler attachmentMigrationScheduler;

	@Test
	void migrate() {
		// Act
		attachmentMigrationScheduler.migrate();

		// Verify
		verify(attachmentMigrationWorkerMock).migrateAttachments();
		verifyNoMoreInteractions(attachmentMigrationWorkerMock);
	}
}
