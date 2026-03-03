package se.sundsvall.casedata.integration.parkingpermit;

import generated.se.sundsvall.parkingpermit.StartProcessResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import se.sundsvall.casedata.integration.parkingpermit.configuration.ParkingPermitConfiguration;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.casedata.integration.parkingpermit.configuration.ParkingPermitConfiguration.CLIENT_ID;

@FeignClient(
	name = CLIENT_ID,
	url = "${integration.parkingpermit.base-url}",
	configuration = ParkingPermitConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface ParkingPermitClient {

	@PostMapping(path = "/{municipalityId}/{namespace}/process/start/{errandId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	StartProcessResponse startProcess(
		@PathVariable final String municipalityId,
		@PathVariable final String namespace,
		@PathVariable final Long errandId);

	@PostMapping(path = "/{municipalityId}/{namespace}/process/update/{processInstanceId}", consumes = APPLICATION_JSON_VALUE)
	ResponseEntity<Void> updateProcess(
		@PathVariable final String municipalityId,
		@PathVariable final String namespace,
		@PathVariable final String processInstanceId);
}
