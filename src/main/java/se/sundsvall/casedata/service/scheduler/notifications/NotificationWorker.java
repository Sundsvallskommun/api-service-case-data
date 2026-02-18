package se.sundsvall.casedata.service.scheduler.notifications;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.casedata.integration.db.NotificationRepository;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;

@Component
public class NotificationWorker {

	private final NotificationRepository notificationRepository;

	public NotificationWorker(final NotificationRepository notificationRepository) {
		this.notificationRepository = notificationRepository;
	}

	@Transactional
	public void cleanUpNotifications() {

		notificationRepository.deleteByExpiresBefore(now(systemDefault()));
	}
}
