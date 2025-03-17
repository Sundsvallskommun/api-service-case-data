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
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.casedata.api.model.Notification;
import se.sundsvall.casedata.api.model.PatchNotification;
import se.sundsvall.casedata.service.NotificationService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

@RestController
@Validated
@RequestMapping("/{municipalityId}/{namespace}")
@Tag(name = "Notifications", description = "User notification operations")
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

	@GetMapping("/errands/{errandId}/notifications/{notificationId}")
	@Operation(summary = "Get notification", description = "Get a specific notification for the namespace and municipality", responses = {
		@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true)
	})
	ResponseEntity<Notification> getNotification(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "notificationId", description = "notificationId", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable final String notificationId,
		@Parameter(name = "errandId", description = "Errand ID", example = "123") @PathVariable(name = "errandId") final Long errandId) {

		return ok(notificationService.findNotification(municipalityId, namespace, errandId, notificationId));
	}

	@GetMapping(path = "/errands/{errandId}/notifications", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get notifications", description = "Get notifications for the provided namespace, municipality and ownerId", responses = {
		@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
	})
	ResponseEntity<List<Notification>> getNotificationsForErrand(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "errandId", description = "Errand ID", example = "123") @PathVariable(name = "errandId") final Long errandId,
		@ParameterObject final Sort sort) {

		return ok(notificationService.findNotifications(municipalityId, namespace, errandId, sort));
	}

	@PostMapping(path = "/errands/{errandId}/notifications", produces = ALL_VALUE)
	@Operation(summary = "Create notification", description = "Create new notification for the namespace and municipality", responses = {
		@ApiResponse(responseCode = "201", description = "Created - Successful operation", headers = @Header(name = LOCATION, schema = @Schema(type = "string")), useReturnTypeSchema = true)
	})
	ResponseEntity<Void> createNotification(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "errandId", description = "Errand ID", example = "123") @PathVariable(name = "errandId") final Long errandId,
		@Valid @NotNull @RequestBody final Notification notification) {
		notification.setErrandId(errandId);
		final var result = notificationService.create(municipalityId, namespace, notification);
		return created(fromPath("/{municipalityId}/{namespace}/errands/{errandId}/notifications/{notificationId}")
			.buildAndExpand(municipalityId, namespace, errandId, result).toUri())
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@DeleteMapping(path = "/errands/{errandId}/notifications/{notificationId}", produces = ALL_VALUE)
	@Operation(summary = "Delete notification", description = "Delete notification for the namespace and municipality", responses = {
		@ApiResponse(responseCode = "204", description = "Successful operation", useReturnTypeSchema = true)
	})
	ResponseEntity<Void> deleteNotification(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "errandId", description = "Errand ID", example = "123") @PathVariable(name = "errandId") final Long errandId,
		@Parameter(name = "notificationId", description = "Notification ID") @ValidUuid @PathVariable final String notificationId) {

		notificationService.delete(municipalityId, namespace, errandId, notificationId);
		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@PutMapping(path = "/errands/{errandId}/notifications/global-acknowledged", produces = ALL_VALUE)
	@Operation(summary = "Global acknowledge notifications", description = "Set the globalAcknowledged-attribute to 'true' for all notifications on the errand", responses = {
		@ApiResponse(responseCode = "204", description = "Successful operation", useReturnTypeSchema = true),
	})
	ResponseEntity<Void> globalAcknowledgeNotification(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "errandId", description = "Errand ID", example = "123") @PathVariable(name = "errandId") final Long errandId) {

		notificationService.globalAcknowledgeNotificationsByErrandId(municipalityId, namespace, errandId);
		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@GetMapping(path = "/notifications", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get notifications", description = "Get notifications for the provided namespace, municipality and ownerId", responses = {
		@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
	})
	ResponseEntity<List<Notification>> getNotificationsForOwner(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "ownerId", description = "ownerId", example = "AD01") @RequestParam final String ownerId) {

		return ok(notificationService.findNotificationsByOwnerId(municipalityId, namespace, ownerId));
	}

	@PatchMapping(path = "/notifications", produces = ALL_VALUE)
	@Operation(summary = "Update notification", description = "Update notifications for the namespace and municipality", responses = {
		@ApiResponse(responseCode = "204", description = "Successful operation", useReturnTypeSchema = true)
	})
	ResponseEntity<Void> updateNotifications(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Valid @NotEmpty @RequestBody final List<PatchNotification> notifications) {

		notificationService.update(municipalityId, namespace, notifications);
		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}
}
