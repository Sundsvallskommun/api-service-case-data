package se.sundsvall.casedata.api;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.Notification;
import se.sundsvall.casedata.api.model.PatchNotification;
import se.sundsvall.casedata.service.NotificationService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class NotificationsResourceTest {

	private static final String PATH = "/{municipalityId}/{namespace}";
	private static final String NAMESPACE = "namespace";
	private static final String MUNICIPALITY_ID = "2281";

	@MockitoBean
	private NotificationService notificationServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getNotification() {

		// Arrange
		final var notificationId = randomUUID().toString();
		final var errandId = 12345L;

		when(notificationServiceMock.findNotification(MUNICIPALITY_ID, NAMESPACE, errandId, notificationId)).thenReturn(Notification.builder().build());

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH + "/errands/{errandId}/notifications/{notificationId}")
				.build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID, "errandId", errandId, "notificationId", notificationId)))
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(Notification.class)
			.returnResult();

		// Assert
		assertThat(response).isNotNull();
		verify(notificationServiceMock).findNotification(MUNICIPALITY_ID, NAMESPACE, errandId, notificationId);
	}

	@Test
	void getNotificationsForOwner() {

		// Arrange
		final var ownerId = "owner";
		when(notificationServiceMock.findNotificationsByOwnerId(MUNICIPALITY_ID, NAMESPACE, ownerId)).thenReturn(List.of(Notification.builder().build()));

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH + "/notifications")
				.queryParam("ownerId", ownerId)
				.build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID)))
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(List.class)
			.returnResult();

		// Assert
		assertThat(response).isNotNull();
		verify(notificationServiceMock).findNotificationsByOwnerId(MUNICIPALITY_ID, NAMESPACE, ownerId);
	}

	@Test
	void getNotificationsForErrand() {

		// Arrange
		final var errandId = 12345L;
		when(notificationServiceMock.findNotifications(MUNICIPALITY_ID, NAMESPACE, errandId, null)).thenReturn(List.of(Notification.builder().build()));

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH + "/errands/{errandId}/notifications")
				.build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID, "errandId", errandId)))
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(List.class)
			.returnResult();

		// Assert
		assertThat(response).isNotNull();
		verify(notificationServiceMock).findNotifications(MUNICIPALITY_ID, NAMESPACE, errandId, Sort.unsorted());
	}

	@Test
	void createNotification() {

		// Arrange
		final var errandId = 12345L;
		final var requestBody = Notification.builder()
			.withOwnerId("SomeOwnerId")
			.withOwnerFullName("SomeOwnerFullName")
			.withCreatedBy("SomeUser")
			.withType("SomeType")
			.withDescription("Some description")
			.withErrandId(errandId)
			.withAcknowledged(false)
			.build();

		final var notificationId = randomUUID().toString();
		when(notificationServiceMock.create(MUNICIPALITY_ID, NAMESPACE, requestBody)).thenReturn(Notification.builder().withErrandId(errandId).withId(notificationId).build());

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(PATH + "/errands/{errandId}/notifications")
				.build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID, "errandId", requestBody.getErrandId())))
			.contentType(APPLICATION_JSON)
			.accept(APPLICATION_JSON)
			.bodyValue(requestBody)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL)
			.expectHeader().location("/" + MUNICIPALITY_ID + "/" + NAMESPACE + "/errands/" + errandId + "/notifications/" + notificationId)
			.expectBody().isEmpty();

		// Assert
		assertThat(response).isNotNull();
		verify(notificationServiceMock).create(MUNICIPALITY_ID, NAMESPACE, requestBody);
	}

	@Test
	void updateNotifications() {

		// Arrange
		final var requestBody = List.of(
			PatchNotification.builder().withId(randomUUID().toString()).withAcknowledged(true).withErrandId(1L).build(),
			PatchNotification.builder().withId(randomUUID().toString()).withAcknowledged(true).withErrandId(2L).build());

		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(PATH + "/notifications")
				.build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID)))
			.contentType(APPLICATION_JSON)
			.accept(APPLICATION_JSON)
			.bodyValue(requestBody)
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		// Assert
		assertThat(response).isNotNull();
		verify(notificationServiceMock).update(MUNICIPALITY_ID, NAMESPACE, requestBody);
	}

	@Test
	void deleteNotification() {

		// Arrange
		final var notificationId = randomUUID().toString();
		final var errandId = 12345L;

		// Act
		final var response = webTestClient.delete()
			.uri(builder -> builder.path(PATH + "/errands/{errandId}/notifications/{notificationId}")
				.build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID, "errandId", errandId, "notificationId", notificationId)))
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		// Assert
		assertThat(response).isNotNull();
		verify(notificationServiceMock).delete(MUNICIPALITY_ID, NAMESPACE, errandId, notificationId);
	}

	@Test
	void globalAcknowledgeNotifications() {

		// Arrange
		final var errandId = 12345L;

		// TODO: Mock
		// doNothing().when(notificationServiceMock).globalAcknowledgeNotificationsByErrandId(MUNICIPALITY_ID, NAMESPACE,
		// ERRAND_ID);

		// Call
		final var response = webTestClient.put()
			.uri(builder -> builder.path(PATH + "/errands/{errandId}/notifications/global-acknowledged")
				.build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID, "errandId", errandId)))
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL)
			.expectBody().isEmpty();

		// Verification
		assertThat(response).isNotNull();
		// TODO: verify(notificationServiceMock).globalAcknowledgeNotificationsByErrandId(MUNICIPALITY_ID, NAMESPACE,
		// ERRAND_ID);
	}
}
