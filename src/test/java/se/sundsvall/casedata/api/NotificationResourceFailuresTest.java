package se.sundsvall.casedata.api;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.zalando.problem.Status.BAD_REQUEST;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;
import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.Notification;
import se.sundsvall.casedata.api.model.PatchNotification;
import se.sundsvall.casedata.service.NotificationService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class NotificationResourceFailuresTest {

	private static final String BASE_PATH = "/{municipalityId}/{namespace}";
	private static final String NOTIFICATIONS_PATH = BASE_PATH + "/notifications";
	private static final String ERRAND_NOTIFICATIONS_PATH = BASE_PATH + "/errands/{errandId}/notifications";
	private static final String NAMESPACE = "namespace";
	private static final String MUNICIPALITY_ID = "2281";

	@Autowired
	private WebTestClient webTestClient;

	@MockitoBean
	private NotificationService notificationServiceMock;

	@Test
	void createWithMissingBody() {

		// Arrange
		final var errandId = 12345L;

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(ERRAND_NOTIFICATIONS_PATH).build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", errandId)))
			.contentType(APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo(BAD_REQUEST.getReasonPhrase());
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getDetail()).isEqualTo(
			"Required request body is missing: org.springframework.http.ResponseEntity<java.lang.Void> se.sundsvall.casedata.api.NotificationResource.createNotification(java.lang.String,java.lang.String,java.lang.Long,se.sundsvall.casedata.api.model.Notification)");

		verifyNoInteractions(notificationServiceMock);
	}

	@Test
	void createWithInvalidMunicipalityId() {

		// Arrange
		final var municipalityId = "invalid";
		final var errandId = 12345L;
		final var requestBody = Notification.builder()
			.withOwnerId("SomeOwnerId")
			.withOwnerFullName("SomeOwnerFullName")
			.withCreatedBy("SomeUser")
			.withType("SomeType")
			.withDescription("Some description")
			.withErrandId(12345L)
			.withAcknowledged(false)
			.build();

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(ERRAND_NOTIFICATIONS_PATH).build(Map.of("municipalityId", municipalityId, "namespace", NAMESPACE, "errandId", errandId)))
			.contentType(APPLICATION_JSON)
			.bodyValue(requestBody)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("createNotification.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(notificationServiceMock);
	}

	@Test
	void createWithEmptyBody() {

		// Arrange
		final var requestBody = Notification.builder().build();

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(ERRAND_NOTIFICATIONS_PATH).build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", 12345L)))
			.contentType(APPLICATION_JSON)
			.bodyValue(requestBody)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(
				tuple("description", "must not be blank"),
				tuple("errandId", "must not be null"),
				tuple("ownerId", "must not be blank"),
				tuple("type", "must not be blank"));

		verifyNoInteractions(notificationServiceMock);
	}

	@Test
	void updateWithMissingBody() {

		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(NOTIFICATIONS_PATH).build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE)))
			.contentType(APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo(BAD_REQUEST.getReasonPhrase());
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getDetail()).isEqualTo(
			"Required request body is missing: org.springframework.http.ResponseEntity<java.lang.Void> se.sundsvall.casedata.api.NotificationResource.updateNotifications(java.lang.String,java.lang.String,java.util.List<se.sundsvall.casedata.api.model.PatchNotification>)");

		verifyNoInteractions(notificationServiceMock);
	}

	@Test
	void updateWithInvalidMunicipalityId() {

		// Arrange
		final var municipalityId = "invalid";
		final var requestBody = List.of(
			PatchNotification.builder()
				.withId(randomUUID().toString())
				.withOwnerId("SomeOwnerId")
				.withType("SomeType")
				.withDescription("Some description")
				.withAcknowledged(false)
				.build());

		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(NOTIFICATIONS_PATH).build(Map.of("municipalityId", municipalityId, "namespace", NAMESPACE)))
			.contentType(APPLICATION_JSON)
			.bodyValue(requestBody)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("updateNotifications.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(notificationServiceMock);
	}

	@Test
	void updateWithMissingId() {

		// Arrange
		final var requestBody = List.of(
			PatchNotification.builder().withId(randomUUID().toString()).withAcknowledged(false).build(),
			PatchNotification.builder().withAcknowledged(false).build(), // Missing ID
			PatchNotification.builder().withId(randomUUID().toString()).withAcknowledged(false).build());

		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(NOTIFICATIONS_PATH).build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE)))
			.contentType(APPLICATION_JSON)
			.bodyValue(requestBody)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("updateNotifications.notifications[1].id", "not a valid UUID"));

		verifyNoInteractions(notificationServiceMock);
	}

	@Test
	void getNotificationsWithInvalidMunicipalityId() {

		// Arrange
		final var ownerId = "owner";
		final var municipalityId = "invalid";

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(NOTIFICATIONS_PATH).queryParam("ownerId", ownerId).build(Map.of("municipalityId", municipalityId, "namespace", NAMESPACE)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("getNotificationsForOwner.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(notificationServiceMock);
	}

	@Test
	void getNotificationsWithMissingOwnerId() {

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(NOTIFICATIONS_PATH).build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo(BAD_REQUEST.getReasonPhrase());
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getDetail()).isEqualTo("Required request parameter 'ownerId' for method parameter type String is not present");

		verifyNoInteractions(notificationServiceMock);
	}

	@Test
	void getNotificationWithInvalidMunicipalityId() {

		// Arrange
		final var notificationId = randomUUID().toString();
		final var municipalityId = "invalid";
		final var errandId = 12345L;

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(ERRAND_NOTIFICATIONS_PATH + "/{notificationId}").build(Map.of("municipalityId", municipalityId, "namespace", NAMESPACE, "errandId", errandId, "notificationId", notificationId)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("getNotification.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(notificationServiceMock);
	}
}
