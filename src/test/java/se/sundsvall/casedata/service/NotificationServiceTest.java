package se.sundsvall.casedata.service;

import static java.util.Optional.empty;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.TestUtil.createNotificationEntity;
import static se.sundsvall.casedata.TestUtil.createPatchNotification;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Problem;

import generated.se.sundsvall.employee.PortalPersonData;
import se.sundsvall.casedata.TestUtil;
import se.sundsvall.casedata.api.filter.IncomingRequestFilter;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.NotificationRepository;
import se.sundsvall.casedata.integration.db.model.NotificationEntity;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

	@Mock
	private EmployeeService employeeServiceMock;

	@Mock
	private ErrandRepository errandRepositoryMock;

	@Mock
	private IncomingRequestFilter incomingRequestFilterMock;

	@Mock
	private NotificationRepository notificationRepositoryMock;

	@InjectMocks
	private NotificationService notificationService;

	@Captor
	private ArgumentCaptor<NotificationEntity> notificationEntityArgumentCaptor;

	@Test
	void getNotification() {

		// Arrange
		final var municipalityId = "2281";
		final var namespace = "namespace";
		final var notificationId = randomUUID().toString();
		final var notificationEntity = createNotificationEntity(n -> {});

		when(notificationRepositoryMock.findByIdAndNamespaceAndMunicipalityId(notificationId, namespace, municipalityId)).thenReturn(Optional.of(notificationEntity));

		// Act
		final var result = notificationService.getNotification(municipalityId, namespace, notificationId);

		// Assert
		assertThat(result).isNotNull();
		verify(notificationRepositoryMock).findByIdAndNamespaceAndMunicipalityId(notificationId, namespace, municipalityId);
	}

	@Test
	void getNotificationNotFound() {

		// Arrange
		final var municipalityId = "2281";
		final var namespace = "namespace";
		final var notificationId = randomUUID().toString();

		// Act
		assertThatThrownBy(() -> notificationService.getNotification(municipalityId, namespace, notificationId))
			.isInstanceOf(Problem.class)
			.hasMessage(String.format("Not Found: Notification with id '%s' not found in namespace '%s' for municipality with id '%s'", notificationId, namespace, municipalityId));

		// Assert
		verify(notificationRepositoryMock).findByIdAndNamespaceAndMunicipalityId(notificationId, namespace, municipalityId);
	}

	@Test
	void getNotifications() {

		// Arrange
		final var municipalityId = "2281";
		final var namespace = "namespace";
		final var ownerId = randomUUID().toString();
		final var notificationEntity = createNotificationEntity(n -> {});

		when(notificationRepositoryMock.findAllByNamespaceAndMunicipalityIdAndOwnerId(namespace, municipalityId, ownerId)).thenReturn(List.of(notificationEntity));

		// Act
		final var result = notificationService.getNotifications(municipalityId, namespace, ownerId);

		// Assert
		assertThat(result).isNotNull().hasSize(1);
		verify(notificationRepositoryMock).findAllByNamespaceAndMunicipalityIdAndOwnerId(namespace, municipalityId, ownerId);
	}

	@Test
	void getNotificationsNoneFound() {

		// Arrange
		final var municipalityId = "2281";
		final var namespace = "namespace";
		final var ownerId = randomUUID().toString();

		// Act
		final var result = notificationService.getNotifications(municipalityId, namespace, ownerId);

		// Assert
		assertThat(result).isNotNull().isEmpty();
		verify(notificationRepositoryMock).findAllByNamespaceAndMunicipalityIdAndOwnerId(namespace, municipalityId, ownerId);
	}

	@Test
	void createNotification() {

		// Arrange
		final var municipalityId = "2281";
		final var namespace = "namespace";
		final var notification = TestUtil.createNotification(n -> {});
		final var notificationEntity = createNotificationEntity(n -> {});
		final var id = "SomeId";
		final var fullName = "Full Name";
		final var personalPortalData = new PortalPersonData().fullname(fullName);

		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(notification.getErrandId(), municipalityId, namespace)).thenReturn(Optional.of(notificationEntity.getErrand()));
		when(incomingRequestFilterMock.getAdUser()).thenReturn("otherAD");
		when(notificationRepositoryMock.findByNamespaceAndMunicipalityIdAndOwnerIdAndAcknowledgedAndErrandIdAndType(namespace, municipalityId, notification.getOwnerId(), notification.isAcknowledged(), notification.getErrandId(), notification.getType()))
			.thenReturn(empty());
		when(notificationRepositoryMock.save(any())).thenReturn(createNotificationEntity(n -> n.setId(id)));
		when(employeeServiceMock.getEmployeeByLoginName(any())).thenReturn(personalPortalData);

		// Act
		final var result = notificationService.createNotification(municipalityId, namespace, notification);

		// Assert
		assertThat(result).isNotNull();
		verify(notificationRepositoryMock).save(notificationEntityArgumentCaptor.capture());
		assertThat(notificationEntityArgumentCaptor.getValue().getOwnerFullName()).isEqualTo(fullName);
		verify(notificationRepositoryMock).findByNamespaceAndMunicipalityIdAndOwnerIdAndAcknowledgedAndErrandIdAndType(namespace, municipalityId, notification.getOwnerId(), notification.isAcknowledged(), notification.getErrandId(), notification.getType());
	}

	@Test
	void updateNotifications() {

		// Arrange
		final var municipalityId = "2281";
		final var notificationId = randomUUID().toString();
		final var patchNotification = createPatchNotification(n -> n.setId(notificationId));
		final var fullName = "Full Name";
		final var personalPortalData = new PortalPersonData().fullname(fullName);

		when(notificationRepositoryMock.findByIdAndNamespaceAndMunicipalityId(notificationId, NAMESPACE, municipalityId))
			.thenReturn(Optional.ofNullable(createNotificationEntity(n -> n.setId(notificationId))));
		when(employeeServiceMock.getEmployeeByLoginName(any())).thenReturn(personalPortalData);

		// Act
		notificationService.updateNotifications(municipalityId, NAMESPACE, List.of(patchNotification));

		// Assert
		verify(notificationRepositoryMock).save(notificationEntityArgumentCaptor.capture());
		assertThat(notificationEntityArgumentCaptor.getValue().getOwnerId()).isEqualTo(patchNotification.getOwnerId());
		assertThat(notificationEntityArgumentCaptor.getValue().getOwnerFullName()).isEqualTo(fullName);
		assertThat(notificationEntityArgumentCaptor.getValue().getType()).isEqualTo(patchNotification.getType());
		assertThat(notificationEntityArgumentCaptor.getValue().getDescription()).isEqualTo(patchNotification.getDescription());
		assertThat(notificationEntityArgumentCaptor.getValue().isAcknowledged()).isEqualTo(patchNotification.getAcknowledged());
		assertThat(notificationEntityArgumentCaptor.getValue().getNamespace()).isEqualTo(NAMESPACE);
		assertThat(notificationEntityArgumentCaptor.getValue().getMunicipalityId()).isEqualTo(municipalityId);
	}

	@Test
	void updateNotificationNotFound() {

		// Arrange
		final var municipalityId = "2281";
		final var namespace = "namespace";
		final var notificationId = randomUUID().toString();
		final var patchNotification = createPatchNotification(n -> n.setId(notificationId));

		// Act
		assertThatThrownBy(() -> notificationService.updateNotifications(municipalityId, namespace, List.of(patchNotification)))
			.isInstanceOf(Problem.class)
			.hasMessage(String.format("Not Found: Notification with id '%s' not found in namespace '%s' for municipality with id '%s'", notificationId, namespace, municipalityId));

		// Assert
		verify(notificationRepositoryMock).findByIdAndNamespaceAndMunicipalityId(notificationId, namespace, municipalityId);
		verifyNoMoreInteractions(notificationRepositoryMock);
	}

	@Test
	void deleteNotification() {

		// Arrange
		final var municipalityId = "2281";
		final var namespace = "namespace";
		final var notificationId = randomUUID().toString();
		final var notificationEntity = createNotificationEntity(n -> {});

		when(notificationRepositoryMock.findByIdAndNamespaceAndMunicipalityId(notificationId, namespace, municipalityId)).thenReturn(Optional.of(notificationEntity));

		// Act
		notificationService.deleteNotification(municipalityId, namespace, notificationId);

		// Assert
		verify(notificationRepositoryMock).findByIdAndNamespaceAndMunicipalityId(notificationId, namespace, municipalityId);
		verify(notificationRepositoryMock).delete(notificationEntity);
	}

	@Test
	void deleteNotificationNotFound() {

		// Arrange
		final var municipalityId = "2281";
		final var namespace = "namespace";
		final var notificationId = randomUUID().toString();

		// Act
		assertThatThrownBy(() -> notificationService.deleteNotification(municipalityId, namespace, notificationId))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Notification with id '%s' not found in namespace '%s' for municipality with id '%s'".formatted(notificationId, namespace, municipalityId));

		// Assert
		verify(notificationRepositoryMock).findByIdAndNamespaceAndMunicipalityId(notificationId, namespace, municipalityId);
		verifyNoMoreInteractions(notificationRepositoryMock);
	}
}
