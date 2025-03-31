package se.sundsvall.casedata.service;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.ADMINISTRATOR;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casedata.api.model.MessageRequest;
import se.sundsvall.casedata.api.model.Notification;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.MessageAttachmentRepository;
import se.sundsvall.casedata.integration.db.MessageRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.MessageAttachmentDataEntity;
import se.sundsvall.casedata.integration.db.model.MessageAttachmentEntity;
import se.sundsvall.casedata.integration.db.model.MessageEntity;
import se.sundsvall.casedata.integration.db.model.StakeholderEntity;
import se.sundsvall.casedata.service.scheduler.MessageMapper;
import se.sundsvall.casedata.service.util.BlobBuilder;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

	@Mock
	private ErrandRepository errandRepositoryMock;

	@Mock
	private NotificationService notificationServiceMock;

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

	@Captor
	private ArgumentCaptor<Notification> notificationCaptor;

	@Test
	void findMessages() {

		// Arrange
		final var errandId = new Random().nextLong(1, 100000);
		final var messages = List.of(MessageEntity.builder()
			.withAttachments(List.of(MessageAttachmentEntity.builder().build()))
			.build());
		when(messageRepositoryMock.findAllByErrandIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(messages);

		// Act
		messageService.findMessages(errandId, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		verify(messageRepositoryMock).findAllByErrandIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(messageMapperMock).toMessageResponses(any(), eq(true));
		verifyNoMoreInteractions(messageMapperMock);
		verifyNoMoreInteractions(messageRepositoryMock);
	}

	@Test
	void findExternalMessages() {

		// Arrange
		final var errandId = new Random().nextLong(1, 100000);
		final var messages = List.of(MessageEntity.builder()
			.withAttachments(List.of(MessageAttachmentEntity.builder().build()))
			.build());
		when(messageRepositoryMock.findAllByErrandIdAndMunicipalityIdAndNamespaceAndInternalFalse(errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(messages);

		// Act
		messageService.findExternalMessages(errandId, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		verify(messageRepositoryMock).findAllByErrandIdAndMunicipalityIdAndNamespaceAndInternalFalse(errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(messageMapperMock).toMessageResponses(any(), eq(false));
		verifyNoMoreInteractions(messageMapperMock);
		verifyNoMoreInteractions(messageRepositoryMock);
	}

	@Test
	void findMessageAttachmentAsStreamedResponse() throws Exception {

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
		messageService.findMessageAttachmentAsStreamedResponse(1L, attachmentId, MUNICIPALITY_ID, NAMESPACE, servletResponseMock);

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
	void findMessageAttachmentAsStreamedResponseThrowsException() throws Exception {

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
		final var exception = assertThrows(ThrowableProblem.class, () -> messageService.findMessageAttachmentAsStreamedResponse(1L, attachmentId, MUNICIPALITY_ID, NAMESPACE, servletResponseMock));

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
	void findMessageAttachmentAsStreamedResponseNotFound() {

		// Arrange
		final var attachmentId = "attachmentId";
		when(errandRepositoryMock.existsByIdAndMunicipalityIdAndNamespace(any(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(true);

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> messageService.findMessageAttachmentAsStreamedResponse(1L, attachmentId, MUNICIPALITY_ID, NAMESPACE, servletResponseMock));

		// Assert
		assertThat(exception.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(exception.getMessage()).isEqualTo("Not Found: MessageAttachment with id:'%s' not found in namespace:'%s' for municipality with id:'%s'".formatted(attachmentId, NAMESPACE, MUNICIPALITY_ID));
		verify(messageAttachmentRepositoryMock).findByAttachmentIdAndMunicipalityIdAndNamespace(attachmentId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoInteractions(messageAttachmentEntityMock, servletResponseMock);
	}

	@Test
	void create() {

		// Arrange
		final var errandId = 1L;
		final var request = MessageRequest.builder().build();
		final var stakeholder = StakeholderEntity.builder()
			.withAdAccount("adminAdAccount")
			.withRoles(List.of(ADMINISTRATOR.name())).build();
		final var errand = ErrandEntity.builder()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withNamespace(NAMESPACE)
			.withId(errandId)
			.withStakeholders(List.of(stakeholder))
			.build();

		when(messageRepositoryMock.save(any(MessageEntity.class))).thenReturn(MessageEntity.builder().build());
		when(messageMapperMock.toMessageEntity(request, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(MessageEntity.builder().build());
		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(any(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(errand));

		// Act
		messageService.create(errandId, request, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		verify(messageMapperMock).toMessageEntity(request, errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(messageMapperMock).toMessageResponse(any(MessageEntity.class), eq(true));
		verify(errandRepositoryMock).findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(notificationServiceMock).create(eq(MUNICIPALITY_ID), eq(NAMESPACE), notificationCaptor.capture(), same(errand));
		assertThat(notificationCaptor.getValue()).satisfies(notification -> {
			assertThat(notification.getErrandId()).isEqualTo(errandId);
			assertThat(notification.getType()).isEqualTo("UPDATE");
			assertThat(notification.getDescription()).isEqualTo("Meddelande skickat");
			assertThat(notification.getOwnerId()).isEqualTo("adminAdAccount");
		});
		verify(messageMapperMock).toMessageEntity(request, errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(messageRepositoryMock).save(any());
		verifyNoMoreInteractions(messageRepositoryMock, messageMapperMock);
	}

	@ParameterizedTest
	@ValueSource(booleans = {
		true, false
	})
	void updateViewedStatusOnExistingMessage(final boolean viewed) {

		// Arrange
		final var errandId = 1L;
		final var messageId = RandomStringUtils.secure().nextAlphabetic(10);
		when(errandRepositoryMock.existsByIdAndMunicipalityIdAndNamespace(any(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(true);
		when(messageRepositoryMock.findByMunicipalityIdAndNamespaceAndErrandIdAndMessageId(MUNICIPALITY_ID, NAMESPACE, errandId, messageId)).thenReturn(Optional.of(messageMock));

		// Act
		messageService.updateViewedStatus(1L, messageId, MUNICIPALITY_ID, NAMESPACE, viewed);

		// Assert
		verify(messageRepositoryMock).findByMunicipalityIdAndNamespaceAndErrandIdAndMessageId(MUNICIPALITY_ID, NAMESPACE, errandId, messageId);
		verify(messageMock).setViewed(viewed);
		verify(messageRepositoryMock).save(messageMock);
	}

	@Test
	void updateViewedStatusOnNonExistingMessage() {

		// Arrange
		final var errandId = 1L;
		final var messageId = RandomStringUtils.secure().nextAlphabetic(10);
		when(errandRepositoryMock.existsByIdAndMunicipalityIdAndNamespace(any(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(true);

		// Act & assert
		assertThatThrownBy(() -> messageService.updateViewedStatus(1L, messageId, MUNICIPALITY_ID, NAMESPACE, true))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND)
			.hasFieldOrPropertyWithValue("message", "Not Found: Message with id:'%s' not found in namespace:'%s' for municipality with id:'%s'".formatted(messageId, NAMESPACE, MUNICIPALITY_ID));

		verify(messageRepositoryMock).findByMunicipalityIdAndNamespaceAndErrandIdAndMessageId(MUNICIPALITY_ID, NAMESPACE, errandId, messageId);
		verify(messageRepositoryMock, never()).save(any());
		verifyNoInteractions(messageMock);
	}
}
