package se.sundsvall.casedata.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.casedata.api.model.Attachment;
import se.sundsvall.casedata.service.DecisionAttachmentService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;
import static se.sundsvall.casedata.service.util.Constants.NAMESPACE_REGEXP;
import static se.sundsvall.casedata.service.util.Constants.NAMESPACE_VALIDATION_MESSAGE;

/**
 * Attachments owned by a decision. They mirror the errand attachment operations but are scoped by decision id, and an
 * attachment created here belongs to the decision only - it is not part of the errand's own attachments.
 */
@RestController
@Validated
@RequestMapping("/{municipalityId}/{namespace}/errands/{errandId}/decisions/{decisionId}/attachments")
@Tag(name = "Decisions", description = "Decision operations")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class DecisionAttachmentResource {

	private final DecisionAttachmentService decisionAttachmentService;
	private final ObjectMapper objectMapper;
	private final Validator validator;

	DecisionAttachmentResource(final DecisionAttachmentService decisionAttachmentService, final ObjectMapper objectMapper, final Validator validator) {
		this.decisionAttachmentService = decisionAttachmentService;
		this.objectMapper = objectMapper;
		this.validator = validator;
	}

	@GetMapping(path = "/{attachmentId}", produces = ALL_VALUE)
	@Operation(summary = "Get a streamed decision attachment.", description = "Fetches the binary content of the attachment that matches the provided id in a streamed manner.", responses = {
		@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
	})
	void getDecisionAttachment(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "errandId", description = "Errand ID", example = "123") @PathVariable final Long errandId,
		@Parameter(name = "decisionId", description = "Decision ID", example = "123") @PathVariable final Long decisionId,
		@PathVariable final Long attachmentId,
		final HttpServletResponse response) {

		decisionAttachmentService.findAttachmentAsStreamedResponse(errandId, decisionId, attachmentId, municipalityId, namespace, response);
	}

	@GetMapping(produces = APPLICATION_JSON_VALUE)
	@Operation(description = "Get attachments metadata by decision id.", responses = {
		@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
	})
	ResponseEntity<List<Attachment>> getAttachmentsByDecisionId(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "errandId", description = "Errand ID", example = "123") @PathVariable final Long errandId,
		@Parameter(name = "decisionId", description = "Decision ID", example = "123") @PathVariable final Long decisionId) {

		return ok(decisionAttachmentService.findAttachments(errandId, decisionId, municipalityId, namespace));
	}

	@PostMapping(consumes = MULTIPART_FORM_DATA_VALUE, produces = ALL_VALUE)
	@Operation(description = "Create attachment on decision. The metadata is supplied as a JSON part named 'attachment' and the binary content as a file part named 'file'.", responses = {
		@ApiResponse(responseCode = "201", description = "Created - Successful operation", headers = @Header(name = LOCATION, description = "Location of the created resource.", schema = @Schema(type = "string")), useReturnTypeSchema = true)
	})
	ResponseEntity<Void> postDecisionAttachment(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "errandId", description = "Errand ID", example = "123") @PathVariable final Long errandId,
		@Parameter(name = "decisionId", description = "Decision ID", example = "123") @PathVariable final Long decisionId,
		@RequestPart("attachment") @Schema(description = "Attachment metadata", implementation = Attachment.class) final String attachment,
		@NotNull @RequestPart("file") final MultipartFile file) {

		if (file.isEmpty()) {
			throw Problem.valueOf(BAD_REQUEST, "The 'file' part must not be empty");
		}

		final var attachmentMetadata = AttachmentMetadataParts.parse(objectMapper, attachment);
		AttachmentMetadataParts.validate(validator, attachmentMetadata);

		final var result = decisionAttachmentService.create(errandId, decisionId, attachmentMetadata, file, municipalityId, namespace);
		return created(fromPath("/{municipalityId}/{namespace}/errands/{errandId}/decisions/{decisionId}/attachments/{id}").buildAndExpand(municipalityId, namespace, errandId, decisionId, result.getId()).toUri())
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@PatchMapping(path = "/{attachmentId}", consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	@Operation(description = "Update attachment metadata on decision. The binary content cannot be updated; replace it by deleting and recreating the attachment.", responses = {
		@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	})
	ResponseEntity<Void> patchDecisionAttachment(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "errandId", description = "Errand ID", example = "123") @PathVariable final Long errandId,
		@Parameter(name = "decisionId", description = "Decision ID", example = "123") @PathVariable final Long decisionId,
		@PathVariable final Long attachmentId,
		@RequestBody @Valid final Attachment attachment) {

		decisionAttachmentService.update(errandId, decisionId, attachmentId, municipalityId, namespace, attachment);
		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@DeleteMapping(path = "/{attachmentId}", produces = ALL_VALUE)
	@Operation(description = "Delete attachment on decision.", responses = {
		@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	})
	ResponseEntity<Void> deleteDecisionAttachment(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "errandId", description = "Errand ID", example = "123") @PathVariable final Long errandId,
		@Parameter(name = "decisionId", description = "Decision ID", example = "123") @PathVariable final Long decisionId,
		@PathVariable final Long attachmentId) {

		decisionAttachmentService.delete(errandId, decisionId, attachmentId, municipalityId, namespace);
		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}
}
