package se.sundsvall.casedata.service.notification.processor;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;

import generated.se.sundsvall.employee.PortalPersonData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casedata.api.model.Notification;
import se.sundsvall.casedata.integration.db.NotificationRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.NotificationEntity;
import se.sundsvall.casedata.service.EmployeeService;
import se.sundsvall.dept44.support.Identifier;

@ExtendWith(MockitoExtension.class)
class AbstractNotificationProcessorTest {

	@Mock
	private EmployeeService employeeServiceMock;

	@Mock
	private NotificationRepository notificationRepositoryMock;

	@Mock
	private NotificationEntity notificationEntityMock;

	@InjectMocks
	private DummyProcessor processor;

	@BeforeEach
	void setup() {
		Identifier.remove();
	}

	@AfterEach
	void teardown() {
		verifyNoMoreInteractions(employeeServiceMock, notificationEntityMock, notificationRepositoryMock);
	}

	@Test
	void applyCommmonBusinessLogicWhenExecutingUserIsNotificationOwner() {
		final var adUsername = "adUsername";
		final var fullname = "fullname";

		Identifier.set(Identifier.parse("%s; type=adAccount".formatted(adUsername)));

		when(notificationEntityMock.getOwnerId()).thenReturn(adUsername);
		when(notificationEntityMock.getMunicipalityId()).thenReturn(MUNICIPALITY_ID);
		when(employeeServiceMock.getEmployeeByLoginName(MUNICIPALITY_ID, adUsername)).thenReturn(new PortalPersonData().fullname(fullname));

		processor.applyCommmonBusinessLogic(notificationEntityMock);

		verify(employeeServiceMock, times(2)).getEmployeeByLoginName(MUNICIPALITY_ID, adUsername);
		verify(notificationEntityMock).setAcknowledged(true);
		verify(notificationEntityMock).setOwnerFullName(fullname);
		verify(notificationEntityMock).setCreatedBy(adUsername);
		verify(notificationEntityMock).setCreatedByFullName(fullname);
	}

	@Test
	void applyCommmonBusinessLogicWhenExecutingUserIsNotNotificationOwner() {
		final var adUsername = "adUsername";
		final var fullname = "fullname";
		final var notificationOwner = "noteOwnerUsername";
		final var notificationOwnerFullname = "notificationOwnerFullname";

		Identifier.set(Identifier.parse("%s; type=adAccount".formatted(adUsername)));

		when(notificationEntityMock.getOwnerId()).thenReturn(notificationOwner);
		when(notificationEntityMock.getMunicipalityId()).thenReturn(MUNICIPALITY_ID);
		when(employeeServiceMock.getEmployeeByLoginName(MUNICIPALITY_ID, adUsername)).thenReturn(new PortalPersonData().fullname(fullname));
		when(employeeServiceMock.getEmployeeByLoginName(MUNICIPALITY_ID, notificationOwner)).thenReturn(new PortalPersonData().fullname(notificationOwnerFullname));

		processor.applyCommmonBusinessLogic(notificationEntityMock);

		verify(employeeServiceMock).getEmployeeByLoginName(MUNICIPALITY_ID, adUsername);
		verify(employeeServiceMock).getEmployeeByLoginName(MUNICIPALITY_ID, notificationOwner);
		verify(notificationEntityMock).setOwnerFullName(notificationOwnerFullname);
		verify(notificationEntityMock).setCreatedBy(adUsername);
		verify(notificationEntityMock).setCreatedByFullName(fullname);
	}

	@Test
	void applyCommmonBusinessLogicWhenOwnerIdNotSet() {
		final var adUsername = "adUsername";
		final var fullname = "fullname";

		Identifier.set(Identifier.parse("%s; type=adAccount".formatted(adUsername)));

		when(notificationEntityMock.getMunicipalityId()).thenReturn(MUNICIPALITY_ID);
		when(employeeServiceMock.getEmployeeByLoginName(MUNICIPALITY_ID, adUsername)).thenReturn(new PortalPersonData().fullname(fullname));

		processor.applyCommmonBusinessLogic(notificationEntityMock);

		verify(notificationEntityMock, times(2)).getOwnerId();
		verify(employeeServiceMock).getEmployeeByLoginName(MUNICIPALITY_ID, adUsername);
		verify(notificationEntityMock).setCreatedBy(adUsername);
		verify(notificationEntityMock).setCreatedByFullName(fullname);
	}

	@Test
	void applyCommmonBusinessLogicWhenIdentifierAndOwnerIdNotSet() {
		processor.applyCommmonBusinessLogic(notificationEntityMock);

		verify(notificationEntityMock, times(2)).getOwnerId();
	}

	private static class DummyProcessor extends AbstractNotificationProcessor {

		DummyProcessor(NotificationRepository notificationRepository, EmployeeService employeeService) {
			super(notificationRepository, employeeService);
		}

		@Override
		public String processNotification(String municipalityId, String namespace, Notification notification, ErrandEntity errandEntity) {
			return null;
		}

	}
}
