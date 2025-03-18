package se.sundsvall.casedata.service.scheduler.notifications;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.service.util.Constants.UNKNOWN;

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
	void cleanUpNotifications() {

		// Arrange
		final var entity1 = NotificationEntity.builder()
			.withOwnerId(null)
			.withAcknowledged(true)
			.withGlobalAcknowledged(true) // Will be deleted: YES
			.build();

		final var entity2 = NotificationEntity.builder()
			.withOwnerId(null)
			.withAcknowledged(false)
			.withGlobalAcknowledged(true) // Will be deleted: YES
			.build();

		final var entity3 = NotificationEntity.builder()
			.withOwnerId(UNKNOWN)
			.withAcknowledged(false)
			.withGlobalAcknowledged(true) // Will be deleted: YES
			.build();

		final var entity4 = NotificationEntity.builder()
			.withOwnerId("user123")
			.withAcknowledged(false)
			.withGlobalAcknowledged(true) // Will be deleted: NO
			.build();

		final var entity5 = NotificationEntity.builder()
			.withOwnerId("user123")
			.withAcknowledged(false)
			.withGlobalAcknowledged(true) // Will be deleted: NO
			.build();

		when(notificationRepositoryMock.findByExpiresBefore(any())).thenReturn(List.of(entity1, entity2, entity3, entity4, entity5));

		// Act
		notificationWorker.cleanUpNotifications();

		// Assert
		verify(notificationRepositoryMock).findByExpiresBefore(any());
		verify(notificationRepositoryMock).deleteAllInBatch(List.of(entity1, entity2, entity3));
	}

	@Test
	void cleanUpNotificationsNoNotifications() {

		// Arrange
		when(notificationRepositoryMock.findByExpiresBefore(any())).thenReturn(emptyList());

		// Act
		notificationWorker.cleanUpNotifications();

		// Assert
		verify(notificationRepositoryMock).findByExpiresBefore(any());
		verify(notificationRepositoryMock).deleteAllInBatch(emptyList());
	}
}
