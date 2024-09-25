package se.sundsvall.casedata.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.MessageRequest;
import se.sundsvall.casedata.api.model.MessageResponse;
import se.sundsvall.casedata.service.MessageService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class MessageResourceTest {

	private static final String BASE_URL = "/{municipalityId}/messages";

	@MockBean
	private MessageService messageServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getMessagesForErrandNumber() {

		// Arrange
		final var errandNumber = RandomStringUtils.randomAlphanumeric(10);
		final var messages = List.of(MessageResponse.builder().build());

		when(messageServiceMock.getMessagesByErrandNumber(errandNumber, MUNICIPALITY_ID)).thenReturn(messages);

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandNumber}")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "errandNumber", errandNumber)))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBodyList(MessageResponse.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isEqualTo(messages);
		verify(messageServiceMock).getMessagesByErrandNumber(errandNumber, MUNICIPALITY_ID);
	}

	@Test
	void patchMessage() {
		// Arrange
		final var request = MessageRequest.builder().build();

		// Act
		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL).build(MUNICIPALITY_ID))
			.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
			.bodyValue(request)
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE)
			.expectBody().isEmpty();

		// Assert
		verify(messageServiceMock).saveMessage(request, MUNICIPALITY_ID);
	}

	@Test
	void setViewedOnMessage() {
		// Arrange
		final var messageID = RandomStringUtils.randomAlphabetic(10);
		final var isViewed = new Random().nextBoolean();

		// Act
		webTestClient.put()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{messageID}/viewed/{isViewed}")
				.build(Map.of(
					"municipalityId", MUNICIPALITY_ID,
					"messageID", messageID,
					"isViewed", isViewed)))
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE)
			.expectBody().isEmpty();

		// Assert
		verify(messageServiceMock).updateViewedStatus(messageID, MUNICIPALITY_ID, isViewed);
	}
}
