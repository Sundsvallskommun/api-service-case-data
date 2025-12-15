package se.sundsvall.casedata.service.notification.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;

import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casedata.api.model.Notification;
import se.sundsvall.casedata.integration.db.NotificationRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.NotificationEntity;
import se.sundsvall.casedata.service.EmployeeService;
import se.sundsvall.dept44.support.Identifier;

@ExtendWith(MockitoExtension.class)
class ErrandOwnerNotificationProcessorTest {

	@Mock
	private EmployeeService employeeServiceMock;

	@Mock
	private NotificationRepository notificationRepositoryMock;

	@Mock
	private Notification notificationMock;

	@Mock
	private ErrandEntity errandEntityMock;

	@InjectMocks
	private ErrandOwnerNotificationProcessor processor;

	private ErrandOwnerNotificationProcessor processorWrapper;

	@BeforeAll
	static void resetIdentifier() {
		Identifier.remove();
	}

	@BeforeEach
	void setup() {
		processorWrapper = Mockito.spy(processor);
	}

	@Test
	void processNotification() {
		// Arrange
		final var id = UUID.randomUUID().toString();

		when(notificationRepositoryMock.save(any(NotificationEntity.class))).thenAnswer(invocation -> {
			final var entity = ((NotificationEntity) invocation.getArgument(0));
			entity.setId(id);
			return entity;
		});

		// Act
		final var createdId = processorWrapper.processNotification(MUNICIPALITY_ID, NAMESPACE, notificationMock, errandEntityMock);

		// Verify and assert
		verify(processorWrapper).applyCommmonBusinessLogic(any());
		verify(notificationRepositoryMock).save(any());
		assertThat(createdId).isEqualTo(id);
	}
}
