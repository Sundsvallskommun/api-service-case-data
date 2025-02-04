package se.sundsvall.casedata.api;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;
import static se.sundsvall.casedata.service.util.Constants.NAMESPACE_REGEXP;
import static se.sundsvall.casedata.service.util.Constants.NAMESPACE_VALIDATION_MESSAGE;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.casedata.api.model.Facility;
import se.sundsvall.casedata.service.FacilityService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;

@RestController
@Validated
@RequestMapping("/{municipalityId}/{namespace}/errands/{errandId}/facilities")
@Tag(name = "Facilities", description = "Errand facilities operations")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class FacilityResource {

	private final FacilityService facilityService;

	FacilityResource(final FacilityService facilityService) {
		this.facilityService = facilityService;
	}

	@PostMapping(consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	@Operation(description = "Create errand facility", responses = {
		@ApiResponse(responseCode = "201", description = "Created - Successful operation", headers = @Header(name = LOCATION, schema = @Schema(type = "string")), useReturnTypeSchema = true)
	})
	ResponseEntity<Void> postErrandFacility(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "my.namespace") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "errandId", description = "Errand ID", example = "123") @PathVariable(name = "errandId") final Long errandId,
		@RequestBody @Valid final Facility facility) {

		final Facility result = facilityService.create(errandId, municipalityId, namespace, facility);
		return created(
			fromPath("/{municipalityId}/{namespace}/errands/{id}/facilities/{facilityId}")
				.buildAndExpand(municipalityId, namespace, errandId, result.getId())
				.toUri())
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@GetMapping(produces = APPLICATION_JSON_VALUE)
	@Operation(description = "Get all facilities on errand", responses = {
		@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
	})
	ResponseEntity<List<Facility>> getFacilities(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "my.namespace") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "errandId", description = "Errand ID", example = "123") @PathVariable(name = "errandId") final Long errandId) {

		return ok(facilityService.findFacilities(errandId, municipalityId, namespace));
	}

	@GetMapping(path = "/{facilityId}", produces = APPLICATION_JSON_VALUE)
	@Operation(description = "Get a specific facility on errand", responses = {
		@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
	})
	ResponseEntity<Facility> getFacility(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "my.namespace") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "errandId", description = "Errand ID", example = "123") @PathVariable(name = "errandId") final Long errandId,
		@PathVariable(name = "facilityId") final Long facilityId) {

		return ok(facilityService.findFacility(errandId, facilityId, municipalityId, namespace));
	}

	@PatchMapping(path = "/{facilityId}", consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	@Operation(description = "Update errand facility by facility id", responses = {
		@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	})
	ResponseEntity<Void> patchErrandFacility(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "my.namespace") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "errandId", description = "Errand ID", example = "123") @PathVariable(name = "errandId") final Long errandId,
		@PathVariable(name = "facilityId") final Long facilityId,
		@RequestBody @Valid final Facility facility) {

		facilityService.update(errandId, municipalityId, namespace, facilityId, facility);
		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@PutMapping(consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	@Operation(description = "Add/replace facility on errand", responses = {
		@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	})
	@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<Void> putFacilitiesOnErrand(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "my.namespace") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "errandId", description = "Errand ID", example = "123") @PathVariable(name = "errandId") final Long errandId,
		@RequestBody @Valid final List<Facility> facilities) {

		facilityService.replaceFacilities(errandId, municipalityId, namespace, facilities);
		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@DeleteMapping(path = "/{facilityId}", produces = ALL_VALUE)
	@Operation(description = "Delete facility on errand", responses = {
		@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	})
	ResponseEntity<Void> deleteFacility(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "my.namespace") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "errandId", description = "Errand ID", example = "123") @PathVariable(name = "errandId") final Long errandId,
		@PathVariable(name = "facilityId") final Long facilityId) {

		facilityService.delete(errandId, municipalityId, namespace, facilityId);
		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}
}
