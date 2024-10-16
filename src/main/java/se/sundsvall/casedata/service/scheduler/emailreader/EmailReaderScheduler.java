package se.sundsvall.casedata.service.scheduler.emailreader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Service
@ConditionalOnProperty(prefix = "scheduler.emailreader", name = "enabled", havingValue = "true", matchIfMissing = true)
public class EmailReaderScheduler {

	private static final Logger LOG = LoggerFactory.getLogger(EmailReaderScheduler.class);

	private final EmailReaderWorker emailReaderWorker;

	public EmailReaderScheduler(final EmailReaderWorker emailReaderWorker) {
		this.emailReaderWorker = emailReaderWorker;
	}

	@Scheduled(initialDelayString = "${scheduler.emailreader.initialDelay}", fixedRateString = "${scheduler.emailreader.fixedRate}", zone = "Europe/Stockholm")
	@SchedulerLock(name = "emailreader", lockAtMostFor = "${scheduler.emailreader.shedlock-lock-at-most-for}")
	void getAndProcessEmails() {

		LOG.info("Getting and processing emails");
		emailReaderWorker.getAndProcessEmails();
		LOG.info("Finished getting and processing emails");
	}
}
