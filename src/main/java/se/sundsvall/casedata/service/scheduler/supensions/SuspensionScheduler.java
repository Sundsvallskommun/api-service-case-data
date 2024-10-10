package se.sundsvall.casedata.service.scheduler.supensions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import se.sundsvall.dept44.requestid.RequestId;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Service
@ConditionalOnProperty(prefix = "scheduler.suspensions", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SuspensionScheduler {

	private static final Logger LOG = LoggerFactory.getLogger(SuspensionScheduler.class);

	private final SuspensionWorker suspensionWorker;

	public SuspensionScheduler(final SuspensionWorker suspensionWorker) {
		this.suspensionWorker = suspensionWorker;
	}

	@Scheduled(cron = "${scheduler.suspension.cron}")
	@SchedulerLock(name = "clean_suspensions", lockAtMostFor = "${scheduler.suspension.shedlock-lock-at-most-for}")
	void cleanUpSuspensions() {
		try {
			RequestId.init();

			LOG.debug("Cleaning up suspensions");
			suspensionWorker.cleanUpSuspensions();
			LOG.debug("Finished cleaning up suspensions");
		} finally {
			RequestId.reset();
		}
	}

}
