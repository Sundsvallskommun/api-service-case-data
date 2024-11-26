package se.sundsvall.casedata.service;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.casedata.api.model.MessageRequest;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.MessageAttachmentRepository;
import se.sundsvall.casedata.integration.db.MessageRepository;
import se.sundsvall.casedata.integration.db.model.MessageAttachmentDataEntity;
import se.sundsvall.casedata.integration.db.model.MessageAttachmentEntity;
import se.sundsvall.casedata.integration.db.model.MessageEntity;
import se.sundsvall.casedata.service.scheduler.MessageMapper;
import se.sundsvall.casedata.service.util.BlobBuilder;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

	@Mock
	ErrandRepository errandRepositoryMock;

	@Mock
	private MessageMapper messageMapperMock;

	@Mock
	private MessageRepository messageRepositoryMock;

	@Mock
	private MessageAttachmentRepository messageAttachmentRepositoryMock;

	@Mock
	private MessageEntity messageMock;

	@Mock
	private MessageAttachmentEntity messageAttachmentEntityMock;

	@Mock
	private MessageAttachmentDataEntity messageAttachmentDataEntityMock;

	@Mock
	private BlobBuilder blobBuilderMock;

	@Mock
	private Blob blobMock;

	@Mock
	private HttpServletResponse servletResponseMock;

	@Mock
	private ServletOutputStream servletOutputStreamMock;

	@InjectMocks
	private MessageService messageService;

	@Test
	void getMessagesByErrandNumber() {
		// Arrange
		final var messages = List.of(MessageEntity.builder()
			.withAttachments(List.of(MessageAttachmentEntity.builder().build()))
			.build());
		when(messageRepositoryMock.findAllByErrandNumberAndMunicipalityIdAndNamespace(anyString(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(messages);
		final var messageId = RandomStringUtils.secure().nextAlphabetic(10);

		// Act
		messageService.getMessagesByErrandNumber(messageId, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		verify(messageRepositoryMock).findAllByErrandNumberAndMunicipalityIdAndNamespace(messageId, MUNICIPALITY_ID, NAMESPACE);
		verify(messageMapperMock).toMessageResponses(any());
		verifyNoMoreInteractions(messageMapperMock);
		verifyNoMoreInteractions(messageRepositoryMock);
	}

	@Test
	void getMessageAttachment() {
		// Arrange
		final var attachmentId = "attachmentId";
		when(errandRepositoryMock.existsByIdAndMunicipalityIdAndNamespace(any(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(true);
		when(messageAttachmentRepositoryMock.findByAttachmentIdAndMunicipalityIdAndNamespace(any(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(messageAttachmentEntityMock));

		// Act
		messageService.getMessageAttachment(1L, attachmentId, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		verify(messageAttachmentRepositoryMock).findByAttachmentIdAndMunicipalityIdAndNamespace(attachmentId, MUNICIPALITY_ID, NAMESPACE);
		verify(messageMapperMock).toMessageAttachment(messageAttachmentEntityMock);
	}

	@Test
	void getNonExistingMessageAttachment() {
		// Arrange
		final var attachmentId = "attachmentId";
		when(errandRepositoryMock.existsByIdAndMunicipalityIdAndNamespace(any(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(true);

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> messageService.getMessageAttachment(1L, attachmentId, MUNICIPALITY_ID, NAMESPACE));

		// Assert
		assertThat(exception.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(exception.getMessage()).isEqualTo("Not Found: MessageAttachment not found");
		verify(messageAttachmentRepositoryMock).findByAttachmentIdAndMunicipalityIdAndNamespace(attachmentId, MUNICIPALITY_ID, NAMESPACE);
		verify(messageMapperMock, never()).toMessageAttachment(any());
	}

	@Test
	void getMessageAttachmentStreamed() throws Exception {
		// Arrange
		final var attachmentId = "attachmentId";
		final var content = "content";
		final var contentType = "contentType";
		final var fileName = "fileName";
		final var inputStream = IOUtils.toInputStream(content, UTF_8);
		when(errandRepositoryMock.existsByIdAndMunicipalityIdAndNamespace(any(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(true);
		when(messageAttachmentRepositoryMock.findByAttachmentIdAndMunicipalityIdAndNamespace(any(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(messageAttachmentEntityMock));
		when(messageAttachmentEntityMock.getContentType()).thenReturn(contentType);
		when(messageAttachmentEntityMock.getName()).thenReturn(fileName);
		when(messageAttachmentEntityMock.getAttachmentData()).thenReturn(messageAttachmentDataEntityMock);
		when(messageAttachmentDataEntityMock.getFile()).thenReturn(blobMock);
		when(blobMock.length()).thenReturn((long) content.length());
		when(blobMock.getBinaryStream()).thenReturn(inputStream);
		when(servletResponseMock.getOutputStream()).thenReturn(servletOutputStreamMock);

		// Act
		messageService.getMessageAttachmentStreamed(1L, attachmentId, MUNICIPALITY_ID, NAMESPACE, servletResponseMock);

		// Assert
		verify(messageAttachmentRepositoryMock).findByAttachmentIdAndMunicipalityIdAndNamespace(attachmentId, MUNICIPALITY_ID, NAMESPACE);
		verify(messageAttachmentEntityMock).getAttachmentData();
		verify(messageAttachmentDataEntityMock).getFile();
		verify(blobMock).length();
		verify(blobMock).getBinaryStream();
		verify(servletResponseMock).addHeader(CONTENT_TYPE, contentType);
		verify(servletResponseMock).addHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
		verify(servletResponseMock).setContentLength(content.length());
		verify(servletResponseMock).getOutputStream();
	}

	@Test
	void getMessageAttachmentStreamedThrowsException() throws Exception {
		// Arrange
		final var attachmentId = "attachmentId";
		final var contentType = "contentType";
		final var fileName = "fileName";
		when(errandRepositoryMock.existsByIdAndMunicipalityIdAndNamespace(any(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(true);
		when(messageAttachmentRepositoryMock.findByAttachmentIdAndMunicipalityIdAndNamespace(any(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(messageAttachmentEntityMock));
		when(messageAttachmentEntityMock.getContentType()).thenReturn(contentType);
		when(messageAttachmentEntityMock.getName()).thenReturn(fileName);
		when(messageAttachmentEntityMock.getAttachmentData()).thenReturn(messageAttachmentDataEntityMock);
		when(messageAttachmentDataEntityMock.getFile()).thenReturn(blobMock);
		when(blobMock.length()).thenThrow(new SQLException("testException"));

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> messageService.getMessageAttachmentStreamed(1L, attachmentId, MUNICIPALITY_ID, NAMESPACE, servletResponseMock));

		// Assert
		assertThat(exception.getStatus()).isEqualTo(Status.INTERNAL_SERVER_ERROR);
		assertThat(exception.getMessage()).isEqualTo("Internal Server Error: SQLException occurred when copying file with attachment id 'attachmentId' to response: testException");
		verify(messageAttachmentRepositoryMock).findByAttachmentIdAndMunicipalityIdAndNamespace(attachmentId, MUNICIPALITY_ID, NAMESPACE);
		verify(messageAttachmentEntityMock).getAttachmentData();
		verify(messageAttachmentDataEntityMock).getFile();
		verify(servletResponseMock).addHeader(CONTENT_TYPE, contentType);
		verify(servletResponseMock).addHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
		verify(servletResponseMock, never()).setContentLength(anyInt());
		verify(servletResponseMock, never()).getOutputStream();
	}

	@Test
	void getNonExistingMessageAttachmentStreamed() {
		// Arrange
		final var attachmentId = "attachmentId";
		when(errandRepositoryMock.existsByIdAndMunicipalityIdAndNamespace(any(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(true);

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> messageService.getMessageAttachmentStreamed(1L, attachmentId, MUNICIPALITY_ID, NAMESPACE, servletResponseMock));

		// Assert
		assertThat(exception.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(exception.getMessage()).isEqualTo("Not Found: MessageAttachment not found");
		verify(messageAttachmentRepositoryMock).findByAttachmentIdAndMunicipalityIdAndNamespace(attachmentId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoInteractions(messageAttachmentEntityMock, servletResponseMock);
	}

	@Test
	void saveMessageOnErrand() {
		// Arrange
		final var request = MessageRequest.builder().build();
		when(errandRepositoryMock.existsByIdAndMunicipalityIdAndNamespace(any(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(true);

		// Act
		messageService.saveMessageOnErrand(1L, request, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		verify(messageMapperMock).toMessageEntity(request, MUNICIPALITY_ID, NAMESPACE);
		verify(messageRepositoryMock).save(any());
		verifyNoMoreInteractions(messageRepositoryMock, messageMapperMock);
	}

	@ParameterizedTest
	@ValueSource(booleans = {
		true, false
	})
	void updateViewedStatusOnExistingMessage(final boolean viewed) {
		// Arrange
		final var messageId = RandomStringUtils.secure().nextAlphabetic(10);
		when(errandRepositoryMock.existsByIdAndMunicipalityIdAndNamespace(any(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(true);
		when(messageRepositoryMock.findByMessageIdAndMunicipalityIdAndNamespace(messageId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(messageMock));

		// Act
		messageService.updateViewedStatus(1L, messageId, MUNICIPALITY_ID, NAMESPACE, viewed);

		// Assert
		verify(messageRepositoryMock).findByMessageIdAndMunicipalityIdAndNamespace(messageId, MUNICIPALITY_ID, NAMESPACE);
		verify(messageMock).setViewed(viewed);
		verify(messageRepositoryMock).save(messageMock);
	}

	@Test
	void updateViewedStatusOnNonExistingMessage() {
		// Arrange
		final var messageId = RandomStringUtils.secure().nextAlphabetic(10);
		when(errandRepositoryMock.existsByIdAndMunicipalityIdAndNamespace(any(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(true);

		// Act & assert
		assertThatThrownBy(() -> messageService.updateViewedStatus(1L, messageId, MUNICIPALITY_ID, NAMESPACE, true))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND)
			.hasFieldOrPropertyWithValue("message", "Not Found: Message with id %s not found".formatted(messageId));

		verify(messageRepositoryMock).findByMessageIdAndMunicipalityIdAndNamespace(messageId, MUNICIPALITY_ID, NAMESPACE);
		verify(messageRepositoryMock, never()).save(any());
		verifyNoInteractions(messageMock);
	}

}
