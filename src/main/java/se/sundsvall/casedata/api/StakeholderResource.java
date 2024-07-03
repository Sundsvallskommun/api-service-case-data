package se.sundsvall.casedata.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Validated
@RequestMapping("/{municipalityId}/stakeholders")
@Tag(name = "Stakeholders", description = "Stakeholder operations")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {Problem.class, ConstraintViolationProblem.class})))
@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class StakeholderResource {

	private final StakeholderService stakeholderService;

	StakeholderResource(final StakeholderService stakeholderService) {
		this.stakeholderService = stakeholderService;
	}

	@Operation(description = "Get stakeholder by ID.")
	@GetMapping(path = "/{stakeholderId}", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<StakeholderDTO> getStakeholders(
		@PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@PathVariable(name = "stakeholderId") final Long stakeholderId) {

		return ok(stakeholderService.findByIdAndMunicipalityId(stakeholderId, municipalityId));
	}

	@GetMapping(produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<List<StakeholderDTO>> getStakeholders(
		@PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@RequestParam(required = false) final Optional<String> stakeholderRole) {

		return stakeholderRole.map(role -> ok(stakeholderService.findStakeholdersByRoleAndMunicipalityId(role, municipalityId)))
			.orElseGet(() -> ok(stakeholderService.findAllStakeholdersByMunicipalityId(municipalityId)));
	}

	@Operation(description = "Update stakeholder.")
	@PatchMapping(path = "/{stakeholderId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_PROBLEM_JSON_VALUE})
	@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<Void> patchStakeholder(
		@PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@PathVariable(name = "stakeholderId") final Long stakeholderId,
		@RequestBody @Valid final StakeholderDTO stakeholderDTO) {

		stakeholderService.patch(stakeholderId, municipalityId, stakeholderDTO);
		return noContent().build();
	}

	@Operation(description = "Replace stakeholder.")
	@PutMapping(path = "/{stakeholderId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_PROBLEM_JSON_VALUE})
	@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<Void> putStakeholder(
		@PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@PathVariable(name = "stakeholderId") final Long stakeholderId,
		@RequestBody @Valid final StakeholderDTO stakeholderDTO) {

		stakeholderService.put(stakeholderId, municipalityId, stakeholderDTO);
		return noContent().build();
	}

}
