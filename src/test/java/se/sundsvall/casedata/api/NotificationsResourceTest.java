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
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.Notification;
import se.sundsvall.casedata.api.model.PatchNotification;
import se.sundsvall.casedata.service.NotificationService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class NotificationsResourceTest {

	private static final String PATH = "/{municipalityId}/{namespace}/notifications";
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
		when(notificationServiceMock.getNotification(MUNICIPALITY_ID, NAMESPACE, notificationId)).thenReturn(Notification.builder().build());

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH + "/{notificationId}")
				.build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID, "notificationId", notificationId)))
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(Notification.class)
			.returnResult();

		// Assert
		assertThat(response).isNotNull();
		verify(notificationServiceMock).getNotification(MUNICIPALITY_ID, NAMESPACE, notificationId);
	}

	@Test
	void getNotifications() {

		// Arrange
		final var ownerId = "owner";
		when(notificationServiceMock.getNotifications(MUNICIPALITY_ID, NAMESPACE, ownerId)).thenReturn(List.of(Notification.builder().build()));

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH).queryParam("ownerId", ownerId)
				.build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID)))
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(List.class)
			.returnResult();

		// Assert
		assertThat(response).isNotNull();
		verify(notificationServiceMock).getNotifications(MUNICIPALITY_ID, NAMESPACE, ownerId);
	}

	@Test
	void createNotification() {

		// Arrange
		final var requestBody = Notification.builder()
			.withOwnerId("SomeOwnerId")
			.withOwnerFullName("SomeOwnerFullName")
			.withCreatedBy("SomeUser")
			.withType("SomeType")
			.withDescription("Some description")
			.withErrandId(12345L)
			.withAcknowledged(false)
			.build();

		final var notificationId = UUID.randomUUID().toString();
		when(notificationServiceMock.createNotification(MUNICIPALITY_ID, NAMESPACE, requestBody)).thenReturn(Notification.builder().withId(notificationId).build());

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(PATH)
				.build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID)))
			.contentType(APPLICATION_JSON)
			.accept(APPLICATION_JSON)
			.bodyValue(requestBody)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL)
			.expectHeader().location("/" + MUNICIPALITY_ID + "/" + NAMESPACE + "/notifications/" + notificationId)
			.expectBody().isEmpty();

		// Assert
		assertThat(response).isNotNull();
		verify(notificationServiceMock).createNotification(MUNICIPALITY_ID, NAMESPACE, requestBody);
	}

	@Test
	void updateNotification() {

		// Arrange
		final var notificationId1 = UUID.randomUUID().toString();
		final var notificationId2 = UUID.randomUUID().toString();
		final var requestBody = List.of(
			PatchNotification.builder().withId(notificationId1).withAcknowledged(true).build(),
			PatchNotification.builder().withId(notificationId2).withAcknowledged(true).build());

		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(PATH)
				.build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID)))
			.contentType(APPLICATION_JSON)
			.accept(APPLICATION_JSON)
			.bodyValue(requestBody)
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		// Assert
		assertThat(response).isNotNull();
		verify(notificationServiceMock).updateNotifications(MUNICIPALITY_ID, NAMESPACE, requestBody);
	}

	@Test
	void deleteNotification() {

		// Arrange
		final var notificationId = UUID.randomUUID().toString();

		// Act
		final var response = webTestClient.delete()
			.uri(builder -> builder.path(PATH + "/" + notificationId)
				.build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		// Assert
		assertThat(response).isNotNull();
		verify(notificationServiceMock).deleteNotification(MUNICIPALITY_ID, NAMESPACE, notificationId);
	}
}
