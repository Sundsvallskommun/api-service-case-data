package se.sundsvall.casedata.service.scheduler.emailreader;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import generated.se.sundsvall.emailreader.Email;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmailReaderSchedulerTest {

	@Mock
	private EmailReaderWorker emailReaderWorkerMock;

	@InjectMocks
	private EmailReaderScheduler emailReaderScheduler;

	@Test
	void getAndProcessEmails_success() {
		// Arrange
		final var email = new Email(); // Assuming Email is a class used in EmailReaderWorker
		when(emailReaderWorkerMock.getAndProcessEmails()).thenReturn(Collections.singletonList(email));
		when(emailReaderWorkerMock.save(email)).thenReturn(true);

		// Act
		emailReaderScheduler.getAndProcessEmails();

		// Assert
		verify(emailReaderWorkerMock).getAndProcessEmails();
		verify(emailReaderWorkerMock).save(email);
		verify(emailReaderWorkerMock).deleteMail(email);
		verifyNoMoreInteractions(emailReaderWorkerMock);
	}

	@Test
	void getAndProcessEmails_saveFails() {
		// Arrange
		final var email = new Email(); // Assuming Email is a class used in EmailReaderWorker
		when(emailReaderWorkerMock.getAndProcessEmails()).thenReturn(Collections.singletonList(email));
		when(emailReaderWorkerMock.save(email)).thenReturn(false);

		// Act
		emailReaderScheduler.getAndProcessEmails();

		// Assert
		verify(emailReaderWorkerMock).getAndProcessEmails();
		verify(emailReaderWorkerMock).save(email);
		verify(emailReaderWorkerMock, never()).deleteMail(email);
		verifyNoMoreInteractions(emailReaderWorkerMock);
	}

	@Test
	void getAndProcessEmails_noEmails() {
		// Arrange
		when(emailReaderWorkerMock.getAndProcessEmails()).thenReturn(Collections.emptyList());

		// Act
		emailReaderScheduler.getAndProcessEmails();

		// Assert
		verify(emailReaderWorkerMock).getAndProcessEmails();
		verifyNoMoreInteractions(emailReaderWorkerMock);
	}
}
