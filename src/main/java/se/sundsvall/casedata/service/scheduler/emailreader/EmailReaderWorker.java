package se.sundsvall.casedata.service.scheduler.emailreader;

import static se.sundsvall.casedata.service.scheduler.emailreader.ErrandNumberParser.parseSubject;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNotification;

import generated.se.sundsvall.emailreader.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.casedata.integration.db.AttachmentRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.MessageRepository;
import se.sundsvall.casedata.integration.emailreader.EmailReaderClient;
import se.sundsvall.casedata.integration.emailreader.configuration.EmailReaderProperties;
import se.sundsvall.casedata.service.NotificationService;

@Component
public class EmailReaderWorker {

	private static final Logger LOG = LoggerFactory.getLogger(EmailReaderWorker.class.getName());
	private static final String NOTIFICATION_DESCRIPTION = "Meddelande mottaget";
	private static final String NOTIFICATION_TYPE = "UPDATE";

	private final MessageRepository messageRepository;

	private final ErrandRepository errandRepository;

	private final AttachmentRepository attachmentRepository;

	private final EmailReaderClient emailReaderClient;

	private final EmailReaderProperties emailReaderProperties;

	private final EmailReaderMapper emailReaderMapper;

	private final NotificationService notificationService;

	public EmailReaderWorker(final MessageRepository repository, final ErrandRepository errandRepository, final AttachmentRepository attachmentRepository, final EmailReaderClient client, final EmailReaderProperties emailReaderProperties,
		final EmailReaderMapper emailReaderMapper, final NotificationService notificationService) {
		this.messageRepository = repository;
		this.errandRepository = errandRepository;
		this.attachmentRepository = attachmentRepository;
		this.emailReaderClient = client;
		this.emailReaderProperties = emailReaderProperties;
		this.emailReaderMapper = emailReaderMapper;
		this.notificationService = notificationService;
	}

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
					messageRepository.save(emailReaderMapper.toMessage(email, errand.getMunicipalityId(), errand.getNamespace()).withErrandNumber(errandNumber));
					notificationService.createNotification(errand.getMunicipalityId(), errand.getNamespace(), toNotification(errand, NOTIFICATION_TYPE, NOTIFICATION_DESCRIPTION));
					attachmentRepository.saveAll(emailReaderMapper.toAttachments(email, errand.getMunicipalityId(), errand.getNamespace()).stream()
						.map(attachment -> attachment.withErrandNumber(errandNumber).withMunicipalityId(emailReaderProperties.municipalityId()))
						.toList());
				});

			emailReaderClient.deleteEmail(emailReaderProperties.municipalityId(), email.getId());
		} catch (final Exception e) {
			LOG.error("Error when processing email with subject: {}", email.getSubject(), e);
		}
	}

}
