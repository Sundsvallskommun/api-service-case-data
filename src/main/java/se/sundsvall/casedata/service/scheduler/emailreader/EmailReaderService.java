package se.sundsvall.casedata.service.scheduler.emailreader;

import static se.sundsvall.casedata.service.scheduler.emailreader.ErrandNumberParser.parseSubject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import se.sundsvall.casedata.integration.db.AttachmentRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.MessageRepository;
import se.sundsvall.casedata.integration.emailreader.EmailReaderClient;
import se.sundsvall.casedata.integration.emailreader.configuration.EmailReaderProperties;

import generated.se.sundsvall.emailreader.Email;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Component
@ConditionalOnProperty(prefix = "scheduler.emailreader", name = "enabled", havingValue = "true", matchIfMissing = true)
public class EmailReaderService {

	private static final Logger LOG = LoggerFactory.getLogger(EmailReaderService.class.getName());

	private final MessageRepository messageRepository;

	private final ErrandRepository errandRepository;

	private final AttachmentRepository attachmentRepository;

	private final EmailReaderClient emailReaderClient;

	private final EmailReaderProperties emailReaderProperties;

	private final EmailReaderMapper emailReaderMapper;

	public EmailReaderService(final MessageRepository repository, final ErrandRepository errandRepository, final AttachmentRepository attachmentRepository, final EmailReaderClient client, final EmailReaderProperties emailReaderProperties,
		final EmailReaderMapper emailReaderMapper) {
		this.messageRepository = repository;
		this.errandRepository = errandRepository;
		this.attachmentRepository = attachmentRepository;
		this.emailReaderClient = client;
		this.emailReaderProperties = emailReaderProperties;
		this.emailReaderMapper = emailReaderMapper;
	}

	@Scheduled(initialDelayString = "${scheduler.emailreader.initialDelay}", fixedRateString = "${scheduler.emailreader.fixedRate}")
	@SchedulerLock(name = "emailreader", lockAtMostFor = "${scheduler.emailreader.shedlock-lock-at-most-for}")
	void getAndProcessEmails() {

		try {
			emailReaderClient.getEmail(emailReaderProperties.municipalityId(), emailReaderProperties.namespace())
				.forEach(this::saveAndRemoteDelete);
		} catch (final Exception e) {
			LOG.error("Error when fetching emails from EmailReader", e);
		}
	}

	@Transactional
	public void saveAndRemoteDelete(final Email email) {
		try {
			final var errandNumber = parseSubject(email.getSubject());

			errandRepository.findByErrandNumber(errandNumber)
				.filter(errand -> !messageRepository.existsById(email.getId()))
				.ifPresent(errand -> {
					messageRepository.save(emailReaderMapper.toMessage(email, emailReaderProperties.municipalityId()).withErrandNumber(errandNumber));
					attachmentRepository.saveAll(emailReaderMapper.toAttachments(email).stream()
						.map(attachment -> attachment.withErrandNumber(errandNumber))
						.toList());
				});

			emailReaderClient.deleteEmail(email.getId());
		} catch (final Exception e) {
			LOG.error("Error when processing email with subject: {}", email.getSubject(), e);
		}
	}

}
