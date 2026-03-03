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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.IOException;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.casedata.api.model.conversation.Conversation;
import se.sundsvall.casedata.api.model.conversation.Message;
import se.sundsvall.casedata.service.ConversationService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
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
@RequestMapping("/{municipalityId}/{namespace}/errands/{errandId}/communication/conversations")
@Tag(name = "Conversations", description = "Conversation operations")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class ConversationResource {

	private final ConversationService conversationService;

	ConversationResource(final ConversationService conversationService) {
		this.conversationService = conversationService;
	}

	@PostMapping(consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	@Operation(summary = "Create conversation", description = "Create new conversation", responses = {
		@ApiResponse(responseCode = "201", headers = @Header(name = LOCATION, schema = @Schema(type = "string")), description = "Successful operation", useReturnTypeSchema = true),
	})
	ResponseEntity<Void> createConversation(
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "errandId", description = "Errand ID", example = "1") @PathVariable("errandId") final Long errandId,
		@Valid @NotNull @RequestBody final Conversation request) {

		final var conversationId = conversationService.createConversation(municipalityId, namespace, errandId, request);

		return created(fromPath("/{municipalityId}/{namespace}/errands/{errandId}/communication/conversations/{conversationId}")
			.buildAndExpand(municipalityId, namespace, errandId, conversationId).toUri())
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@GetMapping(path = "/{conversationId}", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get conversation", description = "Get existing conversation", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<Conversation> getConversation(
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "errandId", description = "Errand ID", example = "1") @PathVariable("errandId") final Long errandId,
		@Parameter(name = "conversationId", description = "Conversation ID", example = "1aefbbb8-de82-414b-b5d7-ba7c5bbe4506") @ValidUuid @PathVariable("conversationId") final String conversationId) {

		return ok(conversationService.getConversation(municipalityId, namespace, errandId, conversationId));
	}

	@GetMapping(produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get conversations", description = "Get existing conversations", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<List<Conversation>> getConversations(
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "errandId", description = "Errand ID", example = "1") @PathVariable("errandId") final Long errandId) {

		return ok(conversationService.getConversations(municipalityId, namespace, errandId));
	}

	@PatchMapping(path = "/{conversationId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Update conversation", description = "Update existing conversation", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<Conversation> updateConversation(
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "errandId", description = "Errand ID", example = "1") @PathVariable("errandId") final Long errandId,
		@Parameter(name = "conversationId", description = "Conversation ID", example = "1aefbbb8-de82-414b-b5d7-ba7c5bbe4506") @ValidUuid @PathVariable("conversationId") final String conversationId,
		@Valid @NotNull @RequestBody final Conversation request) {

		return ok(conversationService.updateConversation(municipalityId, namespace, errandId, conversationId, request));
	}

	@PostMapping(path = "/{conversationId}/messages", consumes = MULTIPART_FORM_DATA_VALUE, produces = ALL_VALUE)
	@Operation(summary = "Create new message", description = "Create new message", responses = {
		@ApiResponse(responseCode = "204", description = "Successful operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<Void> createMessage(
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "errandId", description = "Errand ID", example = "1") @PathVariable("errandId") final Long errandId,
		@Parameter(name = "conversationId", description = "Conversation ID", example = "1aefbbb8-de82-414b-b5d7-ba7c5bbe4506") @ValidUuid @PathVariable("conversationId") final String conversationId,
		@RequestPart("message") @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Message to be posted") @Valid final Message messageRequest,
		@RequestPart(value = "attachments", required = false) final List<MultipartFile> attachments) {

		conversationService.createMessage(municipalityId, namespace, errandId, conversationId, messageRequest, attachments);
		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@GetMapping(path = "/{conversationId}/messages", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get message list", description = "Get message list", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<Page<Message>> getMessages(
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "errandId", description = "Errand ID", example = "1") @PathVariable("errandId") final Long errandId,
		@Parameter(name = "conversationId", description = "Conversation ID", example = "1aefbbb8-de82-414b-b5d7-ba7c5bbe4506") @ValidUuid @PathVariable("conversationId") final String conversationId,
		@ParameterObject final Pageable pageable) {

		return ok(conversationService.getMessages(municipalityId, namespace, errandId, conversationId, pageable));
	}

	@GetMapping(path = "{conversationId}/messages/{messageId}/attachments/{attachmentId}", produces = ALL_VALUE)
	@Operation(summary = "Get a streamed conversation message attachment.", description = "Fetches the conversation message attachment that matches the provided ID, in a streamed manner", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class))),
	})
	void getConversationMessageAttachment(
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "errandId", description = "Errand ID", example = "1") @PathVariable("errandId") final Long errandId,
		@Parameter(name = "conversationId", description = "Conversation ID", example = "1aefbbb8-de82-414b-b5d7-ba7c5bbe4506") @ValidUuid @PathVariable("conversationId") final String conversationId,
		@Parameter(name = "messageId", description = "Message ID", example = "be3a5c7b-0f39-4867-8065-6a286b36fcc3") @ValidUuid @PathVariable("messageId") final String messageId,
		@Parameter(name = "attachmentId", description = "Conversation message attachment ID", example = "b82bd8ac-1507-4d9a-958d-369261eecc15") @ValidUuid @PathVariable final String attachmentId,
		final HttpServletResponse response) throws IOException {

		conversationService.getConversationMessageAttachment(municipalityId, namespace, errandId, conversationId, messageId, attachmentId, response);

	}

}
