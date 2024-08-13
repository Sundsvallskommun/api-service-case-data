package se.sundsvall.casedata.integration.emailreader;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import se.sundsvall.casedata.integration.emailreader.configuration.EmailReaderConfiguration;

import generated.se.sundsvall.emailreader.Email;
import io.swagger.v3.oas.annotations.Parameter;

@FeignClient(
	name = EmailReaderConfiguration.CLIENT_ID,
	url = "${integration.email-reader.base-url}",
	configuration = EmailReaderConfiguration.class
)
public interface EmailReaderClient {

	@GetMapping("/{municipalityId}/email/{namespace}")
	List<Email> getEmail(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281")
		@PathVariable("municipalityId") final String municipalityId,
		@Parameter(name = "namespace", description = "A specific namespace", example = "CONTACTCENTER")
		@PathVariable("namespace") final String namespace);

	@DeleteMapping("/{municipalityId}/email/{id}")
	void deleteEmail(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281")
		@PathVariable("municipalityId") final String municipalityId,
		@Parameter(name = "id", description = "Email message ID", example = "81471222-5798-11e9-ae24-57fa13b361e1")
		@PathVariable("id") final String id);

}
