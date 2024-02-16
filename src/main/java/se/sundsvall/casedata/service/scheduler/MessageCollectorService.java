package se.sundsvall.casedata.service.scheduler;

import static java.util.Objects.nonNull;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import generated.se.sundsvall.webmessagecollector.MessageDTO;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.MessageRepository;
import se.sundsvall.casedata.integration.webmessagecollector.WebMessageCollectorClient;
import se.sundsvall.casedata.integration.webmessagecollector.configuration.WebMessageCollectorProperties;

@Component
@ConditionalOnProperty(prefix = "scheduler.message-collector", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MessageCollectorService {

	private final MessageRepository messageRepository;
	private final WebMessageCollectorClient webMessageCollectorClient;
	private final ErrandRepository errandRepository;
	private final MessageMapper messageMapper;
	private final WebMessageCollectorProperties webMessageCollectorProperties;

	public MessageCollectorService(final MessageRepository messageRepository, final WebMessageCollectorClient webMessageCollectorClient, final ErrandRepository errandRepository, final MessageMapper messageMapper,
		final WebMessageCollectorProperties webMessageCollectorProperties) {
		this.messageRepository = messageRepository;
		this.webMessageCollectorClient = webMessageCollectorClient;
		this.errandRepository = errandRepository;
		this.messageMapper = messageMapper;
		this.webMessageCollectorProperties = webMessageCollectorProperties;
	}

	@Scheduled(initialDelayString = "${scheduler.message-collector.initialDelay}", fixedRateString = "${scheduler.message-collector.fixedRate}")
	@SchedulerLock(name = "message-collector", lockAtMostFor = "${scheduler.message-collector.shedlock-lock-at-most-for}")
	void getAndProcessMessages() {

		final var handledIds = getMessages().stream()
			.map(messageDTO -> {
				final var result = errandRepository.findByExternalCaseId(messageDTO.getExternalCaseId());
				if (nonNull(result)) {
					final var errandNumber = result.getErrandNumber();
					messageRepository.save(messageMapper.toMessageEntity(errandNumber, messageDTO));
				}
				return messageDTO.getId();
			})
			.toList();
		deleteMessages(handledIds);
	}

	private List<MessageDTO> getMessages() {
		return webMessageCollectorProperties.familyIds().stream()
			.flatMap(familyId -> webMessageCollectorClient.getMessages(familyId).stream()).toList();

	}

	private void deleteMessages(final List<Integer> ids) {
		webMessageCollectorClient.deleteMessages(ids);
	}

}
