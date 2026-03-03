package se.sundsvall.casedata.integration.emailreader;

import generated.se.sundsvall.emailreader.Email;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import se.sundsvall.casedata.integration.emailreader.configuration.EmailReaderConfiguration;

import static se.sundsvall.casedata.integration.emailreader.configuration.EmailReaderConfiguration.CLIENT_ID;

@FeignClient(
	name = CLIENT_ID,
	url = "${integration.email-reader.base-url}",
	configuration = EmailReaderConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface EmailReaderClient {

	@GetMapping("/{municipalityId}/email/{namespace}")
	List<Email> getEmail(
		@PathVariable final String municipalityId,
		@PathVariable final String namespace);

	@GetMapping("/{municipalityId}/email/attachments/{attachmentId}")
	byte[] getAttachment(
		@PathVariable final String municipalityId,
		@PathVariable final Long attachmentId);

	@DeleteMapping("/{municipalityId}/email/{id}")
	void deleteEmail(
		@PathVariable final String municipalityId,
		@PathVariable final String id);
}
