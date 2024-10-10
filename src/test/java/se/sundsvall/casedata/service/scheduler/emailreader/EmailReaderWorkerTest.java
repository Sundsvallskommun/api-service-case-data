package se.sundsvall.casedata.service.scheduler.emailreader;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.casedata.integration.db.AttachmentRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.MessageRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.MessageEntity;
import se.sundsvall.casedata.integration.emailreader.EmailReaderClient;
import se.sundsvall.casedata.integration.emailreader.configuration.EmailReaderProperties;

import generated.se.sundsvall.emailreader.Email;
import generated.se.sundsvall.emailreader.EmailAttachment;

@ExtendWith(MockitoExtension.class)
class EmailReaderWorkerTest {

	@Mock
	private EmailReaderProperties emailReaderPropertiesMock;

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

	@BeforeEach
	void setUp() {
		when(emailReaderPropertiesMock.municipalityId()).thenReturn("someMunicipalityId");
		when(emailReaderPropertiesMock.namespace()).thenReturn("someNamespace");
	}

	@Test
	void getAndProcessEmails() {

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

		when(emailReaderClientMock.getEmail(any(String.class), any(String.class)))
			.thenReturn(List.of(email));

		when(errandRepositoryMock.findByErrandNumber(any(String.class))).thenReturn(Optional.of(ErrandEntity.builder().build()));
		when(emailReaderMapperMock.toMessage(email, "someMunicipalityId")).thenReturn(messageMock);
		when(messageRepositoryMock.existsById("someId")).thenReturn(false);

		// Act
		emailReaderWorker.getAndProcessEmails();

		// Assert
		verify(emailReaderClientMock).getEmail(any(String.class), any(String.class));
		verify(errandRepositoryMock).findByErrandNumber("PRH-2022-01");
		verify(messageRepositoryMock).existsById("someId");
		verify(emailReaderMapperMock).toMessage(email, "someMunicipalityId");
		verify(emailReaderMapperMock).toAttachments(any());
		verify(messageRepositoryMock).save(any());
		verify(attachmentRepositoryMock).saveAll(any());
		verify(emailReaderClientMock).deleteEmail("someMunicipalityId", "someId");
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
