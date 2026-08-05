package se.sundsvall.casedata.service;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import javax.sql.rowset.serial.SerialBlob;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.casedata.TestUtil;
import se.sundsvall.casedata.api.model.Attachment;
import se.sundsvall.casedata.api.model.validation.enums.AttachmentCategory;
import se.sundsvall.casedata.integration.db.AttachmentRepository;
import se.sundsvall.casedata.integration.db.DecisionRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;
import se.sundsvall.dept44.problem.ThrowableProblem;

import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.TestUtil.createAttachment;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAttachment;

@ExtendWith(MockitoExtension.class)
class DecisionAttachmentServiceTest {

	private static final Long ERRAND_ID = 1L;
	private static final Long DECISION_ID = 2L;

	@Mock
	private AttachmentRepository attachmentRepositoryMock;

	@Mock
	private DecisionRepository decisionRepositoryMock;

	@Mock
	private ErrandRepository errandRepositoryMock;

	@Mock
	private NotificationService notificationServiceMock;

	@Mock
	private AttachmentContentWriter contentWriterMock;

	@Mock
	private HttpServletResponse servletResponseMock;

	@Mock
	private ServletOutputStream servletOutputStreamMock;

	@InjectMocks
	private DecisionAttachmentService decisionAttachmentService;

	@Test
	void findAttachments() {
		// Arrange
		final var attachment = TestUtil.createAttachmentEntity().withErrandId(null);
		when(decisionRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(DECISION_ID, ERRAND_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(TestUtil.createDecisionEntity()));
		when(attachmentRepositoryMock.findAllByDecisionIdAndMunicipalityIdAndNamespace(DECISION_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(List.of(attachment));

		// Act
		final var result = decisionAttachmentService.findAttachments(ERRAND_ID, DECISION_ID, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertEquals(List.of(toAttachment(attachment)), result);
		verify(attachmentRepositoryMock).findAllByDecisionIdAndMunicipalityIdAndNamespace(DECISION_ID, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(attachmentRepositoryMock);
	}

	@Test
	void findAttachmentsWhenDecisionDoesNotExist() {
		// Arrange - a decision that is not on the errand must yield a 404 rather than an empty list.
		when(decisionRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(DECISION_ID, ERRAND_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(empty());

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> decisionAttachmentService.findAttachments(ERRAND_ID, DECISION_ID, MUNICIPALITY_ID, NAMESPACE));

		// Assert
		assertThat(exception)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Decision with id: 2 was not found on errand with id: 1");
		verifyNoInteractions(attachmentRepositoryMock);
	}

	@Test
	void findAttachmentAsStreamedResponse() throws Exception {
		// Arrange
		final var attachmentId = 123L;
		final var content = "test content".getBytes(StandardCharsets.UTF_8);
		final var attachmentEntity = AttachmentEntity.builder()
			.withName("document.pdf")
			.withMimeType("application/pdf")
			.withContent(new SerialBlob(content))
			.build();
		when(decisionRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(DECISION_ID, ERRAND_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(TestUtil.createDecisionEntity()));
		when(attachmentRepositoryMock.findByIdAndDecisionIdAndMunicipalityIdAndNamespace(attachmentId, DECISION_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(attachmentEntity));
		when(servletResponseMock.getOutputStream()).thenReturn(servletOutputStreamMock);

		// Act
		decisionAttachmentService.findAttachmentAsStreamedResponse(ERRAND_ID, DECISION_ID, attachmentId, MUNICIPALITY_ID, NAMESPACE, servletResponseMock);

		// Assert
		verify(servletResponseMock).addHeader(CONTENT_TYPE, "application/pdf");
		verify(servletResponseMock).addHeader(CONTENT_DISPOSITION, "attachment; filename=\"document.pdf\"");
		verify(servletResponseMock).setContentLengthLong(content.length);
	}

	@Test
	void findAttachmentAsStreamedResponseNotFound() {
		// Arrange
		final var attachmentId = 123L;
		when(decisionRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(DECISION_ID, ERRAND_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(TestUtil.createDecisionEntity()));
		when(attachmentRepositoryMock.findByIdAndDecisionIdAndMunicipalityIdAndNamespace(attachmentId, DECISION_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(empty());

		// Act
		final var exception = assertThrows(ThrowableProblem.class,
			() -> decisionAttachmentService.findAttachmentAsStreamedResponse(ERRAND_ID, DECISION_ID, attachmentId, MUNICIPALITY_ID, NAMESPACE, servletResponseMock));

		// Assert
		assertThat(exception)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Attachment with id:'123' not found on decision with id:'2' in namespace:'MY_NAMESPACE' for municipality with id:'2281'");
		verifyNoInteractions(servletResponseMock);
	}

	@Test
	void create() {
		// Arrange - the attachment carries no errand id, so it stays out of the errand attachment endpoints, and it is
		// saved through the attachment repository so that it gets its generated id and is picked up by JaVers.
		final var content = "test content".getBytes(StandardCharsets.UTF_8);
		final var file = new MockMultipartFile("file", "document.pdf", "application/pdf", content);
		final var decisionEntity = TestUtil.createDecisionEntity();
		final var attachmentsBefore = decisionEntity.getAttachments().size();
		final var errandEntity = TestUtil.createErrandEntity();
		when(decisionRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(DECISION_ID, ERRAND_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(decisionEntity));
		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(ERRAND_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errandEntity));

		// Act
		final var result = decisionAttachmentService.create(ERRAND_ID, DECISION_ID, createAttachment(AttachmentCategory.POLICE_REPORT), file, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(result.getErrandId()).isNull();
		assertThat(decisionEntity.getAttachments()).hasSize(attachmentsBefore + 1).contains(result);
		verify(contentWriterMock).applyContent(result, file);
		verify(attachmentRepositoryMock).save(result);
		verify(decisionRepositoryMock).flush();
		// Saving the decision itself would merge the new attachment as a copy and leave this instance without its id.
		verify(decisionRepositoryMock, never()).save(any());
		verify(decisionRepositoryMock, never()).saveAndFlush(any());
		verify(notificationServiceMock).create(eq(MUNICIPALITY_ID), eq(NAMESPACE), any(), same(errandEntity));
	}

	@Test
	void createWhenDecisionDoesNotExist() {
		// Arrange - a missing decision must fail before the upload is read or anything is persisted.
		final var file = mock(MultipartFile.class);
		when(decisionRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(DECISION_ID, ERRAND_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(empty());

		// Act
		final var exception = assertThrows(ThrowableProblem.class,
			() -> decisionAttachmentService.create(ERRAND_ID, DECISION_ID, createAttachment(AttachmentCategory.POLICE_REPORT), file, MUNICIPALITY_ID, NAMESPACE));

		// Assert
		assertThat(exception)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Decision with id: 2 was not found on errand with id: 1");
		verifyNoInteractions(file, contentWriterMock, notificationServiceMock, attachmentRepositoryMock, errandRepositoryMock);
		verify(decisionRepositoryMock).findByIdAndErrandIdAndMunicipalityIdAndNamespace(DECISION_ID, ERRAND_ID, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(decisionRepositoryMock);
	}

	@Test
	void update() {
		// Arrange
		final var attachmentId = 123L;
		final var entity = new AttachmentEntity();
		final var errandEntity = TestUtil.createErrandEntity();
		when(decisionRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(DECISION_ID, ERRAND_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(TestUtil.createDecisionEntity()));
		when(attachmentRepositoryMock.findWithPessimisticLockingByIdAndDecisionIdAndMunicipalityIdAndNamespace(attachmentId, DECISION_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(entity));
		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(ERRAND_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errandEntity));

		// Act
		decisionAttachmentService.update(ERRAND_ID, DECISION_ID, attachmentId, MUNICIPALITY_ID, NAMESPACE, new Attachment());

		// Assert
		verify(notificationServiceMock).create(eq(MUNICIPALITY_ID), eq(NAMESPACE), any(), same(errandEntity));
		verify(attachmentRepositoryMock).save(entity);
		verify(attachmentRepositoryMock).findWithPessimisticLockingByIdAndDecisionIdAndMunicipalityIdAndNamespace(attachmentId, DECISION_ID, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(attachmentRepositoryMock);
	}

	@Test
	void updateNotFound() {
		// Arrange
		final var attachmentId = 123L;
		when(decisionRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(DECISION_ID, ERRAND_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(TestUtil.createDecisionEntity()));
		when(attachmentRepositoryMock.findWithPessimisticLockingByIdAndDecisionIdAndMunicipalityIdAndNamespace(attachmentId, DECISION_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(empty());

		// Act
		final var exception = assertThrows(ThrowableProblem.class,
			() -> decisionAttachmentService.update(ERRAND_ID, DECISION_ID, attachmentId, MUNICIPALITY_ID, NAMESPACE, new Attachment()));

		// Assert
		assertThat(exception)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Attachment with id:'123' not found on decision with id:'2' in namespace:'MY_NAMESPACE' for municipality with id:'2281'");
		verify(attachmentRepositoryMock, never()).save(any(AttachmentEntity.class));
		verifyNoInteractions(notificationServiceMock);
	}

	@Test
	void delete() {
		// Arrange - the attachment is removed from the decision's collection and deleted through its own repository, so
		// that JaVers records the deletion.
		final var decisionEntity = TestUtil.createDecisionEntity();
		final var attachmentEntity = decisionEntity.getAttachments().getFirst();
		final var attachmentId = attachmentEntity.getId();
		final var errandEntity = TestUtil.createErrandEntity();
		when(decisionRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(DECISION_ID, ERRAND_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(decisionEntity));
		when(attachmentRepositoryMock.findWithPessimisticLockingByIdAndDecisionIdAndMunicipalityIdAndNamespace(attachmentId, DECISION_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(attachmentEntity));
		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(ERRAND_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errandEntity));

		// Act
		decisionAttachmentService.delete(ERRAND_ID, DECISION_ID, attachmentId, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(decisionEntity.getAttachments()).isEmpty();
		verify(notificationServiceMock).create(eq(MUNICIPALITY_ID), eq(NAMESPACE), any(), same(errandEntity));
		verify(attachmentRepositoryMock).delete(attachmentEntity);
	}

	@Test
	void deleteNotFound() {
		// Arrange
		final var attachmentId = 123L;
		when(decisionRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(DECISION_ID, ERRAND_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(TestUtil.createDecisionEntity()));
		when(attachmentRepositoryMock.findWithPessimisticLockingByIdAndDecisionIdAndMunicipalityIdAndNamespace(attachmentId, DECISION_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(empty());

		// Act
		final var exception = assertThrows(ThrowableProblem.class,
			() -> decisionAttachmentService.delete(ERRAND_ID, DECISION_ID, attachmentId, MUNICIPALITY_ID, NAMESPACE));

		// Assert
		assertThat(exception)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Attachment with id:'123' not found on decision with id:'2' in namespace:'MY_NAMESPACE' for municipality with id:'2281'");
		verify(attachmentRepositoryMock, never()).delete(any(AttachmentEntity.class));
		verifyNoInteractions(notificationServiceMock);
	}
}
