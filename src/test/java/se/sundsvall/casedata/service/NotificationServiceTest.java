package se.sundsvall.casedata.service;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.Sort.unsorted;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.TestUtil.createNotificationEntity;
import static se.sundsvall.casedata.TestUtil.createPatchNotification;

import generated.se.sundsvall.employee.PortalPersonData;
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

	@Captor
	private ArgumentCaptor<List<NotificationEntity>> notificationEntityListArgumentCaptor;

	@Test
	void findNotification() {

		// Arrange
		final var municipalityId = "2281";
		final var namespace = "namespace";
		final var errandId = 123L;
		final var notificationId = randomUUID().toString();
		final var notificationEntity = createNotificationEntity(n -> {});

		when(notificationRepositoryMock.findByIdAndNamespaceAndMunicipalityIdAndErrandId(notificationId, namespace, municipalityId, errandId)).thenReturn(Optional.of(notificationEntity));

		// Act
		final var result = notificationService.findNotification(municipalityId, namespace, errandId, notificationId);

		// Assert
		assertThat(result).isNotNull();
		verify(notificationRepositoryMock).findByIdAndNamespaceAndMunicipalityIdAndErrandId(notificationId, namespace, municipalityId, errandId);
	}

	@Test
	void findNotificationNotFound() {

		// Arrange
		final var municipalityId = "2281";
		final var namespace = "namespace";
		final var errandId = 123L;
		final var notificationId = randomUUID().toString();

		// Act
		assertThatThrownBy(() -> notificationService.findNotification(municipalityId, namespace, errandId, notificationId))
			.isInstanceOf(Problem.class)
			.hasMessage(String.format("Not Found: Notification with id:'%s' not found in namespace:'%s' for municipality with id:'%s' and errand with id:'%s'", notificationId, namespace, municipalityId, errandId));

		// Assert
		verify(notificationRepositoryMock).findByIdAndNamespaceAndMunicipalityIdAndErrandId(notificationId, namespace, municipalityId, errandId);
	}

	@Test
	void findNotificationsByOwnerId() {

		// Arrange
		final var municipalityId = "2281";
		final var namespace = "namespace";
		final var ownerId = randomUUID().toString();
		final var notificationEntity = createNotificationEntity(n -> {});

		when(notificationRepositoryMock.findAllByNamespaceAndMunicipalityIdAndOwnerId(namespace, municipalityId, ownerId)).thenReturn(List.of(notificationEntity));

		// Act
		final var result = notificationService.findNotificationsByOwnerId(municipalityId, namespace, ownerId);

		// Assert
		assertThat(result).isNotNull().hasSize(1);
		verify(notificationRepositoryMock).findAllByNamespaceAndMunicipalityIdAndOwnerId(namespace, municipalityId, ownerId);
	}

	@Test
	void findNotificationsByOwnerIdNoneFound() {

		// Arrange
		final var municipalityId = "2281";
		final var namespace = "namespace";
		final var ownerId = randomUUID().toString();

		// Act
		final var result = notificationService.findNotificationsByOwnerId(municipalityId, namespace, ownerId);

		// Assert
		assertThat(result).isNotNull().isEmpty();
		verify(notificationRepositoryMock).findAllByNamespaceAndMunicipalityIdAndOwnerId(namespace, municipalityId, ownerId);
	}

	@Test
	void create() {

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

		when(notificationRepositoryMock.save(any())).thenReturn(createNotificationEntity(n -> n.setId(id)));
		when(employeeServiceMock.getEmployeeByLoginName(any(), any())).thenReturn(personalPortalData);

		// Act
		final var result = notificationService.create(municipalityId, namespace, notification);

		// Assert
		assertThat(result).isNotNull();
		verify(notificationRepositoryMock).save(notificationEntityArgumentCaptor.capture());
		assertThat(notificationEntityArgumentCaptor.getValue().getOwnerFullName()).isEqualTo(fullName);
		verifyNoMoreInteractions(notificationRepositoryMock);
	}

	@Test
	void createNotification() {

		// Arrange
		final var municipalityId = "2281";
		final var namespace = "namespace";
		final var notification = TestUtil.createNotification(n -> {});
		final var notificationEntity = createNotificationEntity(n -> {});
		final var id = "SomeId";
		final var executingUserId = "executingUserId";
		final var createdByFullName = "createdByFullName";
		final var ownerFullName = "ownerFullName";

		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(notification.getErrandId(), municipalityId, namespace)).thenReturn(Optional.of(notificationEntity.getErrand()));
		when(notificationRepositoryMock.save(any())).thenReturn(createNotificationEntity(n -> n.setId(id)));
		when(employeeServiceMock.getEmployeeByLoginName(municipalityId, notification.getOwnerId())).thenReturn(new PortalPersonData().loginName(notification.getOwnerId()).fullname(ownerFullName));
		when(employeeServiceMock.getEmployeeByLoginName(municipalityId, executingUserId)).thenReturn(new PortalPersonData().loginName(executingUserId).fullname(createdByFullName));
		when(incomingRequestFilterMock.getAdUser()).thenReturn("executingUserId");

		// Act
		final var result = notificationService.create(municipalityId, namespace, notification);

		// Assert
		assertThat(result).isNotNull().isEqualTo(id);
		verify(employeeServiceMock).getEmployeeByLoginName(municipalityId, executingUserId);
		verify(employeeServiceMock).getEmployeeByLoginName(municipalityId, notification.getOwnerId());
		verify(notificationRepositoryMock).save(notificationEntityArgumentCaptor.capture());
		assertThat(notificationEntityArgumentCaptor.getValue().getOwnerFullName()).isEqualTo(ownerFullName);
		assertThat(notificationEntityArgumentCaptor.getValue().getCreatedByFullName()).isEqualTo(createdByFullName);
		assertThat(notificationEntityArgumentCaptor.getValue().isGlobalAcknowledged()).isFalse();
		assertThat(notificationEntityArgumentCaptor.getValue().isAcknowledged()).isFalse();
	}

	@Test
	void createNotificationWhenExecutinUserIsTheSameAsOwnerId() {

		// Arrange
		final var municipalityId = "2281";
		final var namespace = "namespace";
		final var notification = TestUtil.createNotification(n -> {});
		final var errandEntity = TestUtil.createNotificationEntity(n -> {}).getErrand();
		final var id = "SomeId";
		final var executingUserId = notification.getOwnerId();
		final var fullName = "fullName";

		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(notification.getErrandId(), municipalityId, namespace)).thenReturn(Optional.of(errandEntity));
		when(notificationRepositoryMock.save(any())).thenReturn(createNotificationEntity(n -> n.setId(id)));
		when(employeeServiceMock.getEmployeeByLoginName(municipalityId, executingUserId)).thenReturn(new PortalPersonData().loginName(executingUserId).fullname(fullName));
		when(incomingRequestFilterMock.getAdUser()).thenReturn(executingUserId);

		// Act
		final var result = notificationService.create(municipalityId, namespace, notification);

		// Assert
		assertThat(result).isNotNull().isEqualTo(id);
		verify(employeeServiceMock, times(2)).getEmployeeByLoginName(municipalityId, executingUserId);
		verify(notificationRepositoryMock).save(notificationEntityArgumentCaptor.capture());
		assertThat(notificationEntityArgumentCaptor.getValue().getOwnerFullName()).isEqualTo(fullName);
		assertThat(notificationEntityArgumentCaptor.getValue().getCreatedByFullName()).isEqualTo(fullName);
		assertThat(notificationEntityArgumentCaptor.getValue().isGlobalAcknowledged()).isFalse();
		assertThat(notificationEntityArgumentCaptor.getValue().isAcknowledged()).isTrue(); // Set to true when ownerId == executingUserId
	}

	@Test
	void update() {

		// Arrange
		final var municipalityId = "2281";
		final var notificationId = randomUUID().toString();
		final var patchNotification = createPatchNotification(n -> n.setId(notificationId));
		final var fullName = "Full Name";
		final var personalPortalData = new PortalPersonData().fullname(fullName);

		when(notificationRepositoryMock.findByIdAndNamespaceAndMunicipalityIdAndErrandId(notificationId, NAMESPACE, municipalityId, patchNotification.getErrandId()))
			.thenReturn(Optional.ofNullable(createNotificationEntity(n -> n.setId(notificationId))));

		when(employeeServiceMock.getEmployeeByLoginName(any(), any())).thenReturn(personalPortalData);

		// Act
		notificationService.update(municipalityId, NAMESPACE, List.of(patchNotification));

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
	void updateNotFound() {

		// Arrange
		final var municipalityId = "2281";
		final var namespace = "namespace";
		final var notificationId = randomUUID().toString();
		final var patchNotification = createPatchNotification(n -> n.setId(notificationId));

		// Act
		assertThatThrownBy(() -> notificationService.update(municipalityId, namespace, List.of(patchNotification)))
			.isInstanceOf(Problem.class)
			.hasMessage(String.format("Not Found: Notification with id:'%s' not found in namespace:'%s' for municipality with id:'%s' and errand with id:'%s'", notificationId, namespace, municipalityId, patchNotification.getErrandId()));

		// Assert
		verify(notificationRepositoryMock).findByIdAndNamespaceAndMunicipalityIdAndErrandId(notificationId, namespace, municipalityId, patchNotification.getErrandId());
		verifyNoMoreInteractions(notificationRepositoryMock);
	}

	@Test
	void delete() {

		// Arrange
		final var municipalityId = "2281";
		final var namespace = "namespace";
		final var errandId = 123L;
		final var notificationId = randomUUID().toString();
		final var notificationEntity = createNotificationEntity(n -> {});

		when(notificationRepositoryMock.findByIdAndNamespaceAndMunicipalityIdAndErrandId(notificationId, namespace, municipalityId, errandId))
			.thenReturn(Optional.of(notificationEntity));

		// Act
		notificationService.delete(municipalityId, namespace, errandId, notificationId);

		// Assert
		verify(notificationRepositoryMock).findByIdAndNamespaceAndMunicipalityIdAndErrandId(notificationId, namespace, municipalityId, errandId);
		verify(notificationRepositoryMock).delete(notificationEntity);
	}

	@Test
	void deleteNotFound() {

		// Arrange
		final var municipalityId = "2281";
		final var namespace = "namespace";
		final var errandId = 123L;
		final var notificationId = randomUUID().toString();

		// Act
		assertThatThrownBy(() -> notificationService.delete(municipalityId, namespace, errandId, notificationId))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Notification with id:'%s' not found in namespace:'%s' for municipality with id:'%s' and errand with id:'%s'".formatted(notificationId, namespace, municipalityId, errandId));

		// Assert
		verify(notificationRepositoryMock).findByIdAndNamespaceAndMunicipalityIdAndErrandId(notificationId, namespace, municipalityId, errandId);
		verifyNoMoreInteractions(notificationRepositoryMock);
	}

	@Test
	void globalAcknowledgeNotificationsByErrandId() {

		// Arrange
		final var municipalityId = "2281";
		final var namespace = "namespace";
		final var errandId = 678L;
		final var notificationEntity1 = new NotificationEntity();
		final var notificationEntity2 = new NotificationEntity();
		final var notificationEntity3 = new NotificationEntity();

		when(notificationRepositoryMock.findAllByNamespaceAndMunicipalityIdAndErrandId(eq(namespace), eq(municipalityId), eq(errandId), any()))
			.thenReturn(List.of(notificationEntity1, notificationEntity2, notificationEntity3));

		// Act
		notificationService.globalAcknowledgeNotificationsByErrandId(municipalityId, namespace, errandId);

		// Assert
		verify(notificationRepositoryMock).findAllByNamespaceAndMunicipalityIdAndErrandId(namespace, municipalityId, errandId, unsorted());
		verify(notificationRepositoryMock).saveAll(notificationEntityListArgumentCaptor.capture());

		final var capturedNotificationEntitySaveList = notificationEntityListArgumentCaptor.getValue();
		assertThat(capturedNotificationEntitySaveList).hasSize(3);
		capturedNotificationEntitySaveList.stream().forEach(elem -> assertThat(elem.isGlobalAcknowledged()).isTrue());
	}

	@Test
	void globalAcknowledgeNotificationsByErrandIdWhenNothingFound() {

		// Arrange
		final var municipalityId = "2281";
		final var namespace = "namespace";
		final var errandId = 678L;

		when(notificationRepositoryMock.findAllByNamespaceAndMunicipalityIdAndErrandId(eq(namespace), eq(municipalityId), eq(errandId), any()))
			.thenReturn(emptyList());

		// Act
		notificationService.globalAcknowledgeNotificationsByErrandId(municipalityId, namespace, errandId);

		// Assert
		verify(notificationRepositoryMock).findAllByNamespaceAndMunicipalityIdAndErrandId(namespace, municipalityId, errandId, unsorted());
		verify(notificationRepositoryMock).saveAll(notificationEntityListArgumentCaptor.capture());

		final var capturedNotificationEntitySaveList = notificationEntityListArgumentCaptor.getValue();
		assertThat(capturedNotificationEntitySaveList).isEmpty();
	}
}
