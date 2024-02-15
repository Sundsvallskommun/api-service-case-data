package se.sundsvall.casedata.integration.emailreader;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import se.sundsvall.casedata.integration.emailreader.configuration.EmailReaderConfiguration;

import generated.se.sundsvall.emailreader.Email;

@FeignClient(
	name = EmailReaderConfiguration.CLIENT_ID,
	url = "${integration.email-reader.base-url}",
	configuration = EmailReaderConfiguration.class
)
public interface EmailReaderClient {

	@GetMapping("/email")
	List<Email> getEmail(@RequestParam("municipalityId") final String municipalityId, @RequestParam("namespace") final String namespace);

	@DeleteMapping("/email/{id}")
	void deleteEmail(@PathVariable String id);

}
