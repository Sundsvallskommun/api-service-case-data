package se.sundsvall.casedata.service.scheduler.emailreader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;

import static se.sundsvall.casedata.service.scheduler.emailreader.EmailReaderUtilities.isAutoReply;

@Service
@ConditionalOnProperty(prefix = "scheduler.emailreader", name = "enabled", havingValue = "true", matchIfMissing = true)
public class EmailReaderScheduler {

	private static final Logger LOG = LoggerFactory.getLogger(EmailReaderScheduler.class);

	private final EmailReaderWorker emailReaderWorker;

	public EmailReaderScheduler(final EmailReaderWorker emailReaderWorker) {
		this.emailReaderWorker = emailReaderWorker;
	}

	@Dept44Scheduled(
		cron = "${scheduler.emailreader.cron}",
		name = "${scheduler.emailreader.name}",
		lockAtMostFor = "${scheduler.emailreader.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${scheduler.emailreader.maximum-execution-time}")
	void getAndProcessEmails() {

		emailReaderWorker.getEmails()
			.forEach(email -> {
				if (isAutoReply(email)) {
					LOG.info("Email '{}' is an auto-reply and will be deleted without processing", email.getId());
					emailReaderWorker.deleteMail(email);
				} else if (emailReaderWorker.save(email)) {
					emailReaderWorker.deleteMail(email);
				}
			});
	}
}
