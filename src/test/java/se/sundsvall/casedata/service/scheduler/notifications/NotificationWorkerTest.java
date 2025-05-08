package se.sundsvall.casedata.service.scheduler.notifications;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import se.sundsvall.casedata.integration.db.NotificationRepository;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class NotificationWorkerTest {

	@Mock
	private NotificationRepository notificationRepositoryMock;

	@InjectMocks
	private NotificationWorker notificationWorker;

	@Test
	void cleanUpNotifications() {

		// Act
		notificationWorker.cleanUpNotifications();

		// Assert
		verify(notificationRepositoryMock).deleteByExpiresBefore(any());
	}
}
