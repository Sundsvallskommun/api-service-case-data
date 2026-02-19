package se.sundsvall.casedata.integration.eventlog;

import generated.se.sundsvall.eventlog.Event;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.sundsvall.casedata.integration.eventlog.configuration.EventlogConfiguration;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.casedata.integration.eventlog.configuration.EventlogConfiguration.CLIENT_ID;

@FeignClient(name = CLIENT_ID, url = "${integration.eventlog.base-url}", configuration = EventlogConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface EventlogClient {

	@PostMapping(path = "/{municipalityId}/{logKey}", consumes = APPLICATION_JSON_VALUE)
	void createEvent(
		@PathVariable final String municipalityId,
		@PathVariable final String logKey,
		@RequestBody final Event event);
}
