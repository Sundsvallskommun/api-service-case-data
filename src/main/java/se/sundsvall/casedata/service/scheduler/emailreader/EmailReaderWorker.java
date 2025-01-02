package se.sundsvall.casedata.service.scheduler.emailreader;

import static java.util.Collections.emptyList;
import static se.sundsvall.casedata.service.scheduler.emailreader.ErrandNumberParser.parseSubject;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNotification;

import generated.se.sundsvall.emailreader.Email;
import java.util.List;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
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

	List<Email> getEmails() {

		try {
			return emailReaderClient.getEmail(emailReaderProperties.municipalityId(), emailReaderProperties.namespace());

		} catch (final Exception e) {
			LOG.error("Error when fetching emails from EmailReader", e);
		}
		return emptyList();
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean save(final Email email) {
		try {
			final var errandNumber = parseSubject(email.getSubject());

			errandRepository.findByErrandNumber(errandNumber)
				.filter(errand -> !messageRepository.existsById(email.getId()))
				.ifPresent(errand -> {
					Hibernate.initialize(errand.getStakeholders());
					messageRepository.save(emailReaderMapper.toMessage(email, errand.getMunicipalityId(), errand.getNamespace()).withErrandNumber(errandNumber));
					notificationService.createNotification(errand.getMunicipalityId(), errand.getNamespace(), toNotification(errand, NOTIFICATION_TYPE, NOTIFICATION_DESCRIPTION));
					attachmentRepository.saveAll(emailReaderMapper.toAttachments(email, errand.getMunicipalityId(), errand.getNamespace()).stream()
						.map(attachment -> attachment.withErrandNumber(errandNumber).withMunicipalityId(emailReaderProperties.municipalityId()))
						.toList());
				});
			return true;
		} catch (final Exception e) {
			LOG.error("Error when processing email with subject: {}", email.getSubject(), e);
			return false;
		}
	}

	void deleteMail(final Email email) {
		try {
			emailReaderClient.deleteEmail(emailReaderProperties.municipalityId(), email.getId());
		} catch (final Exception e) {
			LOG.error("Error when deleting email with ID: {}", email.getId(), e);
		}
	}

}
