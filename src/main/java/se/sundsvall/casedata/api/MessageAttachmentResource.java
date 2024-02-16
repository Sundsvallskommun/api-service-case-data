package se.sundsvall.casedata.api;

import static org.springframework.http.MediaType.ALL_VALUE;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import se.sundsvall.casedata.api.model.MessageAttachmentDTO;
import se.sundsvall.casedata.service.MessageService;

@RestController
@Validated
@RequestMapping("/messageattachments")
@Tag(name = "MessageAttachments", description = "MessageAttachment operations")
@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class })))
@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class MessageAttachmentResource {

	private final MessageService service;

	MessageAttachmentResource(final MessageService service) {
		this.service = service;
	}

	/**
	 * @deprecated
	 */
	@Deprecated(forRemoval = true, since = "2023-10-25")
	@Operation(description = "Get a messageAttachment. This resource has been marked as deprecated. Use /{attchmentID}/streamed instead.")
	@GetMapping(path = "/{attachmentID}", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "200", description = "OK - Successful Operation")
	ResponseEntity<MessageAttachmentDTO> getMessageAttachment(@PathVariable final String attachmentID) {
		return ResponseEntity.ok(service.getMessageAttachment(attachmentID));
	}

	@Operation(summary = "Get a streamed messageAttachment.", description = "Fetches the message attachment that matches the provided id in a streamed manner")
	@GetMapping(path = "/{attachmentID}/streamed", produces = { ALL_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class })))
	@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	void getMessageAttachmentStreamed(@PathVariable final String attachmentID, final HttpServletResponse response) {
		service.getMessageAttachmentStreamed(attachmentID, response);
	}
}
