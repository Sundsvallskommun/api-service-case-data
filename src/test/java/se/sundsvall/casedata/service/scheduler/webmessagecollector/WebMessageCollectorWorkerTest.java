package se.sundsvall.casedata.service.scheduler.webmessagecollector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.ADMINISTRATOR;
import static se.sundsvall.casedata.integration.db.model.enums.Direction.INBOUND;

import generated.se.sundsvall.webmessagecollector.MessageDTO;
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
import se.sundsvall.casedata.api.model.Notification;
import se.sundsvall.casedata.integration.db.AttachmentRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.MessageAttachmentRepository;
import se.sundsvall.casedata.integration.db.MessageRepository;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.MessageAttachmentDataEntity;
import se.sundsvall.casedata.integration.db.model.MessageAttachmentEntity;
import se.sundsvall.casedata.integration.db.model.MessageEntity;
import se.sundsvall.casedata.integration.db.model.StakeholderEntity;
import se.sundsvall.casedata.integration.webmessagecollector.WebMessageCollectorClient;
import se.sundsvall.casedata.integration.webmessagecollector.configuration.WebMessageCollectorProperties;
import se.sundsvall.casedata.service.NotificationService;
import se.sundsvall.casedata.service.scheduler.MessageMapper;

@ExtendWith(MockitoExtension.class)
class WebMessageCollectorWorkerTest {

	@Mock
	AttachmentRepository attachmentRepositoryMock;

	@Mock
	private WebMessageCollectorProperties webMessageCollectorProperties;

	@Mock
	private NotificationService notificationServiceMock;

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
	private WebMessageCollectorWorker webMessageCollectorWorker;

	@Captor
	private ArgumentCaptor<MessageEntity> messageCaptor;

	@Captor
	private ArgumentCaptor<MessageAttachmentEntity> messageAttachmentCaptor;

	@Captor
	private ArgumentCaptor<AttachmentEntity> attachmentCaptor;

	@Captor
	private ArgumentCaptor<Notification> notificationCaptor;

	private static void assertSavedMessageHasCorrectValues(final MessageEntity message) {
		assertThat(message.getDirection()).isEqualTo(INBOUND);
		assertThat(message.getFamilyId()).isEqualTo("1");
		assertThat(message.getExternalCaseId()).isEqualTo("1");
		assertThat(message.getTextmessage()).isEqualTo("message");
		assertThat(message.getMessageId()).isEqualTo("1");
		assertThat(message.getSent()).isEqualTo("2021-01-01T00:00:00.000Z");
		assertThat(message.getUsername()).isEqualTo("username");
		assertThat(message.getFirstName()).isEqualTo("firstName");
		assertThat(message.getLastName()).isEqualTo("lastName");
		assertThat(message.getEmail()).isEqualTo("email");
		assertThat(message.getMessageId()).isEqualTo("1");

		assertThat(message.getAttachments()).isNull();
	}

	@Test
	void getAndProcessMessages() throws SQLException {

		// Arrange
		final var familyId = "123";
		final var instance = "instance";
		final var externalCaseId = "someExternalCaseId";
		final var errandNumber = "someErrandNumber";
		final var errandId = 123L;
		final var messageDTOs = createMessages();
		final var message = createMessage();
		final var stakeholder = StakeholderEntity.builder()
			.withAdAccount("adminAdAccount")
			.withRoles(List.of(ADMINISTRATOR.name())).build();

		final var bytes = new byte[] { 1, 23, 45 };
		final var blob = new SerialBlob(bytes);
		final var attachmentData = MessageAttachmentDataEntity.builder().withFile(blob).build();

		when(webMessageCollectorClientMock.getMessages(MUNICIPALITY_ID, familyId, instance)).thenReturn(messageDTOs);

		when(errandRepositoryMock.findByExternalCaseId(externalCaseId)).thenReturn(
			Optional.ofNullable(ErrandEntity.builder().withId(errandId).withStakeholders(List.of(stakeholder)).withErrandNumber(errandNumber).withExternalCaseId(externalCaseId).withMunicipalityId(MUNICIPALITY_ID).withNamespace(NAMESPACE).build()));
		when(webMessageCollectorProperties.familyIds()).thenReturn(Map.of(MUNICIPALITY_ID, Map.of(instance, List.of(familyId))));
		when(messageMapperMock.toMessageEntity(errandNumber, messageDTOs.getFirst(), MUNICIPALITY_ID, NAMESPACE)).thenReturn(message);
		when(messageRepositoryMock.saveAndFlush(any(MessageEntity.class))).thenReturn(message);

		when(messageMapperMock.toAttachmentEntity(any(generated.se.sundsvall.webmessagecollector.MessageAttachment.class), any(String.class), any(String.class), any(String.class))).thenReturn(createAttachment());

		when(webMessageCollectorClientMock.getAttachment(any(String.class), anyInt())).thenReturn(bytes);
		when(messageMapperMock.toMessageAttachmentData(any())).thenReturn(attachmentData);

		when(messageMapperMock.toAttachmentEntity(any(MessageAttachmentEntity.class))).thenReturn(AttachmentEntity.builder().withName("fileName").build());

		// Act
		webMessageCollectorWorker.getAndProcessMessages();

		// Assert
		verify(notificationServiceMock).createNotification(eq(MUNICIPALITY_ID), eq(NAMESPACE), notificationCaptor.capture());
		assertThat(notificationCaptor.getValue()).satisfies(notification -> {
			assertThat(notification.getErrandId()).isEqualTo(errandId);
			assertThat(notification.getType()).isEqualTo("UPDATE");
			assertThat(notification.getDescription()).isEqualTo("Meddelande mottaget");
			assertThat(notification.getOwnerId()).isEqualTo("adminAdAccount");
		});

		verify(webMessageCollectorClientMock).getMessages(MUNICIPALITY_ID, familyId, instance);
		verify(webMessageCollectorClientMock).deleteMessages(any(), any());
		verify(messageRepositoryMock).saveAndFlush(messageCaptor.capture());
		assertThat(messageCaptor.getValue()).satisfies(WebMessageCollectorWorkerTest::assertSavedMessageHasCorrectValues);

		verify(webMessageCollectorClientMock).getAttachment(MUNICIPALITY_ID, 1);
		verify(messageMapperMock).toMessageEntity(errandNumber, messageDTOs.getFirst(), MUNICIPALITY_ID, NAMESPACE);

		verify(messageAttachmentRepositoryMock).saveAndFlush(messageAttachmentCaptor.capture());
		assertThat(messageAttachmentCaptor.getValue()).satisfies(attachment -> {
			assertThat(attachment.getAttachmentId()).isEqualTo("1");
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
		when(webMessageCollectorClientMock.getMessages(MUNICIPALITY_ID, familyId, instance)).thenReturn(createMessages());
		when(webMessageCollectorProperties.familyIds()).thenReturn(Map.of(MUNICIPALITY_ID, Map.of(instance, List.of(familyId))));

		// Act
		webMessageCollectorWorker.getAndProcessMessages();

		// Assert
		verify(webMessageCollectorClientMock).getMessages(MUNICIPALITY_ID, familyId, instance);
		verify(webMessageCollectorClientMock).deleteMessages(any(), any());
		verify(messageRepositoryMock, never()).saveAndFlush(any());
		verify(messageMapperMock, never()).toMessageEntity(any(), any(), any());
	}

	private MessageEntity createMessage() {
		return MessageEntity.builder()
			.withErrandNumber("someErrandNumber")
			.withDirection(INBOUND)
			.withMunicipalityId(MUNICIPALITY_ID)
			.withNamespace(NAMESPACE)
			.withFamilyId("1")
			.withExternalCaseId("1")
			.withTextmessage("message")
			.withMessageId("1")
			.withSent("2021-01-01T00:00:00.000Z")
			.withUsername("username")
			.withFirstName("firstName")
			.withLastName("lastName")
			.withEmail("email")
			.withMessageId("1")
			.build();
	}

	private MessageAttachmentEntity createAttachment() {
		return MessageAttachmentEntity.builder()
			.withNamespace(NAMESPACE)
			.withMunicipalityId(MUNICIPALITY_ID)
			.withAttachmentId("1")
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
