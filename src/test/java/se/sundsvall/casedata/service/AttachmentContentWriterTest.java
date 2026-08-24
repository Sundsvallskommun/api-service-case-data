package se.sundsvall.casedata.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import javax.sql.rowset.serial.SerialBlob;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;
import se.sundsvall.casedata.service.util.BlobBuilder;
import se.sundsvall.dept44.problem.ThrowableProblem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ExtendWith(MockitoExtension.class)
class AttachmentContentWriterTest {

	@Mock
	private BlobBuilder blobBuilderMock;

	@InjectMocks
	private AttachmentContentWriter contentWriter;

	@Test
	void uploadIsStoredAsBlobAndHash() throws Exception {
		// Arrange - the content must be streamed (hash via DigestInputStream, blob from the input stream) and never
		// fully materialised via getBytes().
		final var content = "test content".getBytes(StandardCharsets.UTF_8);
		final var expectedHash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
		final var blob = new SerialBlob(content);
		final var attachmentEntity = new AttachmentEntity();
		final var file = mock(MultipartFile.class);
		when(file.isEmpty()).thenReturn(false);
		when(file.getSize()).thenReturn((long) content.length);
		// A fresh stream per call: pass 1 computes the hash, pass 2 backs the blob.
		when(file.getInputStream()).thenReturn(new ByteArrayInputStream(content), new ByteArrayInputStream(content));
		when(blobBuilderMock.createBlob(any(InputStream.class), anyLong())).thenReturn(blob);

		// Act
		contentWriter.applyContent(attachmentEntity, file);

		// Assert
		verify(blobBuilderMock).createBlob(any(InputStream.class), eq((long) content.length));
		verify(file, never()).getBytes();
		assertThat(attachmentEntity.getContent()).isEqualTo(blob);
		assertThat(attachmentEntity.getHash()).isEqualTo(expectedHash);
	}

	@Test
	void emptyFileIsRejected() {
		// Arrange - an empty upload must not produce a contentless attachment row.
		final var file = mock(MultipartFile.class);
		when(file.isEmpty()).thenReturn(true);
		final var attachmentEntity = new AttachmentEntity();

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> contentWriter.applyContent(attachmentEntity, file));

		// Assert
		assertThat(exception)
			.hasFieldOrPropertyWithValue("status", BAD_REQUEST)
			.hasFieldOrPropertyWithValue("detail", "The attachment content must not be empty");
		verifyNoInteractions(blobBuilderMock);
		assertThat(attachmentEntity.getContent()).isNull();
		assertThat(attachmentEntity.getHash()).isNull();
	}

	@Test
	void missingFileIsRejected() {
		// Arrange - no upload at all is treated the same as an empty one.
		final var attachmentEntity = new AttachmentEntity();

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> contentWriter.applyContent(attachmentEntity, (MultipartFile) null));

		// Assert
		assertThat(exception)
			.hasFieldOrPropertyWithValue("status", BAD_REQUEST)
			.hasFieldOrPropertyWithValue("detail", "The attachment content must not be empty");
		verifyNoInteractions(blobBuilderMock);
		assertThat(attachmentEntity.getContent()).isNull();
		assertThat(attachmentEntity.getHash()).isNull();
	}

	@Test
	void unreadableUploadIsReportedAsBadRequest() throws Exception {
		// Arrange - an upload that cannot be read is a client problem, not a server error.
		final var file = mock(MultipartFile.class);
		when(file.isEmpty()).thenReturn(false);
		when(file.getInputStream()).thenThrow(new IOException("testException"));

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> contentWriter.applyContent(new AttachmentEntity(), file));

		// Assert
		assertThat(exception)
			.hasFieldOrPropertyWithValue("status", BAD_REQUEST)
			.hasFieldOrPropertyWithValue("detail", "IOException occurred when reading uploaded file: testException");
	}

	@Test
	void materialisedContentIsStoredAsBlobAndHash() throws Exception {
		// Arrange - the collectors receive the content as a byte[] and must store it exactly like an upload.
		final var content = "collected content".getBytes(StandardCharsets.UTF_8);
		final var expectedHash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
		final var blob = new SerialBlob(content);
		final var attachmentEntity = new AttachmentEntity();
		when(blobBuilderMock.createBlob(any(byte[].class))).thenReturn(blob);

		// Act
		contentWriter.applyContent(attachmentEntity, content);

		// Assert
		verify(blobBuilderMock).createBlob(content);
		assertThat(attachmentEntity.getContent()).isEqualTo(blob);
		assertThat(attachmentEntity.getHash()).isEqualTo(expectedHash);
	}

	@Test
	void nullOrEmptyMaterialisedContentIsRejected() {
		// Arrange - the collectors must not be able to store a contentless attachment either.
		final var attachmentEntity = new AttachmentEntity();

		// Act
		final var nullException = assertThrows(ThrowableProblem.class, () -> contentWriter.applyContent(attachmentEntity, (byte[]) null));
		final var emptyException = assertThrows(ThrowableProblem.class, () -> contentWriter.applyContent(attachmentEntity, new byte[0]));

		// Assert
		assertThat(nullException)
			.hasFieldOrPropertyWithValue("status", BAD_REQUEST)
			.hasFieldOrPropertyWithValue("detail", "The attachment content must not be empty");
		assertThat(emptyException)
			.hasFieldOrPropertyWithValue("status", BAD_REQUEST)
			.hasFieldOrPropertyWithValue("detail", "The attachment content must not be empty");
		verifyNoInteractions(blobBuilderMock);
		assertThat(attachmentEntity.getContent()).isNull();
		assertThat(attachmentEntity.getHash()).isNull();
	}
}
