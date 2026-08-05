package se.sundsvall.casedata.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;
import javax.sql.rowset.serial.SerialBlob;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
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
	void dualWriteMode() throws Exception {
		// Arrange - DUAL is the default write mode: both the base64 'file' and the binary 'content' + 'hash' are written.
		final var content = "test content".getBytes(StandardCharsets.UTF_8);
		final var expectedHash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
		final var blob = new SerialBlob(content);
		final var file = new MockMultipartFile("file", "document.pdf", "application/pdf", content);
		final var attachmentEntity = new AttachmentEntity();
		when(blobBuilderMock.createBlob(any(byte[].class))).thenReturn(blob);

		// Act
		contentWriter.applyContent(attachmentEntity, file);

		// Assert
		verify(blobBuilderMock).createBlob(content);
		assertThat(attachmentEntity.getFile()).isEqualTo(Base64.getEncoder().encodeToString(content));
		assertThat(attachmentEntity.getContent()).isEqualTo(blob);
		assertThat(attachmentEntity.getHash()).isEqualTo(expectedHash);
	}

	@Test
	void blobWriteMode() throws Exception {
		// Arrange - BLOB write mode (end state): only the binary 'content' + 'hash' are written, no base64 'file'. The
		// content must be streamed (hash via DigestInputStream, blob from the input stream) and never fully materialised
		// via getBytes().
		ReflectionTestUtils.setField(contentWriter, "writeMode", AttachmentStorageMode.BLOB);
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
		assertThat(attachmentEntity.getFile()).isNull();
	}

	@Test
	void base64WriteMode() {
		// Arrange - BASE64 write mode (rollback target): only the legacy base64 'file' is written, no blob/hash.
		ReflectionTestUtils.setField(contentWriter, "writeMode", AttachmentStorageMode.BASE64);
		final var content = "test content".getBytes(StandardCharsets.UTF_8);
		final var file = new MockMultipartFile("file", "document.pdf", "application/pdf", content);
		final var attachmentEntity = new AttachmentEntity();

		// Act
		contentWriter.applyContent(attachmentEntity, file);

		// Assert
		verifyNoInteractions(blobBuilderMock);
		assertThat(attachmentEntity.getFile()).isEqualTo(Base64.getEncoder().encodeToString(content));
		assertThat(attachmentEntity.getContent()).isNull();
		assertThat(attachmentEntity.getHash()).isNull();
	}

	@Test
	void emptyFile() {
		// Arrange - an empty upload leaves all columns unset regardless of write mode.
		final var file = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);
		final var attachmentEntity = new AttachmentEntity();

		// Act
		contentWriter.applyContent(attachmentEntity, file);

		// Assert
		verifyNoInteractions(blobBuilderMock);
		assertThat(attachmentEntity.getFile()).isNull();
		assertThat(attachmentEntity.getContent()).isNull();
		assertThat(attachmentEntity.getHash()).isNull();
	}

	@Test
	void missingFile() {
		// Arrange - no upload at all is treated the same as an empty one.
		final var attachmentEntity = new AttachmentEntity();

		// Act
		contentWriter.applyContent(attachmentEntity, null);

		// Assert
		verifyNoInteractions(blobBuilderMock);
		assertThat(attachmentEntity.getFile()).isNull();
		assertThat(attachmentEntity.getContent()).isNull();
		assertThat(attachmentEntity.getHash()).isNull();
	}

	@Test
	void unreadableUploadIsReportedAsBadRequest() throws Exception {
		// Arrange - an upload that cannot be read is a client problem, not a server error.
		final var file = mock(MultipartFile.class);
		when(file.isEmpty()).thenReturn(false);
		when(file.getBytes()).thenThrow(new IOException("testException"));

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> contentWriter.applyContent(new AttachmentEntity(), file));

		// Assert
		assertThat(exception)
			.hasFieldOrPropertyWithValue("status", BAD_REQUEST)
			.hasFieldOrPropertyWithValue("detail", "IOException occurred when reading uploaded file: testException");
	}
}
