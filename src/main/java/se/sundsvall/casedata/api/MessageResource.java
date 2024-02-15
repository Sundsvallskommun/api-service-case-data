package se.sundsvall.casedata.api;


import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;

import se.sundsvall.casedata.api.model.MessageRequest;
import se.sundsvall.casedata.api.model.MessageResponse;
import se.sundsvall.casedata.service.MessageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Validated
@RequestMapping("/messages")
@Tag(name = "Messages", description = "Message operations")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {Problem.class, ConstraintViolationProblem.class})))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class MessageResource {

	private final MessageService service;

	MessageResource(final MessageService service) {
		this.service = service;
	}

	@Operation(description = "Get all messages for an errand.")
	@GetMapping(path = "/{errandNumber}", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<List<MessageResponse>> getMessagesOnErrand(@PathVariable final String errandNumber) {
		return ResponseEntity.ok(service.getMessagesByErrandNumber(errandNumber));
	}

	@Operation(description = "Save a message.")
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_PROBLEM_JSON_VALUE})
	@ApiResponse(responseCode = "204", description = "No content - Successful operation")
	ResponseEntity<Void> patchErrandWithMessage(@RequestBody final MessageRequest request) {
		service.saveMessage(request);
		return ResponseEntity.noContent().build();
	}

	@Operation(description = "Set viewed status for message.")
	@PutMapping(path = "/{messageID}/viewed/{isViewed}", produces = {APPLICATION_PROBLEM_JSON_VALUE})
	@ApiResponse(responseCode = "204", description = "No content - Successful operation")
	ResponseEntity<Void> updateViewedStatus(@PathVariable final String messageID, @PathVariable final boolean isViewed) {
		service.updateViewedStatus(messageID, isViewed);
		return ResponseEntity.noContent().build();
	}
}
