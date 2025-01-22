package se.sundsvall.casedata.service.scheduler.webmessagecollector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;

@Service
@ConditionalOnProperty(prefix = "scheduler.message-collector", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WebMessageCollectorScheduler {

	private static final Logger LOG = LoggerFactory.getLogger(WebMessageCollectorScheduler.class);

	private final WebMessageCollectorWorker webMessageCollectorWorker;

	public WebMessageCollectorScheduler(final WebMessageCollectorWorker webMessageCollectorWorker) {
		this.webMessageCollectorWorker = webMessageCollectorWorker;
	}

	@Dept44Scheduled(
		cron = "${scheduler.message-collector.cron}",
		name = "${scheduler.message-collector.name}",
		lockAtMostFor = "${scheduler.message-collector.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${scheduler.message-collector.maximum-execution-time}")
	public void getAndProcessMessages() {

		LOG.info("Getting and processing messages");
		final var messageMap = webMessageCollectorWorker.getAndProcessMessages();
		LOG.info("Finished getting and processing messages");

		LOG.info("Deleting messages from WebMessageCollector");
		webMessageCollectorWorker.deleteMessages(messageMap);
		LOG.info("Finished deleting messages from WebMessageCollector");

	}
}
