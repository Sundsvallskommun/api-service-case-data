package se.sundsvall.casedata.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Validated
@RequestMapping("/")
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
	@GetMapping(path = "attachments/{id}/history", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	ResponseEntity<String> getAttachmentHistory(@PathVariable final Long id) {
		return ResponseEntity.ok(historyService.findAttachmentHistory(id));
	}

	@Operation(description = "Get decision history.")
	@GetMapping(path = "decisions/{id}/history", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	ResponseEntity<String> getDecisionHistory(@PathVariable final Long id) {
		return ResponseEntity.ok(historyService.findDecisionHistory(id));
	}

	@Operation(description = "Get errand history.")
	@GetMapping(path = "errands/{id}/history", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	ResponseEntity<String> getErrandHistory(@PathVariable final Long id) {
		return ResponseEntity.ok(historyService.findErrandHistory(id));
	}

	@Operation(description = "Get facility history.")
	@GetMapping(path = "facilities/{id}/history", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	ResponseEntity<String> getFacilityHistory(@PathVariable final Long id) {
		return ResponseEntity.ok(historyService.findFacilityHistory(id));
	}

	@Operation(description = "Get note history.")
	@GetMapping(path = "notes/{id}/history", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	ResponseEntity<String> getNoteHistory(@PathVariable final Long id) {
		return ResponseEntity.ok(historyService.findNoteHistory(id));
	}

	@Operation(description = "Get stakeholder history.")
	@GetMapping(path = "stakeholders/{id}/history", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	ResponseEntity<String> getStakeholderHistory(@PathVariable final Long id) {
		return ResponseEntity.ok(historyService.findStakeholderHistory(id));
	}

}
