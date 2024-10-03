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
import java.util.Optional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;

import se.sundsvall.casedata.api.model.StakeholderDTO;
import se.sundsvall.casedata.service.StakeholderService;
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
@RequestMapping("/{municipalityId}/{namespace}/errands/{errandId}/stakeholders")
@Tag(name = "Stakeholders", description = "Stakeholder operations")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {Problem.class, ConstraintViolationProblem.class})))
@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class StakeholderResource {

	private final StakeholderService stakeholderService;

	StakeholderResource(final StakeholderService stakeholderService) {
		this.stakeholderService = stakeholderService;
	}

	@Operation(description = "Get stakeholder on errand by stakeholder id.")
	@GetMapping(path = "/{stakeholderId}", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<StakeholderDTO> getStakeholders(
		@PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "my.namespace") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@PathVariable(name = "errandId") final Long errandId,
		@PathVariable(name = "stakeholderId") final Long stakeholderId) {

		return ok(stakeholderService.findByIdAndMunicipalityIdAndNamespace(stakeholderId, municipalityId, namespace));
	}

	@Operation(description = "Get all stakeholders on errand.")
	@GetMapping(produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<List<StakeholderDTO>> getStakeholders(
		@PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "my.namespace") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@PathVariable(name = "errandId") final Long errandId,
		@RequestParam(required = false) final Optional<String> stakeholderRole) {

		return stakeholderRole.map(role -> ok(stakeholderService.findStakeholdersByRoleAndMunicipalityIdAndNamespace(role, municipalityId, namespace)))
			.orElseGet(() -> ok(stakeholderService.findAllStakeholdersByMunicipalityIdAndNamespace(municipalityId, namespace)));
	}

	@Operation(description = "Update stakeholder on errand by stakeholder id.")
	@PatchMapping(path = "/{stakeholderId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_PROBLEM_JSON_VALUE})
	@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<Void> patchStakeholder(
		@PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "my.namespace") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@PathVariable(name = "errandId") final Long errandId,
		@PathVariable(name = "stakeholderId") final Long stakeholderId,
		@RequestBody @Valid final StakeholderDTO stakeholderDTO) {

		stakeholderService.patch(stakeholderId, municipalityId, namespace, stakeholderDTO);
		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@Operation(description = "Replace stakeholder on errand by stakeholder id.")
	@PutMapping(path = "/{stakeholderId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_PROBLEM_JSON_VALUE})
	@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<Void> putStakeholder(
		@PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "my.namespace") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@PathVariable(name = "stakeholderId") final Long stakeholderId,
		@PathVariable(name = "errandId") final Long errandId,
		@RequestBody @Valid final StakeholderDTO stakeholderDTO) {

		stakeholderService.put(stakeholderId, municipalityId, namespace, stakeholderDTO);
		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@Operation(description = "Create and add stakeholder to errand.")
	@ApiResponse(responseCode = "201", description = "Created - Successful operation", headers = @Header(name = LOCATION, description = "Location of the created resource."), useReturnTypeSchema = true)
	@PatchMapping(consumes = APPLICATION_JSON_VALUE, produces = {ALL_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	ResponseEntity<Void> patchErrandWithStakeholder(
		@PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "my.namespace") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@PathVariable(name = "errandId") final Long errandId,
		@RequestBody @Valid final StakeholderDTO stakeholderDTO) {

		final var stakeholder = stakeholderService.addStakeholderToErrand(errandId, municipalityId, namespace, stakeholderDTO);
		return created(fromPath("/{municipalityId}/{namespace}/stakeholders/{stakeholderId}").buildAndExpand(municipalityId, namespace, stakeholder.getId()).toUri())
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@Operation(description = "Replace stakeholders on errand.")
	@PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_PROBLEM_JSON_VALUE})
	@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<Void> putStakeholdersOnErrand(
		@PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "my.namespace") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@PathVariable(name = "errandId") final Long errandId,
		@RequestBody @Valid final List<StakeholderDTO> stakeholderDTOList) {

		stakeholderService.replaceStakeholdersOnErrand(errandId, municipalityId, namespace, stakeholderDTOList);
		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@Operation(description = "Delete stakeholder on errand by stakeholder id.")
	@DeleteMapping(path = "/{stakeholderId}", produces = {APPLICATION_PROBLEM_JSON_VALUE})
	@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<Void> deleteStakeholder(
		@PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "my.namespace") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@PathVariable(name = "errandId") final Long errandId,
		@PathVariable(name = "stakeholderId") final Long stakeholderId) {

		stakeholderService.deleteStakeholderOnErrand(errandId, municipalityId, namespace, stakeholderId);
		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

}
