package se.sundsvall.casedata.service.scheduler.emailreader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.ADMINISTRATOR;

import generated.se.sundsvall.emailreader.Email;
import generated.se.sundsvall.emailreader.EmailAttachment;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
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
	private EmailReaderProperties emailReaderPropertiesMock;

	@Mock
	private NotificationService notificationServiceMock;

	@Mock
	private MessageRepository messageRepositoryMock;

	@Mock
	private ErrandRepository errandRepositoryMock;

	@Mock
	private AttachmentRepository attachmentRepositoryMock;

	@Mock
	private EmailReaderClient emailReaderClientMock;

	@Mock
	private EmailReaderMapper emailReaderMapperMock;

	@Mock
	private MessageEntity messageMock;

	@InjectMocks
	private EmailReaderWorker emailReaderWorker;

	@Captor
	private ArgumentCaptor<Notification> notificationCaptor;

	@BeforeEach
	void setUp() {
		when(emailReaderPropertiesMock.municipalityId()).thenReturn("someMunicipalityId");
		when(emailReaderPropertiesMock.namespace()).thenReturn("someNamespace");
	}

	@Test
	void getAndProcessEmails() {

		// Arrange
		final var municipalityId = "someMunicipalityId";
		final var namespace = "someNamespace";
		final var errandId = 123L;
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

		final var stakeholder = StakeholderEntity.builder()
			.withAdAccount("adminAdAccount")
			.withRoles(List.of(ADMINISTRATOR.name())).build();

		when(emailReaderClientMock.getEmail(any(String.class), any(String.class)))
			.thenReturn(List.of(email));

		when(errandRepositoryMock.findByErrandNumber(any(String.class))).thenReturn(Optional.of(ErrandEntity.builder().withId(errandId).withStakeholders(List.of(stakeholder)).withNamespace(namespace).withMunicipalityId(municipalityId).build()));

		when(emailReaderMapperMock.toMessage(email, municipalityId, namespace)).thenReturn(messageMock);
		when(messageRepositoryMock.existsById("someId")).thenReturn(false);

		// Act
		emailReaderWorker.getAndProcessEmails();

		// Assert

		verify(notificationServiceMock).create(eq(municipalityId), eq(namespace), notificationCaptor.capture());
		assertThat(notificationCaptor.getValue()).satisfies(notification -> {
			assertThat(notification.getErrandId()).isEqualTo(errandId);
			assertThat(notification.getType()).isEqualTo("UPDATE");
			assertThat(notification.getDescription()).isEqualTo("Meddelande mottaget");
			assertThat(notification.getOwnerId()).isEqualTo("adminAdAccount");
		});

		verify(emailReaderClientMock).getEmail(any(String.class), any(String.class));
		verify(errandRepositoryMock).findByErrandNumber("PRH-2022-01");
		verify(messageRepositoryMock).existsById("someId");
		verify(emailReaderMapperMock).toMessage(email, municipalityId, namespace);
		verify(emailReaderMapperMock).toAttachments(any(), eq(municipalityId), eq(namespace));
		verify(messageRepositoryMock).save(any());
		verify(attachmentRepositoryMock).saveAll(any());
		verify(emailReaderClientMock).deleteEmail(municipalityId, "someId");
		verifyNoMoreInteractions(emailReaderClientMock, messageRepositoryMock, attachmentRepositoryMock);
	}

	@Test
	void getAndProcessEmailsSkipSave() {

		when(emailReaderClientMock.getEmail(any(String.class), any(String.class)))
			.thenReturn(List.of(new Email()
				.id("someId")
				.subject("Ärende #PRH-2022-01 Ansökan om bygglov för fastighet KATARINA 4")
				.recipients(List.of("someRecipient"))
				.sender("someSender")
				.message("someMessage")
				.receivedAt(OffsetDateTime.now())
				.attachments(List.of(new EmailAttachment()
					.name("someName")
					.content("someContent")
					.contentType("someContentType")))));

		when(errandRepositoryMock.findByErrandNumber(any(String.class))).thenReturn(Optional.of(ErrandEntity.builder().build()));
		when(messageRepositoryMock.existsById("someId")).thenReturn(true);

		emailReaderWorker.getAndProcessEmails();

		verify(emailReaderClientMock).getEmail(any(String.class), any(String.class));
		verify(errandRepositoryMock).findByErrandNumber("PRH-2022-01");
		verify(messageRepositoryMock).existsById("someId");
		verify(emailReaderClientMock).deleteEmail("someMunicipalityId", "someId");
		verifyNoInteractions(emailReaderMapperMock, attachmentRepositoryMock);
		verifyNoMoreInteractions(emailReaderClientMock, messageRepositoryMock);
	}

	@Test
	void getAndProcessEmailsFaultySubject() {

		when(emailReaderClientMock.getEmail(any(String.class), any(String.class)))
			.thenReturn(List.of(new Email()
				.id("someId")
				.subject("im a faulty subject line")
				.recipients(List.of("someRecipient"))
				.sender("someSender")
				.message("someMessage")
				.receivedAt(OffsetDateTime.now())
				.attachments(List.of(new EmailAttachment()
					.name("someName")
					.content("someContent")
					.contentType("someContentType")))));

		emailReaderWorker.getAndProcessEmails();

		verify(emailReaderClientMock).getEmail(any(String.class), any(String.class));
		verify(emailReaderClientMock).deleteEmail("someMunicipalityId", "someId");
		verifyNoMoreInteractions(emailReaderClientMock);
		verifyNoInteractions(messageRepositoryMock, attachmentRepositoryMock);
	}

	@Test
	void getAndProcessEmailsNoEmailsFound() {

		when(emailReaderClientMock.getEmail(any(String.class), any(String.class)))
			.thenReturn(null);

		emailReaderWorker.getAndProcessEmails();

		verify(emailReaderClientMock).getEmail(any(String.class), any(String.class));
		verifyNoMoreInteractions(emailReaderClientMock);
		verifyNoInteractions(messageRepositoryMock, attachmentRepositoryMock);

	}

	@Test
	void getAndProcessEmailsClientThrowsException() {

		when(emailReaderClientMock.getEmail(any(String.class), any(String.class)))
			.thenThrow(new RuntimeException("some exception"));

		emailReaderWorker.getAndProcessEmails();
		verify(emailReaderClientMock).getEmail(any(String.class), any(String.class));
		verifyNoMoreInteractions(emailReaderClientMock);
		verifyNoInteractions(messageRepositoryMock, attachmentRepositoryMock);
	}

}
