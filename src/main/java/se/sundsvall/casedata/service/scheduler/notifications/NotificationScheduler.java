package se.sundsvall.casedata.service.scheduler.notifications;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;

@Service
@ConditionalOnProperty(prefix = "scheduler.notification", name = "enabled", havingValue = "true", matchIfMissing = true)
public class NotificationScheduler {

	private final NotificationWorker notificationWorker;

	public NotificationScheduler(final NotificationWorker notificationWorker) {
		this.notificationWorker = notificationWorker;
	}

	@Dept44Scheduled(
		cron = "${scheduler.notification.cron}",
		name = "${scheduler.notification.name}",
		lockAtMostFor = "${scheduler.notification.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${scheduler.notification.maximum-execution-time}")
	void process() {
		notificationWorker.cleanUpNotifications();
	}
}
