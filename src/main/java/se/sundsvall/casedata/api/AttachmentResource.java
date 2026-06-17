package se.sundsvall.casedata.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
import se.sundsvall.casedata.service.AttachmentService;
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

@RestController
@Validated
@RequestMapping("/{municipalityId}/{namespace}/errands/{errandId}/attachments")
@Tag(name = "Attachments", description = "Attachment operations")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class AttachmentResource {

	private final AttachmentService attachmentService;
	private final ObjectMapper objectMapper;
	private final Validator validator;

	AttachmentResource(final AttachmentService attachmentService, final ObjectMapper objectMapper, final Validator validator) {
		this.attachmentService = attachmentService;
		this.objectMapper = objectMapper;
		this.validator = validator;
	}

	@GetMapping(path = "/{attachmentId}", produces = ALL_VALUE)
	@Operation(summary = "Get a streamed attachment.", description = "Fetches the binary content of the attachment that matches the provided id in a streamed manner.", responses = {
		@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
	})
	void getAttachment(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "errandId", description = "Errand ID", example = "123") @PathVariable final Long errandId,
		@PathVariable final Long attachmentId,
		final HttpServletResponse response) {

		attachmentService.findAttachmentAsStreamedResponse(errandId, attachmentId, municipalityId, namespace, response);
	}

	@GetMapping(produces = APPLICATION_JSON_VALUE)
	@Operation(description = "Get attachments metadata by errand id.", responses = {
		@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
	})
	ResponseEntity<List<Attachment>> getAttachmentsByErrandId(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "errandId", description = "Errand ID", example = "123") @PathVariable final Long errandId) {

		return ok(attachmentService.findAttachments(errandId, municipalityId, namespace));
	}

	@PostMapping(consumes = MULTIPART_FORM_DATA_VALUE, produces = ALL_VALUE)
	@Operation(description = "Create attachment on errand. The metadata is supplied as a JSON part named 'attachment' and the binary content as a file part named 'file'.", responses = {
		@ApiResponse(responseCode = "201", description = "Created - Successful operation", headers = @Header(name = LOCATION, description = "Location of the created resource.", schema = @Schema(type = "string")), useReturnTypeSchema = true)
	})
	ResponseEntity<Void> postAttachment(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "errandId", description = "Errand ID", example = "123") @PathVariable final Long errandId,
		@RequestPart("attachment") @Schema(description = "Attachment metadata", implementation = Attachment.class) final String attachment,
		@NotNull @RequestPart("file") final MultipartFile file) {

		if (file.isEmpty()) {
			throw Problem.valueOf(BAD_REQUEST, "The 'file' part must not be empty");
		}

		final var attachmentMetadata = objectMapper.readValue(attachment, Attachment.class);
		validate(attachmentMetadata);

		final var result = attachmentService.create(errandId, attachmentMetadata, file, municipalityId, namespace);
		return created(fromPath("/{municipalityId}/{namespace}/errands/{errandId}/attachments/{id}").buildAndExpand(municipalityId, namespace, errandId, result.getId()).toUri())
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@PatchMapping(path = "/{attachmentId}", consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	@Operation(description = "Update attachment metadata on errand. The binary content cannot be updated; replace it by deleting and recreating the attachment.", responses = {
		@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	})
	ResponseEntity<Void> patchAttachment(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "errandId", description = "Errand ID", example = "123") @PathVariable final Long errandId,
		@PathVariable final Long attachmentId,
		@RequestBody @Valid final Attachment attachment) {

		attachmentService.update(errandId, attachmentId, municipalityId, namespace, attachment);
		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@DeleteMapping(path = "/{attachmentId}", produces = ALL_VALUE)
	@Operation(description = "Delete attachment on errand.", responses = {
		@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	})
	ResponseEntity<Void> deleteAttachment(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "errandId", description = "Errand ID", example = "123") @PathVariable final Long errandId,
		@PathVariable final Long attachmentId) {

		attachmentService.delete(errandId, attachmentId, municipalityId, namespace);
		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	private <T> void validate(final T object) {
		final Set<ConstraintViolation<T>> violations = validator.validate(object);
		if (!violations.isEmpty()) {
			final var sorted = violations.stream()
				.sorted(Comparator.comparing(v -> v.getPropertyPath().toString()))
				.collect(Collectors.toCollection(LinkedHashSet::new));
			throw new ConstraintViolationException(sorted);
		}
	}
}
