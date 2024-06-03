package se.sundsvall.casedata.service.scheduler.webmessagecollector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.integration.db.model.enums.Direction.INBOUND;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.rowset.serial.SerialBlob;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.casedata.integration.db.AttachmentRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.MessageAttachmentRepository;
import se.sundsvall.casedata.integration.db.MessageRepository;
import se.sundsvall.casedata.integration.db.model.Attachment;
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
	AttachmentRepository attachmentRepositoryMock;

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

	@Captor
	private ArgumentCaptor<Attachment> attachmentCaptor;

	private static void assertSavedMessageHasCorrectValues(final Message message) {
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

		assertThat(message.getAttachments()).isNull();
	}

	@Test
	void getAndProcessMessages() throws SQLException {

		// Arrange
		final var familyId = "123";
		final var instance = "instance";
		final var externalCaseId = "someExternalCaseId";
		final var errandNumber = "someErrandNumber";
		final var messageDTOs = createMessages();
		final var message = createMessage();

		final var bytes = new byte[]{1, 23, 45};
		final var blob = new SerialBlob(bytes);
		final var attachmentData = MessageAttachmentData.builder().withFile(blob).build();

		when(webMessageCollectorClientMock.getMessages(familyId, instance)).thenReturn(messageDTOs);

		when(errandRepositoryMock.findByExternalCaseId(externalCaseId)).thenReturn(Optional.ofNullable(Errand.builder().withErrandNumber(errandNumber).withExternalCaseId(externalCaseId).build()));
		when(webMessageCollectorProperties.familyIds()).thenReturn(Map.of(instance, List.of(familyId)));
		when(messageMapperMock.toMessageEntity(errandNumber, messageDTOs.getFirst())).thenReturn(message);
		when(messageRepositoryMock.saveAndFlush(any(Message.class))).thenReturn(message);

		when(messageMapperMock.toAttachmentEntity(any(generated.se.sundsvall.webmessagecollector.MessageAttachment.class), any(String.class))).thenReturn(createAttachment());

		when(webMessageCollectorClientMock.getAttachment(anyInt())).thenReturn(bytes);
		when(messageMapperMock.toMessageAttachmentData(any())).thenReturn(attachmentData);

		when(messageMapperMock.toAttachment(any(MessageAttachment.class))).thenReturn(Attachment.builder().withName("fileName").build());

		// Act
		webMessageCollectorService.getAndProcessMessages();

		// Assert
		verify(webMessageCollectorClientMock).getMessages(familyId, instance);
		verify(webMessageCollectorClientMock).deleteMessages(any());
		verify(messageRepositoryMock).saveAndFlush(messageCaptor.capture());
		assertThat(messageCaptor.getValue()).satisfies(WebMessageCollectorServiceTest::assertSavedMessageHasCorrectValues);

		verify(webMessageCollectorClientMock).getAttachment(1);
		verify(messageMapperMock).toMessageEntity(errandNumber, messageDTOs.getFirst());

		verify(messageAttachmentRepositoryMock).saveAndFlush(messageAttachmentCaptor.capture());
		assertThat(messageAttachmentCaptor.getValue()).satisfies(attachment -> {
			assertThat(attachment.getAttachmentID()).isEqualTo("1");
			assertThat(attachment.getContentType()).isEqualTo("mimeType");
			assertThat(attachment.getName()).isEqualTo("fileName");
			assertThat(attachment.getAttachmentData().getFile()).isEqualTo(blob);
		});

		verify(attachmentRepositoryMock).saveAndFlush(attachmentCaptor.capture());
		assertThat(attachmentCaptor.getValue().getErrandNumber()).isEqualTo(errandNumber);
		assertThat(attachmentCaptor.getValue().getName()).isEqualTo("fileName");
	}

	@Test
	void getAndProcessMessagesWhenNonMatchingErrandExists() {

		// Arrange
		final var familyId = "123";
		final var instance = "instance";
		when(webMessageCollectorClientMock.getMessages(familyId, instance)).thenReturn(createMessages());
		when(webMessageCollectorProperties.familyIds()).thenReturn(Map.of(instance, List.of(familyId)));

		// Act
		webMessageCollectorService.getAndProcessMessages();

		// Assert
		verify(webMessageCollectorClientMock).getMessages(familyId, instance);
		verify(webMessageCollectorClientMock).deleteMessages(any());
		verify(messageRepositoryMock, never()).saveAndFlush(any());
		verify(messageMapperMock, never()).toMessageEntity(any(), any());
	}


	private Message createMessage() {
		return Message.builder()
			.withErrandNumber("someErrandNumber")
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
			.build();
	}

	private MessageAttachment createAttachment() {
		return MessageAttachment.builder()
			.withAttachmentID("1")
			.withName("fileName")
			.withContentType("mimeType")
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
