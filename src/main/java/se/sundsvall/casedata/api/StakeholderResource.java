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
import java.util.Optional;
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
import se.sundsvall.casedata.api.model.Stakeholder;
import se.sundsvall.casedata.service.StakeholderService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;

@RestController
@Validated
@RequestMapping("/{municipalityId}/{namespace}/errands/{errandId}/stakeholders")
@Tag(name = "Stakeholders", description = "Stakeholder operations")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class StakeholderResource {

	private final StakeholderService stakeholderService;

	StakeholderResource(final StakeholderService stakeholderService) {
		this.stakeholderService = stakeholderService;
	}

	@GetMapping(path = "/{stakeholderId}", produces = APPLICATION_JSON_VALUE)
	@Operation(description = "Get stakeholder on errand by stakeholder id.", responses = {
		@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
	})
	ResponseEntity<Stakeholder> getStakeholderOnErrand(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "errandId", description = "Errand ID", example = "123") @PathVariable(name = "errandId") final Long errandId,
		@PathVariable(name = "stakeholderId") final Long stakeholderId) {

		return ok(stakeholderService.findStakeholder(errandId, stakeholderId, municipalityId, namespace));
	}

	@GetMapping(produces = APPLICATION_JSON_VALUE)
	@Operation(description = "Get all stakeholders on errand.", responses = {
		@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
	})
	ResponseEntity<List<Stakeholder>> getAllStakeholdersOnErrand(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "errandId", description = "Errand ID", example = "123") @PathVariable(name = "errandId") final Long errandId,
		@RequestParam(required = false) final Optional<String> stakeholderRole) {

		return stakeholderRole.map(role -> ok(stakeholderService.findStakeholdersByRole(errandId, role, municipalityId, namespace)))
			.orElseGet(() -> ok(stakeholderService.findStakeholders(errandId, municipalityId, namespace)));
	}

	@PatchMapping(path = "/{stakeholderId}", consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	@Operation(description = "Update stakeholder on errand by stakeholder id.", responses = {
		@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	})
	ResponseEntity<Void> updateStakeholderOnErrand(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "errandId", description = "Errand ID", example = "123") @PathVariable(name = "errandId") final Long errandId,
		@PathVariable(name = "stakeholderId") final Long stakeholderId,
		@RequestBody @Valid final Stakeholder stakeholder) {

		stakeholderService.update(errandId, stakeholderId, municipalityId, namespace, stakeholder);
		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@PutMapping(path = "/{stakeholderId}", consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	@Operation(description = "Replace stakeholder on errand by stakeholder id.", responses = {
		@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	})
	ResponseEntity<Void> replaceStakeholderOnErrand(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "errandId", description = "Errand ID", example = "123") @PathVariable(name = "errandId") final Long errandId,
		@PathVariable(name = "stakeholderId") final Long stakeholderId,
		@RequestBody @Valid final Stakeholder stakeholder) {

		stakeholderService.replaceOnErrand(errandId, stakeholderId, municipalityId, namespace, stakeholder);
		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@PatchMapping(consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	@Operation(description = "Create and add stakeholder to errand.", responses = {
		@ApiResponse(responseCode = "201", description = "Created - Successful operation", headers = @Header(name = LOCATION, description = "Location of the created resource.", schema = @Schema(type = "string")), useReturnTypeSchema = true)
	})
	ResponseEntity<Void> updateErrandWithStakeholder(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "errandId", description = "Errand ID", example = "123") @PathVariable(name = "errandId") final Long errandId,
		@RequestBody @Valid final Stakeholder stakeholder) {

		final var result = stakeholderService.addToErrand(errandId, municipalityId, namespace, stakeholder);
		return created(fromPath("/{municipalityId}/{namespace}/stakeholders/{stakeholderId}").buildAndExpand(municipalityId, namespace, result.getId()).toUri())
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@PutMapping(consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	@Operation(description = "Replace stakeholders on errand.", responses = {
		@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	})
	ResponseEntity<Void> replaceStakeholdersOnErrand(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "errandId", description = "Errand ID", example = "123") @PathVariable(name = "errandId") final Long errandId,
		@RequestBody @Valid final List<Stakeholder> stakeholderList) {

		stakeholderService.replaceOnErrand(errandId, municipalityId, namespace, stakeholderList);
		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@DeleteMapping(path = "/{stakeholderId}", produces = ALL_VALUE)
	@Operation(description = "Delete stakeholder on errand by stakeholder id.", responses = {
		@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	})
	ResponseEntity<Void> deleteStakeholder(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "errandId", description = "Errand ID", example = "123") @PathVariable(name = "errandId") final Long errandId,
		@PathVariable(name = "stakeholderId") final Long stakeholderId) {

		stakeholderService.delete(errandId, municipalityId, namespace, stakeholderId);
		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}
}
