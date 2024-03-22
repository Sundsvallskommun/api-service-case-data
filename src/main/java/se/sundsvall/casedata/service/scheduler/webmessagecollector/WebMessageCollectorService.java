package se.sundsvall.casedata.service.scheduler.webmessagecollector;

import java.util.List;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.MessageAttachmentRepository;
import se.sundsvall.casedata.integration.db.MessageRepository;
import se.sundsvall.casedata.integration.db.model.Message;
import se.sundsvall.casedata.integration.db.model.MessageAttachment;
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

	private final MessageMapper messageMapper;

	private final WebMessageCollectorProperties webMessageCollectorProperties;

	public WebMessageCollectorService(final MessageRepository messageRepository, final WebMessageCollectorClient webMessageCollectorClient, final MessageAttachmentRepository messageAttachmentRepository, final ErrandRepository errandRepository, final MessageMapper messageMapper,
		final WebMessageCollectorProperties webMessageCollectorProperties) {
		this.messageRepository = messageRepository;
		this.webMessageCollectorClient = webMessageCollectorClient;
		this.messageAttachmentRepository = messageAttachmentRepository;
		this.errandRepository = errandRepository;
		this.messageMapper = messageMapper;
		this.webMessageCollectorProperties = webMessageCollectorProperties;
	}

	@Scheduled(initialDelayString = "${scheduler.message-collector.initialDelay}", fixedRateString = "${scheduler.message-collector.fixedRate}")
	@SchedulerLock(name = "message-collector", lockAtMostFor = "${scheduler.message-collector.shedlock-lock-at-most-for}")
	void getAndProcessMessages() {

		final var handledIds = getMessages().stream()
			.map(messageDTO -> {
				processMessage(messageDTO).ifPresent(processedMessage -> {
					for (final var messageAttachment : processedMessage.getAttachments()) {
						processAttachment(messageAttachment);
					}
				});
				return messageDTO.getId();
			})
			.toList();

		deleteMessages(handledIds);
	}

	private Optional<Message> processMessage(final MessageDTO messageDTO) {
		return errandRepository.findByExternalCaseId(messageDTO.getExternalCaseId()).map(result -> {
			final var errandNumber = result.getErrandNumber();
			final var message = messageMapper.toMessageEntity(errandNumber, messageDTO);
			return messageRepository.save(message);
		});
	}

	private void processAttachment(final MessageAttachment attachment) {
		final var result = webMessageCollectorClient.getAttachment(Integer.parseInt(attachment.getAttachmentID()));
		attachment.setAttachmentData(messageMapper.toMessageAttachmentData(result));
		messageAttachmentRepository.save(attachment);
	}


	private List<MessageDTO> getMessages() {
		return webMessageCollectorProperties.familyIds().stream()
			.flatMap(familyId -> webMessageCollectorClient.getMessages(familyId).stream()).toList();

	}

	private void deleteMessages(final List<Integer> ids) {
		webMessageCollectorClient.deleteMessages(ids);
	}

}
