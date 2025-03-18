package se.sundsvall.casedata.integration.db.listeners;

import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.NotificationEntity;

@ExtendWith(MockitoExtension.class)
class NotificationListenerTest {

	@Mock
	private ErrandListener errandListener;

	@InjectMocks
	private NotificationListener notificationListener;

	@Test
	void prePersist() {

		// Arrange
		final var errandEntity = new ErrandEntity();
		final var notificationEntity = NotificationEntity.builder().withErrand(errandEntity).build();

		// Act
		notificationListener.prePersist(notificationEntity);

		// Assert
		assertThat(notificationEntity.getCreated()).isCloseTo(now(), within(1, SECONDS));
		assertThat(notificationEntity).hasAllNullFieldsOrPropertiesExcept("acknowledged", "globalAcknowledged", "errand", "created");
	}

	@Test
	void preUpdate() {

		// Arrange
		final var errandEntity = new ErrandEntity();
		final var notificationEntity = NotificationEntity.builder().withErrand(errandEntity).build();

		// Act
		notificationListener.preUpdate(notificationEntity);

		// Assert
		assertThat(notificationEntity.getModified()).isCloseTo(now(), within(1, SECONDS));
		assertThat(notificationEntity).hasAllNullFieldsOrPropertiesExcept("acknowledged", "globalAcknowledged", "errand", "modified");
	}
}
