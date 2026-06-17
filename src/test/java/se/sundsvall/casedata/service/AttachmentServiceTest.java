package se.sundsvall.casedata.service;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import javax.sql.rowset.serial.SerialBlob;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import se.sundsvall.casedata.TestUtil;
import se.sundsvall.casedata.api.model.Attachment;
import se.sundsvall.casedata.api.model.validation.enums.AttachmentCategory;
import se.sundsvall.casedata.integration.db.AttachmentRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;
import se.sundsvall.casedata.service.util.BlobBuilder;
import se.sundsvall.dept44.problem.ThrowableProblem;

import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.TestUtil.createAttachment;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAttachment;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAttachmentEntity;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

	@Mock
	private NotificationService notificationServiceMock;

	@Mock
	private ErrandRepository errandRepositoryMock;

	@Mock
	private AttachmentRepository attachmentRepositoryMock;

	@Mock
	private BlobBuilder blobBuilderMock;

	@Mock
	private HttpServletResponse servletResponseMock;

	@Mock
	private ServletOutputStream servletOutputStreamMock;

	@InjectMocks
	private AttachmentService attachmentService;

	@Captor
	private ArgumentCaptor<AttachmentEntity> attachmentArgumentCaptor;

	@Test
	void findAttachmentAsStreamedResponse() throws Exception {

		// Arrange
		final var errandId = 1L;
		final var attachmentId = 123L;
		final var content = "test content".getBytes(StandardCharsets.UTF_8);
		final var mimeType = "application/pdf";
		final var fileName = "document.pdf";
		final var attachmentEntity = AttachmentEntity.builder()
			.withName(fileName)
			.withMimeType(mimeType)
			.withContent(new SerialBlob(content))
			.build();
		when(attachmentRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(attachmentEntity));
		when(servletResponseMock.getOutputStream()).thenReturn(servletOutputStreamMock);

		// Act
		attachmentService.findAttachmentAsStreamedResponse(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE, servletResponseMock);

		// Assert
		verify(attachmentRepositoryMock).findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(servletResponseMock).addHeader(CONTENT_TYPE, mimeType);
		verify(servletResponseMock).addHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
		verify(servletResponseMock).setContentLengthLong(content.length);
		verify(servletResponseMock).getOutputStream();
	}

	@Test
	void findAttachmentAsStreamedResponseFromLegacyBase64File() throws Exception {

		// Arrange - rows created before the binary-storage migration only have the legacy base64 file column populated.
		final var errandId = 1L;
		final var attachmentId = 123L;
		final var content = "test content";
		final var mimeType = "application/pdf";
		final var fileName = "document.pdf";
		final var attachmentEntity = AttachmentEntity.builder()
			.withName(fileName)
			.withMimeType(mimeType)
			.withFile(Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8)))
			.build();
		when(attachmentRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(attachmentEntity));
		when(servletResponseMock.getOutputStream()).thenReturn(servletOutputStreamMock);

		// Act
		attachmentService.findAttachmentAsStreamedResponse(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE, servletResponseMock);

		// Assert
		verify(attachmentRepositoryMock).findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(servletResponseMock).addHeader(CONTENT_TYPE, mimeType);
		verify(servletResponseMock).addHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
		verify(servletResponseMock).setContentLengthLong(content.getBytes(StandardCharsets.UTF_8).length);
		verify(servletResponseMock).getOutputStream();
	}

	@Test
	void findAttachmentAsStreamedResponseNotFound() {

		// Arrange
		final var errandId = 1L;
		final var attachmentId = 123L;
		when(attachmentRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(() -> attachmentService.findAttachmentAsStreamedResponse(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE, servletResponseMock))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND);
		verify(attachmentRepositoryMock).findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoInteractions(servletResponseMock);
	}

	@Test
	void findAttachmentAsStreamedResponseThrowsException() throws Exception {

		// Arrange
		final var errandId = 1L;
		final var attachmentId = 123L;
		final var content = "test content";
		final var mimeType = "application/pdf";
		final var fileName = "document.pdf";
		final var attachmentEntity = AttachmentEntity.builder()
			.withName(fileName)
			.withMimeType(mimeType)
			.withFile(Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8)))
			.build();
		when(attachmentRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(attachmentEntity));
		when(servletResponseMock.getOutputStream()).thenThrow(new IOException("testException"));

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> attachmentService.findAttachmentAsStreamedResponse(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE, servletResponseMock));

		// Assert
		assertThat(exception.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR);
		assertThat(exception.getMessage()).isEqualTo("Internal Server Error: IOException occurred when copying file with attachment id '123' to response: testException");
		verify(servletResponseMock).addHeader(CONTENT_TYPE, mimeType);
		verify(servletResponseMock).addHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
	}

	@Test
	void findAttachmentAsStreamedResponseWithMalformedLegacyBase64() {

		// Arrange - a legacy row whose 'file' column holds non-base64 content must yield a controlled Problem, not a raw
		// IllegalArgumentException.
		final var errandId = 1L;
		final var attachmentId = 123L;
		final var attachmentEntity = AttachmentEntity.builder()
			.withName("document.pdf")
			.withMimeType("application/pdf")
			.withFile("this is not valid base64 @@@")
			.build();
		when(attachmentRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(attachmentEntity));

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> attachmentService.findAttachmentAsStreamedResponse(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE, servletResponseMock));

		// Assert
		assertThat(exception.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR);
		assertThat(exception.getMessage()).startsWith("Internal Server Error: Attachment with id '123' has malformed base64 content and cannot be streamed:");
		verifyNoInteractions(servletResponseMock);
	}

	@Test
	void update() {

		// Arrange
		final var errandId = 1L;
		final var attachmentId = 123L;
		final var dto = new Attachment();
		final var entity = new AttachmentEntity();
		final var errandEntity = TestUtil.createErrandEntity();

		when(attachmentRepositoryMock.findWithPessimisticLockingByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(entity));
		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errandEntity));

		// Act
		attachmentService.update(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE, dto);

		// Assert
		verify(attachmentRepositoryMock).findWithPessimisticLockingByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(errandRepositoryMock).findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(notificationServiceMock).create(eq(MUNICIPALITY_ID), eq(NAMESPACE), any(), same(errandEntity));
		verify(attachmentRepositoryMock).save(entity);
		verifyNoMoreInteractions(attachmentRepositoryMock);
	}

	@Test
	void delete() {

		// Arrange
		final var attachmentId = 1L;
		final var errandId = 2L;
		final var attachmentEntity = TestUtil.createAttachmentEntity();
		final var errandEntity = TestUtil.createErrandEntity();

		when(attachmentRepositoryMock.findWithPessimisticLockingByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(attachmentEntity));
		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errandEntity));

		// Act
		attachmentService.delete(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		verify(errandRepositoryMock).findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(notificationServiceMock).create(eq(MUNICIPALITY_ID), eq(NAMESPACE), any(), same(errandEntity));
		verify(attachmentRepositoryMock).findWithPessimisticLockingByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(attachmentRepositoryMock).delete(attachmentEntity);
		verifyNoMoreInteractions(attachmentRepositoryMock);
	}

	@Test
	void deleteNotFound() {

		// Arrange
		final var attachmentId = 1L;
		final var errandId = 2L;
		when(attachmentRepositoryMock.findWithPessimisticLockingByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(empty());

		// Act
		final var result = assertThrows(ThrowableProblem.class, () -> attachmentService.delete(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE));

		// Assert
		assertThat(result)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Attachment with id:'1' not found on errand with id:'2' in namespace:'MY_NAMESPACE' for municipality with id:'2281'");

		verify(attachmentRepositoryMock).findWithPessimisticLockingByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(attachmentRepositoryMock, never()).delete(any(AttachmentEntity.class));
		verifyNoMoreInteractions(attachmentRepositoryMock, notificationServiceMock);
	}

	@Test
	void findAttachments() {
		// Arrange
		final var errandId = 123L;
		final var attachment = toAttachmentEntity(errandId, createAttachment(AttachmentCategory.MEX_PROTOCOL), MUNICIPALITY_ID, NAMESPACE);
		attachment.setErrandId(errandId);
		doReturn(List.of(attachment)).when(attachmentRepositoryMock).findAllByErrandIdAndMunicipalityIdAndNamespace(any(Long.class), eq(MUNICIPALITY_ID), eq(NAMESPACE));

		// Act
		final var result = attachmentService.findAttachments(errandId, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertEquals(List.of(toAttachment(attachment)), result);
		verify(attachmentRepositoryMock).findAllByErrandIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(attachmentRepositoryMock);
	}

	@Test
	void findAttachmentsNothingFound() {
		// Arrange
		final var errandId = 123L;
		doReturn(List.of()).when(attachmentRepositoryMock).findAllByErrandIdAndMunicipalityIdAndNamespace(any(Long.class), eq(MUNICIPALITY_ID), eq(NAMESPACE));

		// Act
		final var result = attachmentService.findAttachments(errandId, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertEquals(List.of(), result);
		verify(attachmentRepositoryMock).findAllByErrandIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(attachmentRepositoryMock);
	}

	@Test
	void createWithDualWriteMode() throws Exception {
		// Arrange - DUAL is the default write mode: both the base64 'file' and the binary 'content' + 'hash' are written.
		final var errandId = 123L;
		final var content = "test content".getBytes(StandardCharsets.UTF_8);
		final var expectedHash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
		final var expectedBase64 = Base64.getEncoder().encodeToString(content);
		final var blob = new SerialBlob(content);
		final var file = new MockMultipartFile("file", "document.pdf", "application/pdf", content);
		final var savedEntity = toAttachmentEntity(errandId, createAttachment(AttachmentCategory.POWER_OF_ATTORNEY), MUNICIPALITY_ID, NAMESPACE);
		savedEntity.setErrandId(errandId);
		final var errandEntity = TestUtil.createErrandEntity();
		doReturn(savedEntity).when(attachmentRepositoryMock).save(any(AttachmentEntity.class));
		when(blobBuilderMock.createBlob(any(byte[].class))).thenReturn(blob);
		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errandEntity));

		// Act
		final var result = attachmentService.create(errandId, createAttachment(AttachmentCategory.ROAD_ALLOWANCE_APPROVAL), file, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertEquals(savedEntity, result);
		verify(errandRepositoryMock).findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(blobBuilderMock).createBlob(content);
		verify(attachmentRepositoryMock).save(attachmentArgumentCaptor.capture());
		verify(notificationServiceMock).create(eq(MUNICIPALITY_ID), eq(NAMESPACE), any(), same(errandEntity));
		verifyNoMoreInteractions(attachmentRepositoryMock);
		assertThat(attachmentArgumentCaptor.getValue().getContent()).isEqualTo(blob);
		assertThat(attachmentArgumentCaptor.getValue().getHash()).isEqualTo(expectedHash);
		assertThat(attachmentArgumentCaptor.getValue().getFile()).isEqualTo(expectedBase64);
	}

	@Test
	void createWithBlobWriteMode() throws Exception {
		// Arrange - BLOB write mode (end state): only the binary 'content' + 'hash' are written, no base64 'file'.
		ReflectionTestUtils.setField(attachmentService, "writeMode", AttachmentStorageMode.BLOB);
		final var errandId = 123L;
		final var content = "test content".getBytes(StandardCharsets.UTF_8);
		final var expectedHash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
		final var blob = new SerialBlob(content);
		final var file = new MockMultipartFile("file", "document.pdf", "application/pdf", content);
		final var savedEntity = toAttachmentEntity(errandId, createAttachment(AttachmentCategory.POWER_OF_ATTORNEY), MUNICIPALITY_ID, NAMESPACE);
		final var errandEntity = TestUtil.createErrandEntity();
		doReturn(savedEntity).when(attachmentRepositoryMock).save(any(AttachmentEntity.class));
		when(blobBuilderMock.createBlob(any(byte[].class))).thenReturn(blob);
		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errandEntity));

		// Act
		attachmentService.create(errandId, createAttachment(AttachmentCategory.ROAD_ALLOWANCE_APPROVAL), file, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		verify(blobBuilderMock).createBlob(content);
		verify(attachmentRepositoryMock).save(attachmentArgumentCaptor.capture());
		assertThat(attachmentArgumentCaptor.getValue().getContent()).isEqualTo(blob);
		assertThat(attachmentArgumentCaptor.getValue().getHash()).isEqualTo(expectedHash);
		assertThat(attachmentArgumentCaptor.getValue().getFile()).isNull();
	}

	@Test
	void createWithBase64WriteMode() {
		// Arrange - BASE64 write mode (rollback target): only the legacy base64 'file' is written, no blob/hash.
		ReflectionTestUtils.setField(attachmentService, "writeMode", AttachmentStorageMode.BASE64);
		final var errandId = 123L;
		final var content = "test content".getBytes(StandardCharsets.UTF_8);
		final var expectedBase64 = Base64.getEncoder().encodeToString(content);
		final var file = new MockMultipartFile("file", "document.pdf", "application/pdf", content);
		final var savedEntity = toAttachmentEntity(errandId, createAttachment(AttachmentCategory.POWER_OF_ATTORNEY), MUNICIPALITY_ID, NAMESPACE);
		final var errandEntity = TestUtil.createErrandEntity();
		doReturn(savedEntity).when(attachmentRepositoryMock).save(any(AttachmentEntity.class));
		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errandEntity));

		// Act
		attachmentService.create(errandId, createAttachment(AttachmentCategory.ROAD_ALLOWANCE_APPROVAL), file, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		verify(attachmentRepositoryMock).save(attachmentArgumentCaptor.capture());
		verifyNoInteractions(blobBuilderMock);
		assertThat(attachmentArgumentCaptor.getValue().getFile()).isEqualTo(expectedBase64);
		assertThat(attachmentArgumentCaptor.getValue().getContent()).isNull();
		assertThat(attachmentArgumentCaptor.getValue().getHash()).isNull();
	}

	@Test
	void createWithEmptyFile() {
		// Arrange - an empty upload leaves all columns unset regardless of write mode.
		final var errandId = 123L;
		final var file = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);
		final var savedEntity = toAttachmentEntity(errandId, createAttachment(AttachmentCategory.POWER_OF_ATTORNEY), MUNICIPALITY_ID, NAMESPACE);
		final var errandEntity = TestUtil.createErrandEntity();
		doReturn(savedEntity).when(attachmentRepositoryMock).save(any(AttachmentEntity.class));
		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errandEntity));

		// Act
		attachmentService.create(errandId, createAttachment(AttachmentCategory.ROAD_ALLOWANCE_APPROVAL), file, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		verify(attachmentRepositoryMock).save(attachmentArgumentCaptor.capture());
		verifyNoInteractions(blobBuilderMock);
		assertThat(attachmentArgumentCaptor.getValue().getContent()).isNull();
		assertThat(attachmentArgumentCaptor.getValue().getHash()).isNull();
		assertThat(attachmentArgumentCaptor.getValue().getFile()).isNull();
	}
}
