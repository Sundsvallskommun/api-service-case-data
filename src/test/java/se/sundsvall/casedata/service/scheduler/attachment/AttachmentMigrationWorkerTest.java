package se.sundsvall.casedata.service.scheduler.attachment;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import se.sundsvall.casedata.integration.db.AttachmentMigrationRepository;

import static java.util.Base64.getEncoder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttachmentMigrationWorkerTest {

	private static final int BATCH_SIZE = 100;

	@Mock
	private AttachmentMigrationRepository attachmentMigrationRepositoryMock;

	@Mock
	private PlatformTransactionManager transactionManagerMock;

	@Captor
	private ArgumentCaptor<byte[]> contentCaptor;

	@Captor
	private ArgumentCaptor<Pageable> pageableCaptor;

	private AttachmentMigrationWorker worker;

	@BeforeEach
	void setUp() {
		// The real TransactionTemplate runs the callback against this mock manager; getTransaction must yield a status.
		lenient().when(transactionManagerMock.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
		worker = new AttachmentMigrationWorker(attachmentMigrationRepositoryMock, transactionManagerMock, BATCH_SIZE);
	}

	private static String hashOf(final byte[] content) throws Exception {
		return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
	}

	@Test
	void migrateAttachmentsWithEmptyBatch() {
		// Arrange
		when(attachmentMigrationRepositoryMock.findIdsForMigration(any(Pageable.class))).thenReturn(List.of());

		// Act
		worker.migrateAttachments();

		// Assert - nothing to do: a single bounded fetch, no transactions, no writes.
		verify(attachmentMigrationRepositoryMock).findIdsForMigration(pageableCaptor.capture());
		assertThat(pageableCaptor.getValue()).isEqualTo(PageRequest.ofSize(BATCH_SIZE));
		verifyNoMoreInteractions(attachmentMigrationRepositoryMock);
		verify(transactionManagerMock, never()).getTransaction(any());
	}

	@Test
	void migrateAttachmentsWithInvalidBatchSizeSkipsRun() {
		// Arrange - a misconfigured (non-positive) batch size must not reach the repository / PageRequest.
		final var misconfigured = new AttachmentMigrationWorker(attachmentMigrationRepositoryMock, transactionManagerMock, 0);

		// Act
		misconfigured.migrateAttachments();

		// Assert - the guard short-circuits before any query or PageRequest.ofSize(0) (which would itself throw).
		verifyNoInteractions(attachmentMigrationRepositoryMock);
	}

	@Test
	void migrateAttachmentsMigratesEachRow() throws Exception {
		// Arrange - two un-migrated legacy rows (base64 in 'file', 'content' null).
		final var content1 = "first attachment".getBytes(StandardCharsets.UTF_8);
		final var content2 = "second attachment".getBytes(StandardCharsets.UTF_8);

		when(attachmentMigrationRepositoryMock.findIdsForMigration(any(Pageable.class))).thenReturn(List.of(1L, 2L));
		when(attachmentMigrationRepositoryMock.findFileById(1L)).thenReturn(Optional.of(getEncoder().encodeToString(content1)));
		when(attachmentMigrationRepositoryMock.findFileById(2L)).thenReturn(Optional.of(getEncoder().encodeToString(content2)));
		when(attachmentMigrationRepositoryMock.applyBinaryContent(any(), any(), any())).thenReturn(1);

		// Act
		worker.migrateAttachments();

		// Assert - each row is migrated, in its own transaction, with the decoded bytes and their SHA-256 hash.
		verify(attachmentMigrationRepositoryMock).applyBinaryContent(eq(1L), contentCaptor.capture(), eq(hashOf(content1)));
		verify(attachmentMigrationRepositoryMock).applyBinaryContent(eq(2L), contentCaptor.capture(), eq(hashOf(content2)));
		assertThat(contentCaptor.getAllValues().get(0)).isEqualTo(content1);
		assertThat(contentCaptor.getAllValues().get(1)).isEqualTo(content2);
		verify(transactionManagerMock, times(2)).getTransaction(any());
	}

	@Test
	void migrateAttachmentsCountsAlreadyMigratedRowAsSkipped() throws Exception {
		// Arrange - the row was migrated concurrently between the id fetch and the update, so the guarded update hits 0 rows.
		final var content = "x".getBytes(StandardCharsets.UTF_8);
		when(attachmentMigrationRepositoryMock.findIdsForMigration(any(Pageable.class))).thenReturn(List.of(1L));
		when(attachmentMigrationRepositoryMock.findFileById(1L)).thenReturn(Optional.of(getEncoder().encodeToString(content)));
		when(attachmentMigrationRepositoryMock.applyBinaryContent(eq(1L), any(), eq(hashOf(content)))).thenReturn(0);

		// Act
		worker.migrateAttachments();

		// Assert - the update was attempted but affected no rows; the worker simply moves on.
		verify(attachmentMigrationRepositoryMock).applyBinaryContent(eq(1L), any(), eq(hashOf(content)));
	}

	@Test
	void migrateAttachmentsSkipsBlankFile() {
		// Arrange - defensive: a row with a blank 'file' has nothing to decode and is never written.
		when(attachmentMigrationRepositoryMock.findIdsForMigration(any(Pageable.class))).thenReturn(List.of(1L));
		when(attachmentMigrationRepositoryMock.findFileById(1L)).thenReturn(Optional.of("   "));

		// Act
		worker.migrateAttachments();

		// Assert
		verify(attachmentMigrationRepositoryMock, never()).applyBinaryContent(any(), any(), any());
	}

	@Test
	void migrateAttachmentsSkipsRowDeletedBetweenFetchAndProcessing() {
		// Arrange - the id was returned but the row is gone by the time its file is loaded.
		when(attachmentMigrationRepositoryMock.findIdsForMigration(any(Pageable.class))).thenReturn(List.of(1L));
		when(attachmentMigrationRepositoryMock.findFileById(1L)).thenReturn(Optional.empty());

		// Act
		worker.migrateAttachments();

		// Assert
		verify(attachmentMigrationRepositoryMock, never()).applyBinaryContent(any(), any(), any());
	}

	@Test
	void migrateAttachmentsContinuesAfterAFailingRow() throws Exception {
		// Arrange - the update of the first row blows up, the second row migrates normally.
		final var content2 = "ok".getBytes(StandardCharsets.UTF_8);
		when(attachmentMigrationRepositoryMock.findIdsForMigration(any(Pageable.class))).thenReturn(List.of(1L, 2L));
		when(attachmentMigrationRepositoryMock.findFileById(1L)).thenReturn(Optional.of(getEncoder().encodeToString("boom".getBytes(StandardCharsets.UTF_8))));
		when(attachmentMigrationRepositoryMock.findFileById(2L)).thenReturn(Optional.of(getEncoder().encodeToString(content2)));
		when(attachmentMigrationRepositoryMock.applyBinaryContent(eq(1L), any(), any())).thenThrow(new RuntimeException("update failure"));
		when(attachmentMigrationRepositoryMock.applyBinaryContent(eq(2L), any(), eq(hashOf(content2)))).thenReturn(1);

		// Act
		worker.migrateAttachments();

		// Assert - the failure is isolated to its row; the second row is still migrated.
		verify(attachmentMigrationRepositoryMock).applyBinaryContent(eq(2L), contentCaptor.capture(), eq(hashOf(content2)));
		assertThat(contentCaptor.getValue()).isEqualTo(content2);
	}
}
