package se.sundsvall.casedata.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;

import se.sundsvall.casedata.api.model.history.HistoryDTO;
import se.sundsvall.casedata.service.HistoryService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Validated
@RequestMapping("/{municipalityId}/")
@Tag(name = "History", description = "History operations")
@ApiResponse(responseCode = "200", description = "OK - Successful operation", content = @Content(mediaType = APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = HistoryDTO.class))))
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {Problem.class, ConstraintViolationProblem.class})))
@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class HistoryResource {

	private final HistoryService historyService;

	HistoryResource(HistoryService historyService) {
		this.historyService = historyService;
	}

	@Operation(description = "Get attachment history.")
	@GetMapping(path = "attachments/{attachmentId}/history", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	ResponseEntity<String> getAttachmentHistory(
		@PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@PathVariable(name = "attachmentId") final Long attachmentId) {

		return ok(historyService.findAttachmentHistory(attachmentId));
	}

	@Operation(description = "Get decision history.")
	@GetMapping(path = "decisions/{decisionId}/history", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	ResponseEntity<String> getDecisionHistory(
		@PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@PathVariable(name = "decisionId") final Long decisionId) {

		return ok(historyService.findDecisionHistory(decisionId));
	}

	@Operation(description = "Get errand history.")
	@GetMapping(path = "errands/{errandId}/history", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	ResponseEntity<String> getErrandHistory(
		@PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@PathVariable(name = "errandId") final Long errandId) {

		return ok(historyService.findErrandHistory(errandId));
	}

	@Operation(description = "Get facility history.")
	@GetMapping(path = "facilities/{facilityId}/history", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	ResponseEntity<String> getFacilityHistory(
		@PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@PathVariable(name = "facilityId") final Long facilityId) {

		return ok(historyService.findFacilityHistory(facilityId));
	}

	@Operation(description = "Get note history.")
	@GetMapping(path = "notes/{noteId}/history", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	ResponseEntity<String> getNoteHistory(
		@PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@PathVariable(name = "noteId") final Long noteId) {

		return ok(historyService.findNoteHistory(noteId));
	}

	@Operation(description = "Get stakeholder history.")
	@GetMapping(path = "stakeholders/{stakeholderId}/history", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	ResponseEntity<String> getStakeholderHistory(
		@PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@PathVariable(name = "stakeholderId") final Long stakeholderId) {

		return ok(historyService.findStakeholderHistory(stakeholderId));
	}
}
