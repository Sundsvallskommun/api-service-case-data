package se.sundsvall.casedata.service.scheduler.emailreader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import generated.se.sundsvall.emailreader.Email;
import generated.se.sundsvall.emailreader.EmailAttachment;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
import se.sundsvall.casedata.integration.emailreader.EmailReaderClient;
import se.sundsvall.casedata.integration.emailreader.configuration.EmailReaderProperties;
import se.sundsvall.casedata.service.NotificationService;
import se.sundsvall.casedata.service.scheduler.MessageMapper;
import se.sundsvall.dept44.scheduling.health.Dept44HealthUtility;

@ExtendWith(MockitoExtension.class)
class EmailReaderWorkerTest {

	@Mock
	private MessageRepository messageRepositoryMock;

	@Mock
	private ErrandRepository errandRepositoryMock;

	@Mock
	private AttachmentRepository attachmentRepositoryMock;

	@Mock
	private EmailReaderClient emailReaderClientMock;

	@Mock
	private EmailReaderProperties emailReaderPropertiesMock;

	@Mock
	private MessageMapper messageMapperMock;

	@Mock
	private MessageAttachmentRepository messageAttachmentRepositoryMock;

	@Mock
	private Dept44HealthUtility dept44HealthUtilityMock;

	@Mock
	private NotificationService notificationServiceMock;

	@InjectMocks
	private EmailReaderWorker emailReaderWorker;

	@Captor
	private ArgumentCaptor<Notification> notificationCaptor;

	@Test
	void getEmails() {
		// Arrange
		final var email = new Email()
			.id("someId")
			.subject("Ärende #PRH-2022-01 Ansökan om bygglov för fastighet KATARINA 4")
			.recipients(List.of("someRecipient"))
			.sender("someSender")
			.message("someMessage")
			.receivedAt(OffsetDateTime.now())
			.attachments(List.of(new EmailAttachment()
				.name("someName")
				.contentType("someContentType")));

		when(emailReaderPropertiesMock.municipalityId()).thenReturn("someMunicipalityId");
		when(emailReaderPropertiesMock.namespace()).thenReturn("someNamespace");
		when(emailReaderClientMock.getEmail(any(String.class), any(String.class)))
			.thenReturn(List.of(email));

		// Act
		final var emails = emailReaderWorker.getEmails();

		// Assert
		assertThat(emails).containsExactly(email);
		verify(emailReaderClientMock).getEmail(any(String.class), any(String.class));
	}

	@Test
	void getEmails_emptyList() {
		// Arrange
		when(emailReaderPropertiesMock.municipalityId()).thenReturn("someMunicipalityId");
		when(emailReaderPropertiesMock.namespace()).thenReturn("someNamespace");
		when(emailReaderClientMock.getEmail(any(String.class), any(String.class)))
			.thenReturn(Collections.emptyList());

		// Act
		final var emails = emailReaderWorker.getEmails();

		// Assert
		assertThat(emails).isEmpty();
		verify(emailReaderClientMock).getEmail(any(String.class), any(String.class));
	}

	@Test
	void save() {
		// Arrange
		final var messageId = 12L;
		final var emailAttachment = new EmailAttachment()
			.name("someName")
			.contentType("someContentType");

		final var email = new Email()
			.id(String.valueOf(messageId))
			.subject("Ärende #PRH-2022-01 Ansökan om bygglov för fastighet KATARINA 4")
			.recipients(List.of("someRecipient"))
			.sender("someSender")
			.message("someMessage")
			.receivedAt(OffsetDateTime.now())
			.attachments(List.of(emailAttachment));

		final var errandNumber = "PRH-2022-01";
		final var errandId = 123L;
		final var municipalityId = "someMunicipalityId";
		final var namespace = "someNamespace";
		final var stakeholder = StakeholderEntity.builder()
			.withAdAccount("adminAdAccount")
			.withRoles(List.of("ADMINISTRATOR")).build();

		final var messageAttachmentEntity = MessageAttachmentEntity.builder().withAttachmentData(MessageAttachmentDataEntity.builder().build()).build();
		final var attachmentEntity = AttachmentEntity.builder().build();

		when(errandRepositoryMock.findByErrandNumber(errandNumber))
			.thenReturn(Optional.of(ErrandEntity.builder()
				.withId(errandId)
				.withStakeholders(List.of(stakeholder))
				.withNamespace(namespace)
				.withMunicipalityId(municipalityId)
				.build()));
		when(messageRepositoryMock.existsById(email.getId())).thenReturn(false);
		when(messageMapperMock.toMessage(email, municipalityId, namespace, errandId)).thenReturn(MessageEntity.builder().build());

		when(messageMapperMock.toAttachmentEntity(any(EmailAttachment.class), any(), any(), any())).thenReturn(messageAttachmentEntity);
		when(messageMapperMock.toAttachmentEntity(any())).thenReturn(attachmentEntity);
		when(messageMapperMock.toMessageAttachmentData(any())).thenReturn(MessageAttachmentDataEntity.builder().build());
		// Act
		final var result = emailReaderWorker.save(email);

		// Assert
		assertThat(result).isTrue();
		verify(errandRepositoryMock).findByErrandNumber(errandNumber);
		verify(messageRepositoryMock).existsById(email.getId());
		verify(messageRepositoryMock).save(any(MessageEntity.class));
		verify(notificationServiceMock).create(eq(municipalityId), eq(namespace), notificationCaptor.capture());
		verify(messageAttachmentRepositoryMock).save(any());
		verify(messageAttachmentRepositoryMock).saveAndFlush(any());
		verify(attachmentRepositoryMock).save(any());
		verify(attachmentRepositoryMock).saveAndFlush(any());
		verifyNoMoreInteractions(errandRepositoryMock, messageRepositoryMock, notificationServiceMock, attachmentRepositoryMock, messageAttachmentRepositoryMock, dept44HealthUtilityMock);
	}

	@Test
	void save_emailAlreadyExists() {
		// Arrange
		final var email = new Email()
			.id("someId")
			.subject("Ärende #PRH-2022-01 Ansökan om bygglov för fastighet KATARINA 4")
			.recipients(List.of("someRecipient"))
			.sender("someSender")
			.message("someMessage")
			.receivedAt(OffsetDateTime.now())
			.attachments(List.of(new EmailAttachment()
				.name("someName")
				.contentType("someContentType")));

		final var errandNumber = "PRH-2022-01";
		final var errandId = 123L;
		final var municipalityId = "someMunicipalityId";
		final var namespace = "someNamespace";
		final var stakeholder = StakeholderEntity.builder()
			.withAdAccount("adminAdAccount")
			.withRoles(List.of("ADMINISTRATOR")).build();

		when(errandRepositoryMock.findByErrandNumber(errandNumber))
			.thenReturn(Optional.of(ErrandEntity.builder()
				.withId(errandId)
				.withStakeholders(List.of(stakeholder))
				.withNamespace(namespace)
				.withMunicipalityId(municipalityId)
				.build()));
		when(messageRepositoryMock.existsById(email.getId())).thenReturn(true);

		// Act
		final var result = emailReaderWorker.save(email);

		// Assert
		assertThat(result).isTrue();
		verify(errandRepositoryMock).findByErrandNumber(errandNumber);
		verify(messageRepositoryMock).existsById(email.getId());
		verifyNoMoreInteractions(messageRepositoryMock, notificationServiceMock, attachmentRepositoryMock);
		verifyNoInteractions(dept44HealthUtilityMock);

	}

	@Test
	void save_errandNotFound() {
		// Arrange
		final var email = new Email()
			.id("someId")
			.subject("Ärende #PRH-2022-01 Ansökan om bygglov för fastighet KATARINA 4")
			.recipients(List.of("someRecipient"))
			.sender("someSender")
			.message("someMessage")
			.receivedAt(OffsetDateTime.now())
			.attachments(List.of(new EmailAttachment()
				.name("someName")
				.contentType("someContentType")));
		final var errandNumber = "PRH-2022-01";

		when(errandRepositoryMock.findByErrandNumber(errandNumber))
			.thenReturn(Optional.empty());

		// Act
		final var result = emailReaderWorker.save(email);

		// Assert
		assertThat(result).isTrue();
		verify(errandRepositoryMock).findByErrandNumber(errandNumber);
		verifyNoMoreInteractions(messageRepositoryMock, notificationServiceMock, attachmentRepositoryMock);
		verifyNoInteractions(dept44HealthUtilityMock);

	}

	@Test
	void save_exceptionThrown() {
		// Arrange
		final var email = new Email()
			.id("someId")
			.subject("Ärende #PRH-2022-01 Ansökan om bygglov för fastighet KATARINA 4")
			.recipients(List.of("someRecipient"))
			.sender("someSender")
			.message("someMessage")
			.receivedAt(OffsetDateTime.now())
			.attachments(List.of(new EmailAttachment()
				.name("someName")
				.contentType("someContentType")));

		final var errandNumber = "PRH-2022-01";

		when(errandRepositoryMock.findByErrandNumber(errandNumber))
			.thenThrow(new RuntimeException("Database error"));

		// Act
		final var result = emailReaderWorker.save(email);

		// Assert
		assertThat(result).isFalse();
		verify(errandRepositoryMock).findByErrandNumber(errandNumber);

		verify(dept44HealthUtilityMock).setHealthIndicatorUnhealthy(null, "Error when processing email");
		verifyNoMoreInteractions(dept44HealthUtilityMock);

	}

	@Test
	void deleteMail() {
		// Arrange
		final var email = new Email()
			.id("someId")
			.subject("Ärende #PRH-2022-01 Ansökan om bygglov för fastighet KATARINA 4")
			.recipients(List.of("someRecipient"))
			.sender("someSender")
			.message("someMessage")
			.receivedAt(OffsetDateTime.now())
			.attachments(List.of(new EmailAttachment()
				.name("someName")

				.contentType("someContentType")));

		when(emailReaderPropertiesMock.municipalityId()).thenReturn("someMunicipalityId");

		// Act
		emailReaderWorker.deleteMail(email);

		// Assert
		verify(emailReaderClientMock).deleteEmail("someMunicipalityId", "someId");
		verifyNoInteractions(dept44HealthUtilityMock);

	}

	@Test
	void deleteMail_exceptionThrown() {
		// Arrange
		final var email = new Email()
			.id("someId")
			.subject("Ärende #PRH-2022-01 Ansökan om bygglov för fastighet KATARINA 4")
			.recipients(List.of("someRecipient"))
			.sender("someSender")
			.message("someMessage")
			.receivedAt(OffsetDateTime.now())
			.attachments(List.of(new EmailAttachment()
				.name("someName")

				.contentType("someContentType")));

		when(emailReaderPropertiesMock.municipalityId()).thenReturn("someMunicipalityId");
		doThrow(new RuntimeException("Error when deleting email")).when(emailReaderClientMock).deleteEmail("someMunicipalityId", "someId");
		// Act
		emailReaderWorker.deleteMail(email);

		// Assert
		verify(dept44HealthUtilityMock).setHealthIndicatorUnhealthy(null, "Error when deleting email");
		verify(emailReaderClientMock).deleteEmail("someMunicipalityId", "someId");
		verifyNoMoreInteractions(dept44HealthUtilityMock, emailReaderClientMock);

	}

	@Test
	void processAttachment() {
		// Arrange
		final var attachment = new EmailAttachment();
		final var messageId = "someMessageId";
		final var errandId = 123L;
		final var municipalityId = "someMunicipalityId";
		final var namespace = "someNamespace";
		final var messageAttachment = MessageAttachmentEntity.builder().withAttachmentData(MessageAttachmentDataEntity.builder().build()).build();
		final var attachmentEntity = new AttachmentEntity().withErrandId(errandId);

		when(messageMapperMock.toAttachmentEntity(attachment, messageId, municipalityId, namespace)).thenReturn(messageAttachment);
		when(messageMapperMock.toAttachmentEntity(messageAttachment)).thenReturn(attachmentEntity);
		when(messageMapperMock.toMessageAttachmentData(any())).thenReturn(MessageAttachmentDataEntity.builder().build());

		// Act
		emailReaderWorker.processAttachment(attachment, messageId, errandId, municipalityId, namespace);

		// Assert
		verify(messageAttachmentRepositoryMock).save(messageAttachment);
		verify(attachmentRepositoryMock).save(attachmentEntity);
		verify(messageAttachmentRepositoryMock).saveAndFlush(messageAttachment);
		verify(attachmentRepositoryMock).saveAndFlush(attachmentEntity);
		verifyNoMoreInteractions(messageAttachmentRepositoryMock, attachmentRepositoryMock);
	}

	@Test
	void processAttachment_exceptionThrown() {
		// Arrange
		final var attachment = new EmailAttachment();
		final var messageId = "someMessageId";
		final var errandId = 123L;
		final var municipalityId = "someMunicipalityId";
		final var namespace = "someNamespace";

		when(messageMapperMock.toAttachmentEntity(attachment, messageId, municipalityId, namespace)).thenThrow(new RuntimeException("Error"));

		// Act
		emailReaderWorker.processAttachment(attachment, messageId, errandId, municipalityId, namespace);

		// Assert
		verify(dept44HealthUtilityMock).setHealthIndicatorUnhealthy(any(), any());
	}

	@Test
	void processAttachmentData() {
		// Arrange
		final var messageAttachment = MessageAttachmentEntity.builder().withAttachmentData(MessageAttachmentDataEntity.builder().build()).build();
		final var attachmentEntity = new AttachmentEntity();
		final var data = "someData".getBytes();

		when(emailReaderClientMock.getAttachment(any(), any())).thenReturn(data);
		when(messageMapperMock.toMessageAttachmentData(data)).thenReturn(MessageAttachmentDataEntity.builder().build());
		when(messageMapperMock.toContentString(data)).thenReturn("someContentString");

		// Act
		emailReaderWorker.processAttachmentData(messageAttachment, attachmentEntity);

		// Assert
		verify(messageAttachmentRepositoryMock).saveAndFlush(messageAttachment);
		verify(attachmentRepositoryMock).saveAndFlush(attachmentEntity);
		verifyNoMoreInteractions(messageAttachmentRepositoryMock, attachmentRepositoryMock);
	}

	@Test
	void processAttachmentData_exceptionThrown() {
		// Arrange
		final var messageAttachment = MessageAttachmentEntity.builder().build();
		final var attachmentEntity = new AttachmentEntity();

		when(emailReaderClientMock.getAttachment(any(), any())).thenThrow(new RuntimeException("Error"));

		// Act
		emailReaderWorker.processAttachmentData(messageAttachment, attachmentEntity);

		// Assert
		verify(dept44HealthUtilityMock).setHealthIndicatorUnhealthy(any(), any());
	}
}
