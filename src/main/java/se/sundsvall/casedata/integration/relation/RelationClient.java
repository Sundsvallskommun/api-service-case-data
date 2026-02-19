package se.sundsvall.casedata.integration.relation;

import generated.se.sundsvall.relation.Relation;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.sundsvall.casedata.integration.relation.configuration.RelationConfiguration;

import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.casedata.integration.relation.configuration.RelationConfiguration.CLIENT_ID;

@FeignClient(
	name = CLIENT_ID,
	url = "${integration.relation.base-url}",
	configuration = RelationConfiguration.class,
	dismiss404 = true)
@CircuitBreaker(name = CLIENT_ID)
public interface RelationClient {

	/**
	 * Get a relation
	 *
	 * @param  municipalityId the municipalityId of the relation
	 * @param  id             the id of the relation to get
	 * @return                the Relation
	 */
	@GetMapping(path = "/{municipalityId}/relations/{id}", produces = APPLICATION_JSON_VALUE)
	ResponseEntity<Relation> getRelation(
		@PathVariable final String municipalityId,
		@PathVariable final String id);

	/**
	 * Create a relation
	 */
	@PostMapping(path = "/{municipalityId}/relations", consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	ResponseEntity<Void> createRelation(
		@PathVariable final String municipalityId,
		@RequestBody final Relation relation);
}
