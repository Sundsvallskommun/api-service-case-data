package se.sundsvall.casedata.integration.messageexchange;

import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static se.sundsvall.casedata.integration.messageexchange.configuration.MessageExchangeConfiguration.CLIENT_ID;

import generated.se.sundsvall.messageexchange.Conversation;
import generated.se.sundsvall.messageexchange.Message;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.casedata.integration.messageexchange.configuration.MessageExchangeConfiguration;

@FeignClient(
	name = CLIENT_ID,
	url = "${integration.message-exchange.base-url}",
	configuration = MessageExchangeConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface MessageExchangeClient {

	@GetMapping(path = "/{municipalityId}/{namespace}/conversations", produces = APPLICATION_JSON_VALUE)
	ResponseEntity<Page<Conversation>> getConversations(
		@PathVariable("municipalityId") final String municipalityId,
		@PathVariable("namespace") final String namespace,
		@ParameterObject final Pageable pageable);

	@GetMapping(path = "/{municipalityId}/{namespace}/conversations/{conversationId}", produces = APPLICATION_JSON_VALUE)
	ResponseEntity<Conversation> getConversation(
		@PathVariable("municipalityId") final String municipalityId,
		@PathVariable("namespace") final String namespace,
		@PathVariable("conversationId") final String conversationId);

	@PostMapping(path = "/{municipalityId}/{namespace}/conversations", consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	ResponseEntity<Void> createConversation(
		@PathVariable("municipalityId") final String municipalityId,
		@PathVariable("namespace") final String namespace,
		@RequestBody final Conversation conversation);

	@PatchMapping(path = "/{municipalityId}/{namespace}/conversations/{conversationId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	ResponseEntity<Conversation> updateConversation(
		@PathVariable("municipalityId") final String municipalityId,
		@PathVariable("namespace") final String namespace,
		@PathVariable("conversationId") final String conversationId,
		@RequestBody final Conversation conversation);

	@DeleteMapping(path = "/{municipalityId}/{namespace}/conversations/{conversationId}", produces = APPLICATION_JSON_VALUE)
	ResponseEntity<Void> deleteConversation(
		@PathVariable("municipalityId") final String municipalityId,
		@PathVariable("namespace") final String namespace,
		@PathVariable("conversationId") final String conversationId);

	@PostMapping(path = "/{municipalityId}/{namespace}/conversations/{conversationId}/messages", consumes = MULTIPART_FORM_DATA_VALUE, produces = ALL_VALUE)
	ResponseEntity<Void> createMessage(
		@PathVariable("municipalityId") final String municipalityId,
		@PathVariable("namespace") final String namespace,
		@PathVariable("conversationId") final String conversationId,
		@RequestPart("message") final Message message,
		@RequestPart("attachments") final List<MultipartFile> attachments);

	@GetMapping(path = "/{municipalityId}/{namespace}/conversations/{conversationId}/messages", produces = APPLICATION_JSON_VALUE)
	ResponseEntity<Page<Message>> getMessages(
		@PathVariable("municipalityId") final String municipalityId,
		@PathVariable("namespace") final String namespace,
		@PathVariable("conversationId") final String conversationId,
		@ParameterObject final Pageable pageable);

	@DeleteMapping(path = "/{municipalityId}/{namespace}/conversations/{conversationId}/messages/{messageId}", produces = ALL_VALUE)
	ResponseEntity<Void> deleteMessage(
		@PathVariable("municipalityId") final String municipalityId,
		@PathVariable("namespace") final String namespace,
		@PathVariable("conversationId") final String conversationId,
		@PathVariable("messageId") final String messageId);

	@GetMapping(path = "/{municipalityId}/{namespace}/conversations/{conversationId}/messages/{messageId}/attachments/{attachmentId}", produces = ALL_VALUE)
	ResponseEntity<InputStreamResource> readErrandAttachment(
		@PathVariable("municipalityId") final String municipalityId,
		@PathVariable("namespace") final String namespace,
		@PathVariable("conversationId") final String conversationId,
		@PathVariable("messageId") final String messageId,
		@PathVariable("attachmentId") final String attachmentId);

}
