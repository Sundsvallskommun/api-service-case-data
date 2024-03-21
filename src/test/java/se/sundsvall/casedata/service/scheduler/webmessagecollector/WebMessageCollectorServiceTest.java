package se.sundsvall.casedata.service.scheduler.webmessagecollector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.integration.db.model.enums.Direction.INBOUND;

import java.io.ByteArrayInputStream;
import java.sql.SQLException;
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

import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.MessageAttachmentRepository;
import se.sundsvall.casedata.integration.db.MessageRepository;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.Message;
import se.sundsvall.casedata.integration.db.model.MessageAttachment;
import se.sundsvall.casedata.integration.db.model.MessageAttachmentData;
import se.sundsvall.casedata.integration.webmessagecollector.WebMessageCollectorClient;
import se.sundsvall.casedata.integration.webmessagecollector.configuration.WebMessageCollectorProperties;
import se.sundsvall.casedata.service.scheduler.MessageMapper;

import generated.se.sundsvall.webmessagecollector.MessageDTO;

@ExtendWith(MockitoExtension.class)
class WebMessageCollectorServiceTest {

	@Mock
	private WebMessageCollectorProperties webMessageCollectorProperties;

	@Mock
	private MessageAttachmentRepository messageAttachmentRepositoryMock;

	@Mock
	private MessageRepository messageRepositoryMock;

	@Mock
	private WebMessageCollectorClient webMessageCollectorClientMock;

	@Mock
	private ErrandRepository errandRepositoryMock;

	@Mock
	private MessageMapper messageMapperMock;

	@InjectMocks
	private WebMessageCollectorService webMessageCollectorService;

	@Captor
	private ArgumentCaptor<Message> messageCaptor;

	@Captor
	private ArgumentCaptor<MessageAttachment> messageAttachmentCaptor;

	@Test
	void getAndProcessMessages() throws SQLException {

		// Arrange
		final var externalCaseId = "someExternalCaseId";
		final var errandNumber = "someErrandNumber";
		final var messageDTOs = createMessages();
		final var messages = createMessage();

		final var stream = new ByteArrayInputStream(new byte[]{1, 23, 45});
		final var blob = new SerialBlob(stream.readAllBytes());
		final var attachmentData = MessageAttachmentData.builder().withFile(blob).build();

		when(webMessageCollectorClientMock.getMessages(any(String.class))).thenReturn(messageDTOs);

		when(errandRepositoryMock.findByExternalCaseId(externalCaseId)).thenReturn(Optional.ofNullable(Errand.builder().withErrandNumber(errandNumber).withExternalCaseId(externalCaseId).build()));
		when(webMessageCollectorProperties.familyIds()).thenReturn(List.of("123"));
		when(messageMapperMock.toMessageEntity(any(), any())).thenReturn(createMessage());
		when(messageRepositoryMock.save(any(Message.class))).thenReturn(messages);

		when(webMessageCollectorClientMock.getAttachment(anyInt())).thenReturn(stream);
		when(messageMapperMock.toMessageAttachmentData(any())).thenReturn(attachmentData);

		// Act
		webMessageCollectorService.getAndProcessMessages();

		// Assert
		verify(webMessageCollectorClientMock).getMessages(any(String.class));
		verify(webMessageCollectorClientMock).deleteMessages(any());
		verify(messageRepositoryMock).save(messageCaptor.capture());
		assertThat(messageCaptor.getValue()).satisfies(message -> {
			assertThat(message.getDirection()).isEqualTo(INBOUND);
			assertThat(message.getFamilyID()).isEqualTo("1");
			assertThat(message.getExternalCaseID()).isEqualTo("1");
			assertThat(message.getTextmessage()).isEqualTo("message");
			assertThat(message.getMessageID()).isEqualTo("1");
			assertThat(message.getSent()).isEqualTo("2021-01-01T00:00:00.000Z");
			assertThat(message.getUsername()).isEqualTo("username");
			assertThat(message.getFirstName()).isEqualTo("firstName");
			assertThat(message.getLastName()).isEqualTo("lastName");
			assertThat(message.getEmail()).isEqualTo("email");
			assertThat(message.getMessageID()).isEqualTo("1");

			assertThat(message.getAttachments()).hasSize(1);
			assertThat(message.getAttachments().getFirst().getAttachmentID()).isEqualTo("1");
			assertThat(message.getAttachments().getFirst().getContentType()).isEqualTo("mimeType");
			assertThat(message.getAttachments().getFirst().getName()).isEqualTo("fileName");
		});

		verify(webMessageCollectorClientMock).getAttachment(1);
		verify(messageMapperMock).toMessageEntity(errandNumber, messageDTOs.getFirst());

		verify(messageAttachmentRepositoryMock).save(messageAttachmentCaptor.capture());
		assertThat(messageAttachmentCaptor.getValue()).satisfies(attachment -> {
			assertThat(attachment.getAttachmentID()).isEqualTo("1");
			assertThat(attachment.getContentType()).isEqualTo("mimeType");
			assertThat(attachment.getName()).isEqualTo("fileName");
			assertThat(attachment.getAttachmentData().getFile()).isEqualTo(blob);
		});
	}

	@Test
	void getAndProcessMessagesWhenNonMatchingErrandExists() {
		when(webMessageCollectorClientMock.getMessages(any(String.class))).thenReturn(createMessages());
		when(webMessageCollectorProperties.familyIds()).thenReturn(List.of("123"));

		webMessageCollectorService.getAndProcessMessages();

		verify(webMessageCollectorClientMock).getMessages(any(String.class));
		verify(webMessageCollectorClientMock).deleteMessages(any());
		verify(messageRepositoryMock, never()).save(any());
		verify(messageMapperMock, never()).toMessageEntity(any(), any());
	}


	private Message createMessage() {
		return Message.builder()
			.withDirection(INBOUND)
			.withFamilyID("1")
			.withExternalCaseID("1")
			.withTextmessage("message")
			.withMessageID("1")
			.withSent("2021-01-01T00:00:00.000Z")
			.withUsername("username")
			.withFirstName("firstName")
			.withLastName("lastName")
			.withEmail("email")
			.withMessageID("1")
			.withAttachments(List.of(MessageAttachment
				.builder()
				.withAttachmentID("1")
				.withContentType("mimeType")
				.withName("fileName")
				.build()))
			.build();
	}

	private List<MessageDTO> createMessages() {
		return List.of(new MessageDTO()
			.direction(MessageDTO.DirectionEnum.INBOUND)
			.familyId("1")
			.externalCaseId("someExternalCaseId")
			.message("message")
			.messageId("1")
			.sent("2021-01-01T00:00:00.000Z")
			.username("username")
			.firstName("firstName")
			.lastName("lastName")
			.email("email")
			.id(1)
			.attachments(List.of(new generated.se.sundsvall.webmessagecollector.MessageAttachment()
				.attachmentId(1)
				.name("fileName")
				.mimeType("mimeType")
				.extension("extension")))
			.userId("1"));
	}

}
