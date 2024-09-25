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
import se.sundsvall.casedata.api.model.AppealDTO;
import se.sundsvall.casedata.api.model.PatchAppealDTO;
import se.sundsvall.casedata.service.AppealService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;

@RestController
@Validated
@RequestMapping("/{municipalityId}/appeals")
@Tag(name = "Appeals", description = "Appeal operations")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class })))
@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class AppealResource {

	private final AppealService appealService;

	AppealResource(final AppealService appealService) {
		this.appealService = appealService;
	}

	@Operation(description = "Get appeal by ID")
	@GetMapping(path = "/{appealId}", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<AppealDTO> getAppealById(
		@PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@PathVariable(name = "appealId") final Long appealId) {

		return ok(appealService.findByIdAndMunicipalityId(appealId, municipalityId));
	}

	@Operation(description = "Update appeal")
	@PatchMapping(path = "/{appealId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = { APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<Void> patchAppeal(
		@PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@PathVariable(name = "appealId") final Long appealId,
		@RequestBody @Valid final PatchAppealDTO patchAppealDTO) {

		appealService.updateAppeal(appealId, municipalityId, patchAppealDTO);
		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@Operation(description = "Replace appeal")
	@PutMapping(path = "/{appealId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = { APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<Void> putAppeal(
		@PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@PathVariable(name = "appealId") final Long appealId,
		@RequestBody @Valid final AppealDTO appealDTO) {

		appealService.replaceAppeal(appealId, municipalityId, appealDTO);
		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}
}
