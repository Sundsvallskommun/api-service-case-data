package se.sundsvall.casedata.api;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import se.sundsvall.casedata.api.model.DecisionDTO;
import se.sundsvall.casedata.api.model.PatchDecisionDTO;
import se.sundsvall.casedata.service.DecisionService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;

@RestController
@Validated
@RequestMapping("/{municipalityId}/decisions")
@Tag(name = "Decisions", description = "Decision operations")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class })))
@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class DecisionResource {

	private final DecisionService decisionService;

	DecisionResource(final DecisionService decisionService) {
		this.decisionService = decisionService;
	}

	@Operation(description = "Get decision by decision id.")
	@GetMapping(path = "/{decisionId}", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "200", description = "OK - Successful operation")
	ResponseEntity<DecisionDTO> getDecisionById(
		@PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@PathVariable(name = "decisionId") final Long decisionId) {

		return ok(decisionService.findByIdAndMunicipalityId(decisionId, municipalityId));
	}

	@Operation(description = "Update decision.")
	@PatchMapping(path = "/{decisionId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = { APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "204", description = "No content - Successful operation")
	ResponseEntity<Void> patchDecision(
		@PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@PathVariable(name = "decisionId") final Long decisionId,
		@RequestBody @Valid final PatchDecisionDTO patchDecisionDTO) {

		decisionService.updateDecision(decisionId, municipalityId, patchDecisionDTO);
		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@Operation(description = "Replace decision.")
	@PutMapping(path = "/{decisionId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = { APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "204", description = "No content - Successful operation")
	ResponseEntity<Void> putDecision(
		@PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@PathVariable(name = "decisionId") final Long decisionId,
		@RequestBody @Valid final DecisionDTO decisionDTO) {

		decisionService.replaceDecision(decisionId, municipalityId, decisionDTO);
		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}
}
