package se.sundsvall.casedata.service;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.casedata.TestUtil;
import se.sundsvall.casedata.api.model.Attachment;
import se.sundsvall.casedata.api.model.validation.enums.AttachmentCategory;
import se.sundsvall.casedata.integration.db.AttachmentRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;
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
import static org.mockito.Mockito.mock;
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
	private AttachmentContentWriter contentWriterMock;

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
	void findAttachmentAsStreamedResponseWithoutContent() {

		// Arrange - every path that creates an attachment stores content, so a row without it is broken rather than empty
		// and must be reported as such instead of being served as a silently empty file.
		final var errandId = 1L;
		final var attachmentId = 123L;
		final var attachmentEntity = AttachmentEntity.builder()
			.withName("document.pdf")
			.withMimeType("application/pdf")
			.build();
		when(attachmentRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(attachmentEntity));

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> attachmentService.findAttachmentAsStreamedResponse(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE, servletResponseMock));

		// Assert
		assertThat(exception)
			.hasFieldOrPropertyWithValue("status", INTERNAL_SERVER_ERROR)
			.hasFieldOrPropertyWithValue("detail", "Attachment with id '123' has no content");
		verify(attachmentRepositoryMock).findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoInteractions(servletResponseMock);
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
		final var content = "test content".getBytes(StandardCharsets.UTF_8);
		final var mimeType = "application/pdf";
		final var fileName = "document.pdf";
		final var attachmentEntity = AttachmentEntity.builder()
			.withName(fileName)
			.withMimeType(mimeType)
			.withContent(new SerialBlob(content))
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
	void create() {
		// Arrange - how the content itself is stored is the content writer's responsibility and is covered by
		// AttachmentContentWriterTest; here only the delegation and the surrounding persistence matter.
		final var errandId = 123L;
		final var file = new MockMultipartFile("file", "document.pdf", "application/pdf", "test content".getBytes(StandardCharsets.UTF_8));
		final var savedEntity = toAttachmentEntity(errandId, createAttachment(AttachmentCategory.POWER_OF_ATTORNEY), MUNICIPALITY_ID, NAMESPACE);
		final var errandEntity = TestUtil.createErrandEntity();
		doReturn(savedEntity).when(attachmentRepositoryMock).save(any(AttachmentEntity.class));
		when(errandRepositoryMock.existsByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(true);
		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errandEntity));

		// Act
		final var result = attachmentService.create(errandId, createAttachment(AttachmentCategory.ROAD_ALLOWANCE_APPROVAL), file, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertEquals(savedEntity, result);
		verify(attachmentRepositoryMock).save(attachmentArgumentCaptor.capture());
		verify(contentWriterMock).applyContent(attachmentArgumentCaptor.getValue(), file);
		verify(errandRepositoryMock).findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(notificationServiceMock).create(eq(MUNICIPALITY_ID), eq(NAMESPACE), any(), same(errandEntity));
		verifyNoMoreInteractions(attachmentRepositoryMock);
		assertThat(attachmentArgumentCaptor.getValue().getErrandId()).isEqualTo(errandId);
	}

	@Test
	void createWhenErrandDoesNotExist() {
		// Arrange - a missing errand must fail fast (404) before the upload is read/materialised or anything is persisted.
		final var errandId = 123L;
		final var file = mock(MultipartFile.class);
		when(errandRepositoryMock.existsByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(false);

		// Act
		final var exception = assertThrows(ThrowableProblem.class,
			() -> attachmentService.create(errandId, createAttachment(AttachmentCategory.ROAD_ALLOWANCE_APPROVAL), file, MUNICIPALITY_ID, NAMESPACE));

		// Assert
		assertThat(exception)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Errand with id:'123' not found in namespace:'MY_NAMESPACE' for municipality with id:'2281'");
		verifyNoInteractions(file, contentWriterMock, notificationServiceMock);
		verify(errandRepositoryMock).existsByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(errandRepositoryMock, attachmentRepositoryMock);
	}

}
