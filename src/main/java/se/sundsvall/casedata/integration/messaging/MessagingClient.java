package se.sundsvall.casedata.integration.messaging;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.casedata.integration.messaging.configuration.MessagingConfiguration.CLIENT_ID;

import generated.se.sundsvall.messaging.MessageRequest;
import generated.se.sundsvall.messaging.MessageResult;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.sundsvall.casedata.integration.messaging.configuration.MessagingConfiguration;

@FeignClient(name = CLIENT_ID, url = "${integration.messaging.base-url}", configuration = MessagingConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface MessagingClient {

	/**
	 * Send a single e-mail
	 *
	 * @param  municipalityId the id of the municipality to send the email to
	 * @param  messageRequest containing message information
	 * @return                response containing id and delivery results for sent message
	 */
	@PostMapping(path = "/{municipalityId}/messages", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
	MessageResult sendMessage(
		@PathVariable("municipalityId") final String municipalityId,
		@RequestBody final MessageRequest messageRequest);
}
