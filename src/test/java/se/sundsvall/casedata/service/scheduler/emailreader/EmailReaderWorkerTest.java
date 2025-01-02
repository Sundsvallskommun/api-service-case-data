package se.sundsvall.casedata.service.scheduler.emailreader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
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
import se.sundsvall.casedata.integration.db.MessageRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.MessageEntity;
import se.sundsvall.casedata.integration.db.model.StakeholderEntity;
import se.sundsvall.casedata.integration.emailreader.EmailReaderClient;
import se.sundsvall.casedata.integration.emailreader.configuration.EmailReaderProperties;
import se.sundsvall.casedata.service.NotificationService;

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
	private EmailReaderMapper emailReaderMapperMock;

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
				.content("someContent")
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
		final var email = new Email()
			.id("someId")
			.subject("Ärende #PRH-2022-01 Ansökan om bygglov för fastighet KATARINA 4")
			.recipients(List.of("someRecipient"))
			.sender("someSender")
			.message("someMessage")
			.receivedAt(OffsetDateTime.now())
			.attachments(List.of(new EmailAttachment()
				.name("someName")
				.content("someContent")
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
		when(messageRepositoryMock.existsById(email.getId())).thenReturn(false);
		when(emailReaderMapperMock.toMessage(email, municipalityId, namespace)).thenReturn(MessageEntity.builder().build());

		// Act
		final var result = emailReaderWorker.save(email);

		// Assert
		assertThat(result).isTrue();
		verify(errandRepositoryMock).findByErrandNumber(errandNumber);
		verify(messageRepositoryMock).existsById(email.getId());
		verify(messageRepositoryMock).save(any(MessageEntity.class));
		verify(notificationServiceMock).createNotification(eq(municipalityId), eq(namespace), notificationCaptor.capture());
		verify(attachmentRepositoryMock).saveAll(any());
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
				.content("someContent")
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
				.content("someContent")
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
				.content("someContent")
				.contentType("someContentType")));

		final var errandNumber = "PRH-2022-01";

		when(errandRepositoryMock.findByErrandNumber(errandNumber))
			.thenThrow(new RuntimeException("Database error"));

		// Act
		final var result = emailReaderWorker.save(email);

		// Assert
		assertThat(result).isFalse();
		verify(errandRepositoryMock).findByErrandNumber(errandNumber);
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
				.content("someContent")
				.contentType("someContentType")));

		when(emailReaderPropertiesMock.municipalityId()).thenReturn("someMunicipalityId");

		// Act
		emailReaderWorker.deleteMail(email);

		// Assert
		verify(emailReaderClientMock).deleteEmail("someMunicipalityId", "someId");
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
				.content("someContent")
				.contentType("someContentType")));

		when(emailReaderPropertiesMock.municipalityId()).thenReturn("someMunicipalityId");
		doThrow(new RuntimeException("Error when deleting email")).when(emailReaderClientMock).deleteEmail("someMunicipalityId", "someId");
		// Act
		emailReaderWorker.deleteMail(email);

		// Assert
		verify(emailReaderClientMock).deleteEmail("someMunicipalityId", "someId");
	}
}
