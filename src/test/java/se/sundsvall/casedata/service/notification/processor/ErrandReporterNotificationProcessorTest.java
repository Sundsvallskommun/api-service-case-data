package se.sundsvall.casedata.service.notification.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;

import generated.se.sundsvall.employee.PortalPersonData;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casedata.api.model.Notification;
import se.sundsvall.casedata.api.model.validation.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.NotificationRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.NotificationEntity;
import se.sundsvall.casedata.integration.db.model.StakeholderEntity;
import se.sundsvall.casedata.service.EmployeeService;
import se.sundsvall.dept44.support.Identifier;

@ExtendWith(MockitoExtension.class)
class ErrandReporterNotificationProcessorTest {

	@Mock
	private EmployeeService employeeServiceMock;

	@Mock
	private NotificationRepository notificationRepositoryMock;

	@Mock
	private Notification notificationMock;

	@Mock
	private ErrandEntity errandEntityMock;

	@InjectMocks
	private ErrandReporterNotificationProcessor processor;

	private ErrandReporterNotificationProcessor processorWrapper;

	@BeforeAll
	static void resetIdentifier() {
		Identifier.remove();
	}

	@BeforeEach
	void setup() {
		processorWrapper = Mockito.spy(processor);
	}

	@Test
	void processNotificationWhenErrandIsNull() {
		// Act and assert
		assertThat(processorWrapper.processNotification(MUNICIPALITY_ID, NAMESPACE, notificationMock, null)).isNull();

		// Verify
		verifyNoInteractions(employeeServiceMock, notificationRepositoryMock, notificationMock, errandEntityMock);
	}

	@ParameterizedTest
	@NullAndEmptySource
	void processNotificationWhenNoStakeholders(List<StakeholderEntity> stakeholders) {
		// Arrange
		when(errandEntityMock.getStakeholders()).thenReturn(stakeholders);

		// Act and assert
		assertThat(processorWrapper.processNotification(MUNICIPALITY_ID, NAMESPACE, notificationMock, errandEntityMock)).isNull();

		// Verify
		verify(errandEntityMock).getStakeholders();
		verifyNoMoreInteractions(errandEntityMock);
		verifyNoInteractions(employeeServiceMock, notificationRepositoryMock, notificationMock);
	}

	@ParameterizedTest
	@NullAndEmptySource
	void processNotificationWhenCorrectRoleButNoAdAccountPresent(String adAccount) {
		// Arrange
		when(errandEntityMock.getStakeholders()).thenReturn(List.of(StakeholderEntity.builder()
			.withAdAccount(adAccount)
			.withRoles(List.of(StakeholderRole.REPORTER.name())).build()));

		// Act and assert
		assertThat(processorWrapper.processNotification(MUNICIPALITY_ID, NAMESPACE, notificationMock, errandEntityMock)).isNull();

		// Verify
		verify(errandEntityMock).getStakeholders();
		verifyNoMoreInteractions(errandEntityMock);
		verifyNoInteractions(employeeServiceMock, notificationRepositoryMock, notificationMock);
	}

	@ParameterizedTest
	@EnumSource(value = StakeholderRole.class, names = "REPORTER", mode = Mode.EXCLUDE)
	void processNotificationWhenAdAccountPresentButNotCorrectRole(StakeholderRole stakeholderRole) {
		// Arrange
		when(errandEntityMock.getStakeholders()).thenReturn(List.of(StakeholderEntity.builder()
			.withAdAccount("adAccount")
			.withRoles(List.of(stakeholderRole.name())).build()));

		// Act and assert
		assertThat(processorWrapper.processNotification(MUNICIPALITY_ID, NAMESPACE, notificationMock, errandEntityMock)).isNull();

		// Verify
		verify(errandEntityMock).getStakeholders();
		verifyNoMoreInteractions(errandEntityMock);
		verifyNoInteractions(employeeServiceMock, notificationRepositoryMock, notificationMock);
	}

	@Test
	void processNotificationWhenCorrectRoleAndAdAccountPresent() {
		// Arrange
		final var adAccount = "adAccount";
		final var fullname = "fullname";
		final var id = UUID.randomUUID().toString();

		when(errandEntityMock.getStakeholders()).thenReturn(List.of(StakeholderEntity.builder()
			.withAdAccount("adAccount")
			.withRoles(List.of(StakeholderRole.REPORTER.name())).build()));
		when(employeeServiceMock.getEmployeeByLoginName(MUNICIPALITY_ID, adAccount)).thenReturn(new PortalPersonData().fullname(fullname));
		when(notificationRepositoryMock.save(any(NotificationEntity.class))).thenAnswer(invocation -> {
			final var entity = ((NotificationEntity) invocation.getArgument(0));
			entity.setId(id);
			return entity;
		});

		// Act and assert
		assertThat(processorWrapper.processNotification(MUNICIPALITY_ID, NAMESPACE, notificationMock, errandEntityMock)).isEqualTo(id);

		// Verify
		verify(errandEntityMock).getStakeholders();
		verify(errandEntityMock).getErrandNumber();
		verify(employeeServiceMock).getEmployeeByLoginName(MUNICIPALITY_ID, adAccount);
		verify(notificationRepositoryMock).save(any());
		verifyNoMoreInteractions(errandEntityMock, employeeServiceMock, notificationRepositoryMock);
	}
}
