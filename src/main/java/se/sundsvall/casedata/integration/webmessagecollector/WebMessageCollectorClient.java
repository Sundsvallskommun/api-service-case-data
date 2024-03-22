package se.sundsvall.casedata.integration.webmessagecollector;

import static se.sundsvall.casedata.integration.webmessagecollector.configuration.WebMessageCollectorConfiguration.CLIENT_ID;

import java.io.InputStream;
import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import se.sundsvall.casedata.integration.webmessagecollector.configuration.WebMessageCollectorConfiguration;

import generated.se.sundsvall.webmessagecollector.MessageDTO;

@FeignClient(
	name = CLIENT_ID,
	url = "${integration.web-message-collector.base-url}",
	configuration = WebMessageCollectorConfiguration.class
)
public interface WebMessageCollectorClient {

	@GetMapping("/messages")
	List<MessageDTO> getMessages(@RequestParam(name = "familyid") String familyId);

	@DeleteMapping("/messages")
	void deleteMessages(List<Integer> ids);

	@GetMapping("/messages/attachments/{attachmentId}")
	InputStream getAttachment(@PathVariable(name = "attachmentId") int attachmentId);

	@DeleteMapping
	void deleteAttachment(@PathVariable(name = "attachmentId") int attachmentId);

}
