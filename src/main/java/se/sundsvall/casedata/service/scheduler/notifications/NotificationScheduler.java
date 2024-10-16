package se.sundsvall.casedata.service.scheduler.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import se.sundsvall.dept44.requestid.RequestId;

@Service
@ConditionalOnProperty(prefix = "scheduler.notification", name = "enabled", havingValue = "true", matchIfMissing = true)
public class NotificationScheduler {

	private static final Logger LOG = LoggerFactory.getLogger(NotificationScheduler.class);

	private final NotificationWorker notificationWorker;

	public NotificationScheduler(final NotificationWorker notificationWorker) {
		this.notificationWorker = notificationWorker;
	}

	@Scheduled(cron = "${scheduler.notification.cron}", zone = "Europe/Stockholm")
	@SchedulerLock(name = "cleanup_notifications", lockAtMostFor = "${scheduler.notification.shedlock-lock-at-most-for}")
	void cleanupNotifications() {
		try {
			RequestId.init();

			LOG.debug("Cleaning up notifications");
			notificationWorker.cleanupNotifications();
			LOG.debug("Finished cleaning up notifications");
		} finally {
			RequestId.reset();
		}
	}
}
