package se.sundsvall.casedata.service;

import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.TestUtil.createAttachment;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAttachment;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAttachmentEntity;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Blob;
import java.util.List;
import java.util.Optional;
import org.jose4j.base64url.Base64;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casedata.TestUtil;
import se.sundsvall.casedata.api.model.Attachment;
import se.sundsvall.casedata.api.model.validation.enums.AttachmentCategory;
import se.sundsvall.casedata.integration.db.AttachmentRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;
import se.sundsvall.casedata.service.util.BlobBuilder;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

	@Mock
	private ErrandRepository errandRepositoryMock;

	@Mock
	private AttachmentRepository attachmentRepositoryMock;

	@InjectMocks
	private AttachmentService attachmentService;

	@Mock
	private BlobBuilder blobBuilderMock;

	@Mock
	private Blob blobMock;

	@Mock
	private AttachmentEntity attachmentEntityMock;

	@Captor
	private ArgumentCaptor<AttachmentEntity> attachmentArgumentCaptor;

	@Mock
	private HttpServletResponse servletResponseMock;

	@Mock
	private ServletOutputStream servletOutputStreamMock;

	@Test
	void findAttachment() {

		// Arrange
		final var errandId = 1L;
		final var attachmentId = 123L;
		final var attachment = toAttachmentEntity(errandId, createAttachment(AttachmentCategory.SIGNATURE), MUNICIPALITY_ID, NAMESPACE);
		when(attachmentRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(attachment));

		// Act
		final var result = attachmentService.findAttachment(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(result).isEqualTo(toAttachment(attachment));
		verify(attachmentRepositoryMock).findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE);
	}

	@Test
	void findAttachmentNotFound() {

		// Arrange
		final var errandId = 1L;
		final var attachmentId = 123L;
		when(attachmentRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(() -> attachmentService.findAttachment(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND);
		verify(attachmentRepositoryMock).findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE);
	}

	@Test
	void replace() {

		// Arrange
		final var errandId = 1L;
		final var attachmentId = 123L;
		final var attachmentEntity = toAttachmentEntity(errandId, createAttachment(AttachmentCategory.PASSPORT_PHOTO), MUNICIPALITY_ID, NAMESPACE);
		attachmentEntity.setId(attachmentId);
		final var attachment = createAttachment(AttachmentCategory.LEASE_REQUEST);
		when(attachmentRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(attachmentEntity));

		// Act
		attachmentService.replace(errandId, attachmentEntity.getId(), MUNICIPALITY_ID, NAMESPACE, attachment);

		// Assert
		verify(attachmentRepositoryMock).save(attachmentArgumentCaptor.capture());
		assertThat(attachmentArgumentCaptor.getValue()).satisfies(entity -> {
			assertThat(entity.getExtraParameters()).isEqualTo(attachment.getExtraParameters());
			assertThat(entity.getCategory()).isEqualTo(attachment.getCategory());
			assertThat(entity.getName()).isEqualTo(attachment.getName());
			assertThat(entity.getNote()).isEqualTo(attachment.getNote());
			assertThat(entity.getExtension()).isEqualTo(attachment.getExtension());
			assertThat(entity.getMimeType()).isEqualTo(attachment.getMimeType());
		});
	}

	@Test
	void update() {

		// Arrange
		final var errandId = 1L;
		final var attachmentId = 123L;
		final var dto = new Attachment();
		final var entity = new AttachmentEntity();

		when(attachmentRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(entity));

		// Act
		attachmentService.update(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE, dto);

		// Assert
		verify(attachmentRepositoryMock).save(entity);
		verifyNoMoreInteractions(attachmentRepositoryMock);
	}

	@Test
	void delete() {

		// Arrange
		final var attachmentId = 1L;
		final var errandId = 2L;
		final var attachmentEntity = TestUtil.createAttachmentEntity();

		when(attachmentRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(attachmentEntity));

		// Act
		attachmentService.delete(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		verify(attachmentRepositoryMock).findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(attachmentRepositoryMock).delete(attachmentEntity);
		verifyNoMoreInteractions(attachmentRepositoryMock);
	}

	@Test
	void deleteNotFound() {

		// Arrange
		final var attachmentId = 1L;
		final var errandId = 2L;
		when(attachmentRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(empty());

		// Act
		final var result = assertThrows(ThrowableProblem.class, () -> attachmentService.delete(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE));

		// Assert
		assertThat(result)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Attachment with id:'1' not found on errand with id:'2' in namespace:'MY_NAMESPACE' for municipality with id:'2281'");

		verify(attachmentRepositoryMock).findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(attachmentRepositoryMock, never()).delete(any(AttachmentEntity.class));
		verifyNoMoreInteractions(attachmentRepositoryMock);
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
		// Arrange
		final var errandId = 123L;
		final var attachment = toAttachmentEntity(errandId, createAttachment(AttachmentCategory.POWER_OF_ATTORNEY), MUNICIPALITY_ID, NAMESPACE);
		attachment.setErrandId(errandId);
		doReturn(attachment).when(attachmentRepositoryMock).save(any(AttachmentEntity.class));
		final var file = new MockMultipartFile("file", "file.txt", "text/plain", "file content".getBytes());

		// Act
		final var result = attachmentService.create(errandId, createAttachment(AttachmentCategory.ROAD_ALLOWANCE_APPROVAL), MUNICIPALITY_ID, NAMESPACE, file);

		// Assert
		assertEquals(attachment, result);
		verify(attachmentRepositoryMock).save(any(AttachmentEntity.class));
		verifyNoMoreInteractions(attachmentRepositoryMock);
	}

	@Test
	void findAttachmentAsStreamedResponse() throws Exception {

		// Arrange
		final var attachmentId = 1L;
		final var content = Base64.encode("file content".getBytes());
		final var contentType = "contentType";
		final var fileName = "fileName";
		final var errandId = 1L;
		when(attachmentRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(attachmentEntityMock));
		when(attachmentEntityMock.getName()).thenReturn(fileName);
		when(attachmentEntityMock.getFile()).thenReturn(content);
		when(attachmentEntityMock.getMimeType()).thenReturn(contentType);
		when(servletResponseMock.getOutputStream()).thenReturn(servletOutputStreamMock);

		// Act
		attachmentService.findAttachmentAsStreamedResponse(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE, servletResponseMock);

		// Assert
		verify(attachmentRepositoryMock).findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(servletResponseMock).addHeader(CONTENT_TYPE, contentType);
		verify(servletResponseMock).addHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
		verify(servletResponseMock).setContentLength(anyInt());
		verify(servletResponseMock).getOutputStream();
	}

	@Test
	void findAttachmentAsStreamedResponseThrowsException() throws Exception {

		// Arrange
		final var attachmentId = 1L;
		final var content = Base64.encode("file content".getBytes());
		final var contentType = "contentType";
		final var fileName = "fileName";
		final var errandId = 1L;
		when(attachmentRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(any(), eq(errandId), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(attachmentEntityMock));
		when(attachmentEntityMock.getFile()).thenReturn(content);
		when(attachmentEntityMock.getMimeType()).thenReturn(contentType);
		when(attachmentEntityMock.getName()).thenReturn(fileName);
		when(servletResponseMock.getOutputStream()).thenThrow(new IOException("testException"));

		// Act
		final var exception = Assertions.assertThrows(ThrowableProblem.class, () -> attachmentService.findAttachmentAsStreamedResponse(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE, servletResponseMock));

		// Assert
		assertThat(exception.getStatus()).isEqualTo(Status.INTERNAL_SERVER_ERROR);
		assertThat(exception.getMessage()).isEqualTo("Internal Server Error: IOException occurred when copying file with attachment id '1' to response: testException");
		verify(attachmentRepositoryMock).findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(servletResponseMock).addHeader(CONTENT_TYPE, contentType);
		verify(servletResponseMock).addHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
	}

	@Test
	void findMessageAttachmentAsStreamedResponseNotFound() {

		// Arrange
		final var errandId = 1L;
		final var attachmentId = 1L;

		// Act
		final var exception = Assertions.assertThrows(ThrowableProblem.class, () -> attachmentService.findAttachmentAsStreamedResponse(1L, errandId, MUNICIPALITY_ID, NAMESPACE, servletResponseMock));

		// Assert
		assertThat(exception.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(exception.getMessage()).isEqualTo("Not Found: Attachment with id:'%s' not found on errand with id:'%s' in namespace:'%s' for municipality with id:'%s'".formatted(attachmentId, errandId, NAMESPACE, MUNICIPALITY_ID));
		verify(attachmentRepositoryMock).findByIdAndErrandIdAndMunicipalityIdAndNamespace(1L, errandId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoInteractions(attachmentEntityMock, servletResponseMock);
	}
}
