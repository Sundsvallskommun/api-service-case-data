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

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;

import org.springframework.http.MediaType;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Validated
@RequestMapping("/{municipalityId}/{namespace}/errands/{errandId}/facilities")
@Tag(name = "Facilities", description = "Errand facilities operations")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {Problem.class, ConstraintViolationProblem.class})))
@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class FacilityResource {

	private final FacilityService facilityService;

	FacilityResource(final FacilityService facilityService) {this.facilityService = facilityService;}

	@Operation(description = "Create errand facility")
	@PostMapping(consumes = APPLICATION_JSON_VALUE, produces = {ALL_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	@ApiResponse(responseCode = "201", description = "Created - Successful operation", headers = @Header(name = LOCATION, schema = @Schema(type = "string")), useReturnTypeSchema = true)
	ResponseEntity<Void> postErrandFacility(
		@PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "my.namespace") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@PathVariable(name = "errandId") final Long errandId,
		@RequestBody @Valid final Facility facility) {

		final Facility result = facilityService.createFacilityOnErrand(errandId, municipalityId, namespace, facility);
		return created(
			fromPath("/{municipalityId}/{namespace}/errands/{id}/facilities/{facilityId}")
				.buildAndExpand(municipalityId, namespace, errandId, result.getId())
				.toUri())
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@Operation(description = "Get all facilities on errand")
	@GetMapping(produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<List<Facility>> getFacilities(
		@PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "my.namespace") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@PathVariable(name = "errandId") final Long errandId) {

		return ok(facilityService.findFacilitiesOnErrand(errandId, municipalityId, namespace));
	}

	@Operation(description = "Get a specific facility on errand")
	@GetMapping(path = "/{facilityId}", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<Facility> getFacility(
		@PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "my.namespace") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@PathVariable(name = "errandId") final Long errandId,
		@PathVariable(name = "facilityId") final Long facilityId) {

		return ok(facilityService.findFacilityOnErrand(errandId, facilityId, municipalityId, namespace));
	}

	@Operation(description = "Update errand facility by facility id")
	@PatchMapping(path = "/{facilityId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_PROBLEM_JSON_VALUE})
	@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<Void> patchErrandFacility(
		@PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "my.namespace") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@PathVariable(name = "errandId") final Long errandId,
		@PathVariable(name = "facilityId") final Long facilityId,
		@RequestBody @Valid final Facility facility) {

		facilityService.updateFacilityOnErrand(errandId, municipalityId, namespace, facilityId, facility);
		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@Operation(description = "Add/replace facility on errand")
	@PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_PROBLEM_JSON_VALUE})
	@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<Void> putFacilitiesOnErrand(
		@PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "my.namespace") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@PathVariable(name = "errandId") final Long errandId,
		@RequestBody @Valid final List<Facility> facilities) {

		facilityService.replaceFacilitiesOnErrand(errandId, municipalityId, namespace, facilities);
		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@Operation(description = "Delete facility on errand")
	@DeleteMapping(path = "/{facilityId}", produces = {APPLICATION_PROBLEM_JSON_VALUE})
	@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<Void> deleteFacility(
		@PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "my.namespace") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@PathVariable(name = "errandId") final Long errandId,
		@PathVariable(name = "facilityId") final Long facilityId) {

		facilityService.deleteFacilityOnErrand(errandId, municipalityId, namespace, facilityId);
		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

}
