package se.sundsvall.casedata.service.scheduler.emailreader;

import static java.util.Collections.emptyList;
import static se.sundsvall.casedata.service.scheduler.emailreader.ErrandNumberParser.parseSubject;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNotification;

import generated.se.sundsvall.emailreader.Email;
import generated.se.sundsvall.emailreader.EmailAttachment;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.casedata.integration.db.AttachmentRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.MessageAttachmentRepository;
import se.sundsvall.casedata.integration.db.MessageRepository;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;
import se.sundsvall.casedata.integration.db.model.MessageAttachmentEntity;
import se.sundsvall.casedata.integration.emailreader.EmailReaderClient;
import se.sundsvall.casedata.integration.emailreader.configuration.EmailReaderProperties;
import se.sundsvall.casedata.service.NotificationService;
import se.sundsvall.casedata.service.scheduler.MessageMapper;
import se.sundsvall.dept44.scheduling.health.Dept44HealthUtility;

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
	private final MessageAttachmentRepository messageAttachmentRepository;
	private final MessageMapper messageMapper;
	private final NotificationService notificationService;
	private final Dept44HealthUtility dept44HealthUtility;
	@Value("${scheduler.emailreader.name}")
	private String jobName;

	public EmailReaderWorker(final MessageRepository repository, final ErrandRepository errandRepository, final AttachmentRepository attachmentRepository, final EmailReaderClient client, final EmailReaderProperties emailReaderProperties,
		final MessageAttachmentRepository messageAttachmentRepository, final MessageMapper messageMapper, final NotificationService notificationService, final Dept44HealthUtility dept44HealthUtility) {
		this.messageRepository = repository;
		this.errandRepository = errandRepository;
		this.attachmentRepository = attachmentRepository;
		this.emailReaderClient = client;
		this.emailReaderProperties = emailReaderProperties;
		this.messageAttachmentRepository = messageAttachmentRepository;
		this.messageMapper = messageMapper;
		this.notificationService = notificationService;
		this.dept44HealthUtility = dept44HealthUtility;
	}

	List<Email> getEmails() {

		try {
			return emailReaderClient.getEmail(emailReaderProperties.municipalityId(), emailReaderProperties.namespace());

		} catch (final Exception e) {
			dept44HealthUtility.setHealthIndicatorUnhealthy(jobName, "Error when fetching emails from EmailReader");

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
					messageRepository.save(messageMapper.toMessage(email, errand.getMunicipalityId(), errand.getNamespace(), errand.getId()));
					notificationService.create(errand.getMunicipalityId(), errand.getNamespace(), toNotification(errand, NOTIFICATION_TYPE, NOTIFICATION_DESCRIPTION));

					email.getAttachments()
						.forEach(emailAttachment -> processAttachment(emailAttachment, email.getId(), errand.getId(), errand.getMunicipalityId(), errand.getMunicipalityId()));
				});
			return true;
		} catch (final Exception e) {
			dept44HealthUtility.setHealthIndicatorUnhealthy(jobName, "Error when processing email");
			LOG.error("Error when processing email with subject: {}", email.getSubject(), e);
			return false;
		}
	}

	void processAttachment(final EmailAttachment attachment, final String messageId, final Long errandId, final String municipalityId, final String namespace) {

		try {
			final var messageAttachment = messageMapper.toAttachmentEntity(attachment, messageId, municipalityId, namespace);
			final var attachmentEntity = messageMapper.toAttachmentEntity(messageAttachment).withErrandId(errandId);
			// Save the attachment
			messageAttachmentRepository.save(messageAttachment);
			attachmentRepository.save(attachmentEntity);

			// Process the file content
			processAttachmentData(messageAttachment, attachmentEntity);
		} catch (final Exception e) {
			dept44HealthUtility.setHealthIndicatorUnhealthy(jobName, "Error when processing attachment");
			LOG.error("Error when processing attachment", e);
		}

	}

	void processAttachmentData(final MessageAttachmentEntity messageAttachment, final AttachmentEntity attachmentEntity) {
		try {
			final var data = emailReaderClient.getAttachment(messageAttachment.getMunicipalityId(), attachmentEntity.getId());
			messageAttachment.setAttachmentData(messageMapper.toMessageAttachmentData(data));
			attachmentEntity.setFile(messageMapper.toContentString(data));
			messageAttachmentRepository.saveAndFlush(messageAttachment);
			attachmentRepository.saveAndFlush(attachmentEntity);
		} catch (final Exception e) {
			dept44HealthUtility.setHealthIndicatorUnhealthy(jobName, "Error when processing attachment data");
			LOG.error("Error when processing attachment data", e);
		}
	}

	void deleteMail(final Email email) {
		try {
			emailReaderClient.deleteEmail(emailReaderProperties.municipalityId(), email.getId());
		} catch (final Exception e) {
			dept44HealthUtility.setHealthIndicatorUnhealthy(jobName, "Error when deleting email");
			LOG.error("Error when deleting email with ID: {}", email.getId(), e);
		}
	}
}
