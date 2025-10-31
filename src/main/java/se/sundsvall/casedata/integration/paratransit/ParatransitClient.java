package se.sundsvall.casedata.integration.paratransit;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.casedata.integration.paratransit.configuration.ParatransitConfiguration.CLIENT_ID;

import generated.se.sundsvall.paratransit.StartProcessResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import se.sundsvall.casedata.integration.paratransit.configuration.ParatransitConfiguration;

@FeignClient(
	name = CLIENT_ID,
	url = "${integration.paratransit.base-url}",
	configuration = ParatransitConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface ParatransitClient {

	@Retry(name = CLIENT_ID)
	@PostMapping(path = "/{municipalityId}/{namespace}/process/start/{errandId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	StartProcessResponse startProcess(
		@PathVariable(name = "municipalityId") final String municipalityId,
		@PathVariable(name = "namespace") final String namespace,
		@PathVariable(name = "errandId") final Long errandId);

	@Retry(name = CLIENT_ID)
	@PostMapping(path = "/{municipalityId}/{namespace}/process/update/{processInstanceId}", consumes = APPLICATION_JSON_VALUE)
	ResponseEntity<Void> updateProcess(
		@PathVariable(name = "municipalityId") final String municipalityId,
		@PathVariable(name = "namespace") final String namespace,
		@PathVariable(name = "processInstanceId") final String processInstanceId);
}
