package se.sundsvall.casedata.service.scheduler.webmessagecollector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import se.sundsvall.dept44.requestid.RequestId;

@Service
@ConditionalOnProperty(prefix = "scheduler.message-collector", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WebMessageCollectorScheduler {

	private static final Logger LOG = LoggerFactory.getLogger(WebMessageCollectorScheduler.class);

	private final WebMessageCollectorWorker webMessageCollectorWorker;

	public WebMessageCollectorScheduler(final WebMessageCollectorWorker webMessageCollectorWorker) {
		this.webMessageCollectorWorker = webMessageCollectorWorker;
	}

	@Scheduled(initialDelayString = "${scheduler.message-collector.initialDelay}", fixedRateString = "${scheduler.message-collector.fixedRate}", zone = "Europe/Stockholm")
	@SchedulerLock(name = "message-collector", lockAtMostFor = "${scheduler.message-collector.shedlock-lock-at-most-for}")
	public void getAndProcessMessages() {
		try {
			RequestId.init();

			LOG.info("Getting and processing messages");
			webMessageCollectorWorker.getAndProcessMessages();
			LOG.info("Finished getting and processing messages");
		} finally {
			RequestId.reset();
		}
	}
}
