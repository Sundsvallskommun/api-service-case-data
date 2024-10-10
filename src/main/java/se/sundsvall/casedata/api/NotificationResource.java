package se.sundsvall.casedata.api;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;
import static se.sundsvall.casedata.service.util.Constants.NAMESPACE_REGEXP;
import static se.sundsvall.casedata.service.util.Constants.NAMESPACE_VALIDATION_MESSAGE;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import se.sundsvall.casedata.api.model.Notification;
import se.sundsvall.casedata.api.model.PatchNotification;
import se.sundsvall.casedata.service.NotificationService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;

@RestController
@Validated
@RequestMapping("/{municipalityId}/{namespace}/notifications")
@Tag(name = "Notifications", description = "User notifications operations")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class NotificationResource {

	private final NotificationService notificationService;

	NotificationResource(final NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@GetMapping(produces = {
		APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE
	})
	@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
	@Operation(summary = "Get notifications", description = "Get notifications for the provided namespace, municipality and ownerId")
	ResponseEntity<List<Notification>> getNotifications(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "my.namespace") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "ownerId", description = "ownerId", example = "AD01") @RequestParam final String ownerId) {

		return ok(notificationService.getNotifications(municipalityId, namespace, ownerId));
	}

	@GetMapping("/{notificationId}")
	@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true)
	@Operation(summary = "Get notification", description = "Get a specific notification for the namespace and municipality")
	ResponseEntity<Notification> getNotification(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "my.namespace") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "notificationId", description = "notificationId", example = "12") @PathVariable final String notificationId) {

		return ok(notificationService.getNotification(municipalityId, namespace, notificationId));
	}

	@PostMapping
	@ApiResponse(responseCode = "201", description = "Created - Successful operation", headers = @Header(name = LOCATION, schema = @Schema(type = "string")), useReturnTypeSchema = true)
	@Operation(summary = "Create notification", description = "Create new notification for the namespace and municipality")
	ResponseEntity<Void> createNotification(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "my.namespace") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Valid @NotNull @RequestBody final Notification notification) {

		final var result = notificationService.createNotification(municipalityId, namespace, notification);
		return created(fromPath("/{municipalityId}/{namespace}/notifications/{notificationId}")
			.buildAndExpand(municipalityId, namespace, result.getId()).toUri())
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@PatchMapping
	@ApiResponse(responseCode = "204", description = "Successful operation", useReturnTypeSchema = true)
	@Operation(summary = "Update notification", description = "Update notifications for the namespace and municipality")
	ResponseEntity<Void> updateNotifications(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "my.namespace") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Valid @NotEmpty @RequestBody final List<PatchNotification> notifications) {

		notificationService.updateNotifications(municipalityId, namespace, notifications);
		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@DeleteMapping("/{notificationId}")
	@ApiResponse(responseCode = "204", description = "Successful operation", useReturnTypeSchema = true)
	@Operation(summary = "Delete notification", description = "Delete notification for the namespace and municipality")
	ResponseEntity<Void> deleteNotification(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "my.namespace") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "notificationId", description = "notificationId", example = "12") @PathVariable final String notificationId) {

		notificationService.deleteNotification(municipalityId, namespace, notificationId);
		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}
}
