package se.sundsvall.casedata.integration.messaging;

import generated.se.sundsvall.messaging.EmailRequest;
import generated.se.sundsvall.messaging.MessageRequest;
import generated.se.sundsvall.messaging.MessageResult;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.sundsvall.casedata.integration.messaging.configuration.MessagingConfiguration;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.casedata.integration.messaging.configuration.MessagingConfiguration.CLIENT_ID;

@FeignClient(name = CLIENT_ID, url = "${integration.messaging.base-url}", configuration = MessagingConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface MessagingClient {

	/**
	 * Send a message to a list of recipients as either sms or email.
	 *
	 * @param  municipalityId the id of the municipality to send the email to
	 * @param  messageRequest containing message information
	 * @return                response containing id and delivery results for sent message
	 */
	@PostMapping(path = "/{municipalityId}/messages", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
	MessageResult sendMessage(
		@PathVariable final String municipalityId,
		@RequestBody final MessageRequest messageRequest);

	/**
	 * Send a email to a single recipient.
	 *
	 * @param municipalityId the id of the municipality to send the email to
	 * @param emailRequest   containing email information
	 */
	@PostMapping(path = "/{municipalityId}/email", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
	MessageResult sendEmail(
		@PathVariable final String municipalityId,
		@RequestBody final EmailRequest emailRequest);
}
