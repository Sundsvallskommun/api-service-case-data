package se.sundsvall.casedata.service.scheduler.emailreader;

import generated.se.sundsvall.emailreader.Email;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailReaderSchedulerTest {

	@Mock
	private EmailReaderWorker emailReaderWorkerMock;

	@InjectMocks
	private EmailReaderScheduler emailReaderScheduler;

	@Test
	void getAndProcessEmailsSuccess() {
		// Arrange
		final var email = new Email();
		when(emailReaderWorkerMock.getEmails()).thenReturn(Collections.singletonList(email));
		when(emailReaderWorkerMock.save(email)).thenReturn(true);

		// Act
		emailReaderScheduler.getAndProcessEmails();

		// Assert
		verify(emailReaderWorkerMock).getEmails();
		verify(emailReaderWorkerMock).save(email);
		verify(emailReaderWorkerMock).deleteMail(email);
		verifyNoMoreInteractions(emailReaderWorkerMock);
	}

	@Test
	void getAndProcessEmailsSaveFails() {
		// Arrange
		final var email = new Email();
		when(emailReaderWorkerMock.getEmails()).thenReturn(Collections.singletonList(email));
		when(emailReaderWorkerMock.save(email)).thenReturn(false);

		// Act
		emailReaderScheduler.getAndProcessEmails();

		// Assert
		verify(emailReaderWorkerMock).getEmails();
		verify(emailReaderWorkerMock).save(email);
		verify(emailReaderWorkerMock, never()).deleteMail(email);
		verifyNoMoreInteractions(emailReaderWorkerMock);
	}

	@Test
	void getAndProcessEmailsNoEmails() {
		// Arrange
		when(emailReaderWorkerMock.getEmails()).thenReturn(Collections.emptyList());

		// Act
		emailReaderScheduler.getAndProcessEmails();

		// Assert
		verify(emailReaderWorkerMock).getEmails();
		verifyNoMoreInteractions(emailReaderWorkerMock);
	}

	@Test
	void getAndProcessEmailsAutoReplyDeleted() {
		// Arrange
		final var email = new Email();
		email.setId("auto-reply-id");
		email.setHeaders(Map.of("AUTO_SUBMITTED", List.of("auto-replied")));
		when(emailReaderWorkerMock.getEmails()).thenReturn(Collections.singletonList(email));

		// Act
		emailReaderScheduler.getAndProcessEmails();

		// Assert
		verify(emailReaderWorkerMock).getEmails();
		verify(emailReaderWorkerMock).deleteMail(email);
		verify(emailReaderWorkerMock, never()).save(email);
		verifyNoMoreInteractions(emailReaderWorkerMock);
	}
}
