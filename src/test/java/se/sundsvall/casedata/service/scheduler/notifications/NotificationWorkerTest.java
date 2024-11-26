package se.sundsvall.casedata.service.scheduler.notifications;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import se.sundsvall.casedata.integration.db.NotificationRepository;
import se.sundsvall.casedata.integration.db.model.NotificationEntity;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class NotificationWorkerTest {

	@Mock
	private NotificationRepository notificationRepositoryMock;

	@InjectMocks
	private NotificationWorker notificationWorker;

	@Test
	void processExpiredNotifications() {

		// Arrange
		final var list = List.of(
			new NotificationEntity(),
			new NotificationEntity(),
			new NotificationEntity());

		when(notificationRepositoryMock.findByExpiresBefore(any())).thenReturn(list);

		// Act
		notificationWorker.processExpiredNotifications();

		// Assert
		verify(notificationRepositoryMock).deleteAllInBatch(list);
	}

	@Test
	void processExpiredNotificationsNoNotificationsFound() {

		// Arrange
		when(notificationRepositoryMock.findByExpiresBefore(any())).thenReturn(emptyList());

		// Act
		notificationWorker.processExpiredNotifications();

		// Assert
		verify(notificationRepositoryMock).findByExpiresBefore(any());
	}
}
