package se.sundsvall.casedata.service.scheduler.supensions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.casedata.api.model.Notification;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.StakeholderEntity;
import se.sundsvall.casedata.service.NotificationService;

@ExtendWith(MockitoExtension.class)
class SuspensionWorkerTest {

	@Mock
	private ErrandRepository errandsRepositoryMock;

	@Mock
	private NotificationService notificationServiceMock;

	@InjectMocks
	private SuspensionWorker suspensionWorker;

	@Captor
	private ArgumentCaptor<Notification> notificationCaptor;

	@Test
	void processExpiredSuspensionsHasExpired() {

		// Arrange
		final var namespace = "namespace";
		final var municipalityId = "municipalityId";
		final var description = "Parkering av ärendet har upphört";
		final var notificationType = "UPDATE";
		final var errandEntity = ErrandEntity.builder()
			.withNamespace(namespace)
			.withId(1L)
			.withMunicipalityId(municipalityId)
			.withSuspendedFrom(OffsetDateTime.now().minusDays(1))
			.withSuspendedTo(OffsetDateTime.now().minusHours(1))
			.withMunicipalityId(municipalityId)
			.withStakeholders(List.of(StakeholderEntity.builder()
				.withFirstName("firstName")
				.withRoles(List.of("ADMINISTRATOR"))
				.withLastName("lastName")
				.withAdAccount("adAccount")
				.build()))
			.build();

		when(errandsRepositoryMock.findAllBySuspendedToBefore(any(OffsetDateTime.class))).thenReturn(List.of(errandEntity));

		// Act
		suspensionWorker.processExpiredSuspensions();

		// Assert
		verify(errandsRepositoryMock).findAllBySuspendedToBefore(any(OffsetDateTime.class));
		verify(notificationServiceMock).createNotification(any(), any(), notificationCaptor.capture());

		final var notification = notificationCaptor.getValue();
		assertThat(notification).isNotNull();
		assertThat(notification.getErrandId()).isEqualTo(errandEntity.getId());
		assertThat(notification.getErrandNumber()).isEqualTo(errandEntity.getErrandNumber());
		assertThat(notification.getOwnerId()).isNotNull();
		assertThat(notification.getOwnerFullName()).isNotNull();
		assertThat(notification.getType()).isEqualTo(notificationType);
		assertThat(notification.getDescription()).isEqualTo(description);

		verifyNoMoreInteractions(errandsRepositoryMock);
	}

	@Test
	void processExpiredSuspensionsNoSuspensions() {

		// Arrange
		when(errandsRepositoryMock.findAllBySuspendedToBefore(any(OffsetDateTime.class))).thenReturn(List.of());

		// Act
		suspensionWorker.processExpiredSuspensions();

		// Assert
		verify(errandsRepositoryMock).findAllBySuspendedToBefore(any(OffsetDateTime.class));
		verifyNoMoreInteractions(errandsRepositoryMock, notificationServiceMock);
	}

}
