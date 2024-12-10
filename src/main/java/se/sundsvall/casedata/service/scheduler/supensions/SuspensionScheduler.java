package se.sundsvall.casedata.service.scheduler.supensions;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.requestid.RequestId;

@Service
@ConditionalOnProperty(prefix = "scheduler.suspension", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SuspensionScheduler {

	private static final Logger LOG = LoggerFactory.getLogger(SuspensionScheduler.class);

	private final SuspensionWorker suspensionWorker;

	public SuspensionScheduler(final SuspensionWorker suspensionWorker) {
		this.suspensionWorker = suspensionWorker;
	}

	@Scheduled(cron = "${scheduler.suspension.cron}", zone = "Europe/Stockholm")
	@SchedulerLock(name = "process_suspensions", lockAtMostFor = "${scheduler.suspension.shedlock-lock-at-most-for}")
	void processs() {
		try {
			RequestId.init();

			LOG.debug("Processing suspensions");
			suspensionWorker.processExpiredSuspensions();
			LOG.debug("Finished processing suspensions");
		} finally {
			RequestId.reset();
		}
	}
}
