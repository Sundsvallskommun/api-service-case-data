package se.sundsvall.casedata.service.scheduler.webmessagecollector;

import static java.util.Collections.emptyMap;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNotification;

import generated.se.sundsvall.webmessagecollector.MessageDTO;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import se.sundsvall.casedata.integration.db.AttachmentRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.MessageAttachmentRepository;
import se.sundsvall.casedata.integration.db.MessageRepository;
import se.sundsvall.casedata.integration.db.model.MessageEntity;
import se.sundsvall.casedata.integration.webmessagecollector.WebMessageCollectorClient;
import se.sundsvall.casedata.integration.webmessagecollector.configuration.WebMessageCollectorProperties;
import se.sundsvall.casedata.service.NotificationService;
import se.sundsvall.casedata.service.scheduler.MessageMapper;

@Component
public class WebMessageCollectorWorker {

	private static final String DESCRIPTION = "Meddelande mottaget";
	private static final String TYPE = "UPDATE";

	private final MessageRepository messageRepository;

	private final WebMessageCollectorClient webMessageCollectorClient;

	private final MessageAttachmentRepository messageAttachmentRepository;

	private final ErrandRepository errandRepository;

	private final AttachmentRepository attachmentRepository;

	private final MessageMapper messageMapper;

	private final WebMessageCollectorProperties webMessageCollectorProperties;

	private final NotificationService notificationService;

	public WebMessageCollectorWorker(final MessageRepository messageRepository, final WebMessageCollectorClient webMessageCollectorClient, final MessageAttachmentRepository messageAttachmentRepository, final ErrandRepository errandRepository,
		final AttachmentRepository attachmentRepository, final MessageMapper messageMapper,
		final WebMessageCollectorProperties webMessageCollectorProperties, final NotificationService notificationService) {
		this.messageRepository = messageRepository;
		this.webMessageCollectorClient = webMessageCollectorClient;
		this.messageAttachmentRepository = messageAttachmentRepository;
		this.errandRepository = errandRepository;
		this.attachmentRepository = attachmentRepository;
		this.messageMapper = messageMapper;
		this.webMessageCollectorProperties = webMessageCollectorProperties;
		this.notificationService = notificationService;
	}

	public void getAndProcessMessages() {

		getMessages().forEach((municipalityId, messages) -> {
			final var handledIds = messages.stream()
				.map(message -> {
					processMessage(message)
						.ifPresent(processedMessage -> message.getAttachments()
							.forEach(messageAttachment -> processAttachment(messageAttachment, processedMessage.getMessageId(), processedMessage.getErrandNumber(), processedMessage.getMunicipalityId(), processedMessage.getNamespace())));
					return message.getId();
				})
				.toList();

			deleteMessages(municipalityId, handledIds);
		});
	}

	private Optional<MessageEntity> processMessage(final MessageDTO message) {
		return errandRepository.findByExternalCaseId(message.getExternalCaseId()).map(errand -> {
			final var errandNumber = errand.getErrandNumber();
			final var municipalityId = errand.getMunicipalityId();
			final var namespace = errand.getNamespace();
			final var entity = messageMapper.toMessageEntity(errandNumber, message, municipalityId, namespace);
			notificationService.createNotification(municipalityId, namespace, toNotification(errand, TYPE, DESCRIPTION));
			return messageRepository.saveAndFlush(entity);
		});
	}

	private void processAttachment(final generated.se.sundsvall.webmessagecollector.MessageAttachment attachment, final String messageId, final String errandNumber, final String municipalityId, final String namespace) {
		final var attachmentId = attachment.getAttachmentId();
		// Map the attachment
		final var messageAttachment = messageMapper.toAttachmentEntity(attachment, messageId, municipalityId, namespace);
		// Fetch the data
		final var data = webMessageCollectorClient.getAttachment(municipalityId, attachmentId);
		messageAttachment.setAttachmentData(messageMapper.toMessageAttachmentData(data));
		// Save the attachment
		messageAttachmentRepository.saveAndFlush(messageAttachment);
		attachmentRepository.saveAndFlush(messageMapper.toAttachmentEntity(messageAttachment).withErrandNumber(errandNumber));
	}

	private Map<String, List<MessageDTO>> getMessages() {
		return Optional.ofNullable(webMessageCollectorProperties.familyIds())
			.orElse(emptyMap())
			.entrySet().stream()
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				municipalityIdEntry -> municipalityIdEntry.getValue().entrySet().stream()
					.flatMap(instanceEntry -> instanceEntry.getValue().stream()
						.flatMap(familyId -> webMessageCollectorClient.getMessages(municipalityIdEntry.getKey(), familyId, instanceEntry.getKey()).stream()))
					.toList()));
	}

	private void deleteMessages(final String municipalityId, final List<Integer> ids) {
		webMessageCollectorClient.deleteMessages(municipalityId, ids);
	}

}
