package se.sundsvall.casedata.service;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
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
import se.sundsvall.casedata.integration.db.MessageAttachmentRepository;
import se.sundsvall.casedata.integration.db.MessageRepository;
import se.sundsvall.casedata.integration.db.model.Message;
import se.sundsvall.casedata.integration.db.model.MessageAttachment;
import se.sundsvall.casedata.integration.db.model.MessageAttachmentData;
import se.sundsvall.casedata.service.scheduler.MessageMapper;
import se.sundsvall.casedata.service.util.BlobBuilder;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

	@Mock
	private MessageMapper messageMapperMock;

	@Mock
	private MessageRepository messageRepositoryMock;

	@Mock
	private MessageAttachmentRepository messageAttachmentRepositoryMock;

	@Mock
	private Message messageMock;

	@Mock
	private MessageAttachment messageAttachmentMock;

	@Mock
	private MessageAttachmentData messageAttachmentDataMock;

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

		final var messages = List.of(Message.builder()
			.withAttachments(List.of(MessageAttachment.builder().build()))
			.build());

		when(messageRepositoryMock.findAllByErrandNumberAndMunicipalityIdAndNamespace(anyString(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(messages);

		final var messageId = randomAlphabetic(10);

		messageService.getMessagesByErrandNumber(messageId, MUNICIPALITY_ID, NAMESPACE);

		verify(messageRepositoryMock).findAllByErrandNumberAndMunicipalityIdAndNamespace(messageId, MUNICIPALITY_ID, NAMESPACE);
		verify(messageMapperMock).toMessageResponses(any());
		verifyNoMoreInteractions(messageMapperMock);
		verifyNoMoreInteractions(messageRepositoryMock);
	}

	@Test
	void getMessageAttachment() {
		final var attachmentId = "attachmentId";
		when(messageAttachmentRepositoryMock.findByAttachmentIDAndMunicipalityIdAndNamespace(any(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(messageAttachmentMock));

		messageService.getMessageAttachment(attachmentId, MUNICIPALITY_ID, NAMESPACE);

		verify(messageAttachmentRepositoryMock).findByAttachmentIDAndMunicipalityIdAndNamespace(attachmentId, MUNICIPALITY_ID, NAMESPACE);
		verify(messageMapperMock).toAttachmentDto(messageAttachmentMock);
	}

	@Test
	void getNonExistingMessageAttachment() {
		final var attachmentId = "attachmentId";

		final var exception = assertThrows(ThrowableProblem.class, () -> messageService.getMessageAttachment(attachmentId, MUNICIPALITY_ID, NAMESPACE));

		assertThat(exception.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(exception.getMessage()).isEqualTo("Not Found: MessageAttachment not found");
		verify(messageAttachmentRepositoryMock).findByAttachmentIDAndMunicipalityIdAndNamespace(attachmentId, MUNICIPALITY_ID, NAMESPACE);
		verify(messageMapperMock, never()).toAttachmentDto(any());
	}

	@Test
	void getMessageAttachmentStreamed() throws Exception {
		final var attachmentId = "attachmentId";
		final var content = "content";
		final var contentType = "contentType";
		final var fileName = "fileName";
		final var inputStream = IOUtils.toInputStream(content, UTF_8);

		when(messageAttachmentRepositoryMock.findByAttachmentIDAndMunicipalityIdAndNamespace(any(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(messageAttachmentMock));
		when(messageAttachmentMock.getContentType()).thenReturn(contentType);
		when(messageAttachmentMock.getName()).thenReturn(fileName);
		when(messageAttachmentMock.getAttachmentData()).thenReturn(messageAttachmentDataMock);
		when(messageAttachmentDataMock.getFile()).thenReturn(blobMock);
		when(blobMock.length()).thenReturn((long) content.length());
		when(blobMock.getBinaryStream()).thenReturn(inputStream);
		when(servletResponseMock.getOutputStream()).thenReturn(servletOutputStreamMock);

		messageService.getMessageAttachmentStreamed(attachmentId, MUNICIPALITY_ID, NAMESPACE, servletResponseMock);

		verify(messageAttachmentRepositoryMock).findByAttachmentIDAndMunicipalityIdAndNamespace(attachmentId, MUNICIPALITY_ID, NAMESPACE);
		verify(messageAttachmentMock).getAttachmentData();
		verify(messageAttachmentDataMock).getFile();
		verify(blobMock).length();
		verify(blobMock).getBinaryStream();
		verify(servletResponseMock).addHeader(CONTENT_TYPE, contentType);
		verify(servletResponseMock).addHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
		verify(servletResponseMock).setContentLength(content.length());
		verify(servletResponseMock).getOutputStream();
	}

	@Test
	void getMessageAttachmentStreamedThrowsException() throws Exception {
		final var attachmentId = "attachmentId";
		final var contentType = "contentType";
		final var fileName = "fileName";

		when(messageAttachmentRepositoryMock.findByAttachmentIDAndMunicipalityIdAndNamespace(any(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(messageAttachmentMock));
		when(messageAttachmentMock.getContentType()).thenReturn(contentType);
		when(messageAttachmentMock.getName()).thenReturn(fileName);
		when(messageAttachmentMock.getAttachmentData()).thenReturn(messageAttachmentDataMock);
		when(messageAttachmentDataMock.getFile()).thenReturn(blobMock);
		when(blobMock.length()).thenThrow(new SQLException("testException"));

		final var exception = assertThrows(ThrowableProblem.class, () -> messageService.getMessageAttachmentStreamed(attachmentId, MUNICIPALITY_ID, NAMESPACE, servletResponseMock));

		assertThat(exception.getStatus()).isEqualTo(Status.INTERNAL_SERVER_ERROR);
		assertThat(exception.getMessage()).isEqualTo("Internal Server Error: SQLException occurred when copying file with attachment id 'attachmentId' to response: testException");
		verify(messageAttachmentRepositoryMock).findByAttachmentIDAndMunicipalityIdAndNamespace(attachmentId, MUNICIPALITY_ID, NAMESPACE);
		verify(messageAttachmentMock).getAttachmentData();
		verify(messageAttachmentDataMock).getFile();
		verify(servletResponseMock).addHeader(CONTENT_TYPE, contentType);
		verify(servletResponseMock).addHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
		verify(servletResponseMock, never()).setContentLength(anyInt());
		verify(servletResponseMock, never()).getOutputStream();
	}

	@Test
	void getNonExistingMessageAttachmentStreamed() {
		final var attachmentId = "attachmentId";

		final var exception = assertThrows(ThrowableProblem.class, () -> messageService.getMessageAttachmentStreamed(attachmentId, MUNICIPALITY_ID, NAMESPACE, servletResponseMock));

		assertThat(exception.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(exception.getMessage()).isEqualTo("Not Found: MessageAttachment not found");
		verify(messageAttachmentRepositoryMock).findByAttachmentIDAndMunicipalityIdAndNamespace(attachmentId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoInteractions(messageAttachmentMock, servletResponseMock);
	}

	@Test
	void saveMessage() {
		final var request = MessageRequest.builder().build();

		messageService.saveMessage(request, MUNICIPALITY_ID, NAMESPACE);

		verify(messageMapperMock).toMessageEntity(request, MUNICIPALITY_ID, NAMESPACE);
		verify(messageRepositoryMock).save(any());
		verifyNoMoreInteractions(messageRepositoryMock, messageMapperMock);
	}

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	void updateViewedStatusOnExistingMessage(final boolean viewed) {
		final var messageId = randomAlphabetic(10);

		when(messageRepositoryMock.findByMessageIDAndMunicipalityIdAndNamespace(messageId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(messageMock));

		messageService.updateViewedStatus(messageId, MUNICIPALITY_ID, NAMESPACE, viewed);

		verify(messageRepositoryMock).findByMessageIDAndMunicipalityIdAndNamespace(messageId, MUNICIPALITY_ID, NAMESPACE);
		verify(messageMock).setViewed(viewed);
		verify(messageRepositoryMock).save(messageMock);
	}

	@Test
	void updateViewedStatusOnNonExistingMessage() {

		final var messageId = randomAlphabetic(10);

		assertThatThrownBy(() -> messageService.updateViewedStatus(messageId, MUNICIPALITY_ID, NAMESPACE, true))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND)
			.hasFieldOrPropertyWithValue("message", "Not Found: Message with id %s not found".formatted(messageId));

		verify(messageRepositoryMock).findByMessageIDAndMunicipalityIdAndNamespace(messageId, MUNICIPALITY_ID, NAMESPACE);
		verify(messageRepositoryMock, never()).save(any());
		verifyNoInteractions(messageMock);
	}

}
