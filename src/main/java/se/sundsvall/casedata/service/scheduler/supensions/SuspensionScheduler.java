package se.sundsvall.casedata.service.scheduler.supensions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;

@Service
@ConditionalOnProperty(prefix = "scheduler.suspension", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SuspensionScheduler {

	private static final Logger LOG = LoggerFactory.getLogger(SuspensionScheduler.class);

	private final SuspensionWorker suspensionWorker;

	public SuspensionScheduler(final SuspensionWorker suspensionWorker) {
		this.suspensionWorker = suspensionWorker;
	}

	@Dept44Scheduled(
		cron = "${scheduler.suspension.cron}",
		name = "${scheduler.suspension.name}",
		lockAtMostFor = "${scheduler.suspension.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${scheduler.suspension.maximum-execution-time}")
	void processs() {
		suspensionWorker.processExpiredSuspensions();
	}
}
