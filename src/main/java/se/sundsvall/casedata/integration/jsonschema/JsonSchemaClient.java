package se.sundsvall.casedata.integration.jsonschema;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.sundsvall.casedata.integration.jsonschema.configuration.JsonSchemaConfiguration;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.casedata.integration.jsonschema.configuration.JsonSchemaConfiguration.CLIENT_ID;

@FeignClient(name = CLIENT_ID, url = "${integration.json-schema.url}", configuration = JsonSchemaConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface JsonSchemaClient {

	@PostMapping(path = "/{municipalityId}/schemas/{schemaId}/validation", consumes = APPLICATION_JSON_VALUE)
	void validateJson(
		@PathVariable final String municipalityId,
		@PathVariable final String schemaId,
		@RequestBody final JsonNode jsonData);
}
