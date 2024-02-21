package se.sundsvall.casedata.api;

import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

import java.util.List;

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
import org.springframework.web.util.UriComponentsBuilder;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import se.sundsvall.casedata.api.model.AttachmentDTO;
import se.sundsvall.casedata.service.AttachmentService;

@RestController
@Validated
@RequestMapping("/attachments")
@Tag(name = "Attachments", description = "Attachment operations")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class })))
@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class AttachmentResource {

	private final AttachmentService attachmentService;

	AttachmentResource(final AttachmentService attachmentService) {
		this.attachmentService = attachmentService;
	}

	@Operation(description = "Get attachment by ID.")
	@GetMapping(path = "/{id}", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<AttachmentDTO> getAttachments(@PathVariable final Long id) {
		return ok(attachmentService.findById(id));
	}

	@Operation(description = "Get attachment by errandnumber.")
	@GetMapping(path = "/errand/{errand_number}", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<List<AttachmentDTO>> getAttachmentsByErrandNumber(@PathVariable("errand_number") final String errandNumber) {
		return ok(attachmentService.findByErrandNumber(errandNumber));
	}

	@Operation(description = "Create attachment")
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = { APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "201", description = "Created - Successful operation", headers = @Header(name = LOCATION, description = "Location of the created resource."), useReturnTypeSchema = true)
	ResponseEntity<Void> postAttachment(final UriComponentsBuilder uriComponentsBuilder, @RequestBody @Valid final AttachmentDTO attachmentDTO) {
		final var result = attachmentService.createAttachment(attachmentDTO);
		return created(uriComponentsBuilder.path("/attachments/{id}").buildAndExpand(result.getId()).toUri())
			.build();
	}

	@Operation(description = "Replace attachment.")
	@PutMapping(path = "/{attachmentId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = { APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<Void> putAttachmentOnErrand(@PathVariable final Long attachmentId, @RequestBody @Valid final AttachmentDTO attachmentDTO) {
		attachmentService.replaceAttachment(attachmentId, attachmentDTO);
		return noContent().build();
	}

	@Operation
	@PatchMapping(path = "/{attachmentId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = { APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<Void> patchAttachment(@PathVariable final Long attachmentId, @RequestBody @Valid final AttachmentDTO attachmentDTO) {
		attachmentService.updateAttachment(attachmentId, attachmentDTO);
		return noContent().build();
	}

	@Operation
	@DeleteMapping(path = "/{attachmentId}")
	@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<Void> deleteAttachment(@PathVariable final Long attachmentId) {
		if (attachmentService.deleteAttachment(attachmentId)) {
			return ResponseEntity.noContent().build();
		}
		return notFound().build();
	}
}
