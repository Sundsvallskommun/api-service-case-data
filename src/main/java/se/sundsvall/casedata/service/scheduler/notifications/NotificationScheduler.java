package se.sundsvall.casedata.service.scheduler.notifications;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
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
	@SchedulerLock(name = "process_notifications", lockAtMostFor = "${scheduler.notification.shedlock-lock-at-most-for}")
	void process() {
		try {
			RequestId.init();

			LOG.debug("Start processing expired notifications");
			notificationWorker.processExpiredNotifications();
			LOG.debug("Finished processing expired notifications");
		} finally {
			RequestId.reset();
		}
	}
}
