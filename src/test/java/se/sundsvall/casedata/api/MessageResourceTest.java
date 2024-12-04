package se.sundsvall.casedata.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;

import java.util.List;
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

	private static final String BASE_URL = "/{municipalityId}/{namespace}/errands/{errandId}/messages";

	@MockBean
	private MessageService messageServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getMessagesOnErrand() {

		// Arrange
		final var errandNumber = RandomStringUtils.secure().nextAlphabetic(10);
		final var messages = List.of(MessageResponse.builder().build());

		when(messageServiceMock.getMessagesByErrandNumber(errandNumber, MUNICIPALITY_ID, NAMESPACE)).thenReturn(messages);

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/{municipalityId}/{namespace}/messages/{errandNumber}").build(MUNICIPALITY_ID, NAMESPACE, errandNumber))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBodyList(MessageResponse.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isEqualTo(messages);
		verify(messageServiceMock).getMessagesByErrandNumber(errandNumber, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(messageServiceMock);
	}

	@Test
	void patchErrandWithMessage() {
		// Arrange
		final var errandId = 123L;
		final var request = MessageRequest.builder().build();

		// Act
		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL).build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		// Assert
		verify(messageServiceMock).saveMessageOnErrand(errandId, request, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(messageServiceMock);
	}

	@Test
	void updateViewedStatus() {
		// Arrange
		final var errandId = 123L;
		final var messageId = RandomStringUtils.secure().nextAlphabetic(10);
		final var isViewed = new Random().nextBoolean();

		// Act
		webTestClient.put()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{messageId}/viewed/{isViewed}").build(MUNICIPALITY_ID, NAMESPACE, errandId, messageId, isViewed))
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		// Assert
		verify(messageServiceMock).updateViewedStatus(errandId, messageId, MUNICIPALITY_ID, NAMESPACE, isViewed);
		verifyNoMoreInteractions(messageServiceMock);
	}

	@Test
	void getMessageAttachmentStreamed() {
		// Arrange
		final var errandId = 123L;
		final var messageId = RandomStringUtils.secure().nextAlphabetic(10);
		final var attachmentId = RandomStringUtils.secure().nextAlphabetic(10);

		// Act
		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{messageId}/attachments/{attachmentId}/streamed").build(MUNICIPALITY_ID, NAMESPACE, errandId, messageId, attachmentId))
			.exchange()
			.expectStatus().isOk();

		// Assert
		verify(messageServiceMock).getMessageAttachmentStreamed(eq(errandId), eq(attachmentId), eq(MUNICIPALITY_ID), eq(NAMESPACE), any());
		verifyNoMoreInteractions(messageServiceMock);
	}
}
