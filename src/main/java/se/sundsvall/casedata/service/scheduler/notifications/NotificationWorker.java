package se.sundsvall.casedata.service.scheduler.notifications;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.springframework.util.StringUtils.hasText;
import static se.sundsvall.casedata.service.util.Constants.UNKNOWN;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.casedata.integration.db.NotificationRepository;
import se.sundsvall.casedata.integration.db.model.NotificationEntity;

@Component
public class NotificationWorker {

	private final NotificationRepository notificationRepository;

	public NotificationWorker(final NotificationRepository notificationRepository) {
		this.notificationRepository = notificationRepository;
	}

	@Transactional
	public void cleanUpNotifications() {

		final var entitiesToDelete = notificationRepository.findByExpiresBefore(now(systemDefault())).stream()
			.filter(notification -> isGlobalAcknowledgedAndHasUnknownOwner(notification) || isBothAcknowledgedAndGlobalAcknowledged(notification))
			.toList();

		notificationRepository.deleteAllInBatch(entitiesToDelete);
	}

	private boolean isGlobalAcknowledgedAndHasUnknownOwner(NotificationEntity notification) {
		return notification.isGlobalAcknowledged() && (equalsIgnoreCase(notification.getOwnerId(), UNKNOWN) || !hasText(notification.getOwnerId()));
	}

	private boolean isBothAcknowledgedAndGlobalAcknowledged(NotificationEntity notification) {
		return notification.isAcknowledged() && notification.isGlobalAcknowledged();
	}
}
