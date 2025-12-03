package se.sundsvall.casedata.integration.webmessagecollector;

import static se.sundsvall.casedata.integration.webmessagecollector.configuration.WebMessageCollectorConfiguration.CLIENT_ID;

import generated.se.sundsvall.webmessagecollector.MessageDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import se.sundsvall.casedata.integration.webmessagecollector.configuration.WebMessageCollectorConfiguration;

@FeignClient(
	name = CLIENT_ID,
	url = "${integration.web-message-collector.base-url}",
	configuration = WebMessageCollectorConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface WebMessageCollectorClient {

	@GetMapping("/{municipalityId}/messages/{familyid}/{instance}")
	List<MessageDTO> getMessages(
		@PathVariable String municipalityId,
		@PathVariable String familyid,
		@PathVariable String instance);

	@DeleteMapping("/{municipalityId}/messages")
	void deleteMessages(@PathVariable String municipalityId, List<Integer> ids);

	@GetMapping("/{municipalityId}/messages/attachments/{attachmentId}")
	byte[] getAttachment(
		@PathVariable String municipalityId,
		@PathVariable int attachmentId);
}
