package se.sundsvall.casedata.integration.parkingpermit;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.casedata.integration.parkingpermit.configuration.ParkingPermitConfiguration.CLIENT_ID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import se.sundsvall.casedata.integration.parkingpermit.configuration.ParkingPermitConfiguration;

import generated.se.sundsvall.parkingpermit.StartProcessResponse;
import io.github.resilience4j.retry.annotation.Retry;

@FeignClient(
	name = CLIENT_ID,
	url = "${integration.parkingpermit.base-url}",
	configuration = ParkingPermitConfiguration.class
)
public interface ParkingPermitClient {

	@Retry(name = CLIENT_ID)
	@PostMapping(path = "/{municipalityId}/process/start/{errandId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	StartProcessResponse startProcess(
		@PathVariable(name = "municipalityId") final String municipalityId,
		@PathVariable(name = "errandId") final Long errandId);

	@Retry(name = CLIENT_ID)
	@PostMapping(path = "/{municipalityId}/process/update/{processInstanceId}", consumes = APPLICATION_JSON_VALUE)
	ResponseEntity<Void> updateProcess(
		@PathVariable(name = "municipalityId") final String municipalityId,
		@PathVariable(name = "processInstanceId") final String processInstanceId);

}
