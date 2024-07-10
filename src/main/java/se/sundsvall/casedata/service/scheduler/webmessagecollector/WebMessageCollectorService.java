package se.sundsvall.casedata.service.scheduler.webmessagecollector;

import static java.util.Collections.emptyMap;

import java.util.List;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import se.sundsvall.casedata.integration.db.AttachmentRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.MessageAttachmentRepository;
import se.sundsvall.casedata.integration.db.MessageRepository;
import se.sundsvall.casedata.integration.db.model.Message;
import se.sundsvall.casedata.integration.webmessagecollector.WebMessageCollectorClient;
import se.sundsvall.casedata.integration.webmessagecollector.configuration.WebMessageCollectorProperties;
import se.sundsvall.casedata.service.scheduler.MessageMapper;

import generated.se.sundsvall.webmessagecollector.MessageDTO;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Component
@ConditionalOnProperty(prefix = "scheduler.message-collector", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WebMessageCollectorService {


	private final MessageRepository messageRepository;

	private final WebMessageCollectorClient webMessageCollectorClient;

	private final MessageAttachmentRepository messageAttachmentRepository;

	private final ErrandRepository errandRepository;

	private final AttachmentRepository attachmentRepository;

	private final MessageMapper messageMapper;

	private final WebMessageCollectorProperties webMessageCollectorProperties;

	public WebMessageCollectorService(final MessageRepository messageRepository, final WebMessageCollectorClient webMessageCollectorClient, final MessageAttachmentRepository messageAttachmentRepository, final ErrandRepository errandRepository, final AttachmentRepository attachmentRepository, final MessageMapper messageMapper,
		final WebMessageCollectorProperties webMessageCollectorProperties) {
		this.messageRepository = messageRepository;
		this.webMessageCollectorClient = webMessageCollectorClient;
		this.messageAttachmentRepository = messageAttachmentRepository;
		this.errandRepository = errandRepository;
		this.attachmentRepository = attachmentRepository;
		this.messageMapper = messageMapper;
		this.webMessageCollectorProperties = webMessageCollectorProperties;
	}

	@Scheduled(initialDelayString = "${scheduler.message-collector.initialDelay}", fixedRateString = "${scheduler.message-collector.fixedRate}")
	@SchedulerLock(name = "message-collector", lockAtMostFor = "${scheduler.message-collector.shedlock-lock-at-most-for}")
	void getAndProcessMessages() {

		final var handledIds = getMessages().stream()
			.map(messageDTO -> {
				processMessage(messageDTO).ifPresent(processedMessage -> {
					for (final var messageAttachment : messageDTO.getAttachments()) {
						processAttachment(messageAttachment, processedMessage.getMessageID(), processedMessage.getErrandNumber());
					}
				});
				return messageDTO.getId();
			})
			.toList();

		deleteMessages(handledIds);
	}

	private Optional<Message> processMessage(final MessageDTO messageDTO) {
		return errandRepository.findByExternalCaseId(messageDTO.getExternalCaseId()).map(errand -> {
			final var errandNumber = errand.getErrandNumber();
			final var municipalityId = errand.getMunicipalityId();
			final var message = messageMapper.toMessageEntity(errandNumber, messageDTO, municipalityId);
			return messageRepository.saveAndFlush(message);
		});
	}

	private void processAttachment(final generated.se.sundsvall.webmessagecollector.MessageAttachment attachment, final String messageId, final String errandNumber) {
		final var attachmentId = attachment.getAttachmentId();
		// Map the attachment
		final var messageAttachment = messageMapper.toAttachmentEntity(attachment, messageId);
		// Fetch the data
		final var data = webMessageCollectorClient.getAttachment(attachmentId);
		messageAttachment.setAttachmentData(messageMapper.toMessageAttachmentData(data));
		// Save the attachment
		messageAttachmentRepository.saveAndFlush(messageAttachment);
		attachmentRepository.saveAndFlush(messageMapper.toAttachment(messageAttachment).withErrandNumber(errandNumber));
	}

	private List<MessageDTO> getMessages() {

		return Optional.ofNullable(webMessageCollectorProperties.familyIds())
			.orElse(emptyMap())
			.entrySet().stream()
			.flatMap(entry -> entry.getValue().stream()
				.flatMap(familyId -> webMessageCollectorClient.getMessages(familyId, entry.getKey())
					.stream()))
			.toList();
	}

	private void deleteMessages(final List<Integer> ids) {
		webMessageCollectorClient.deleteMessages(ids);
	}

}
