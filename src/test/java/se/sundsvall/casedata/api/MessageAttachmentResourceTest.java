package se.sundsvall.casedata.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.MessageAttachment;
import se.sundsvall.casedata.service.MessageService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class MessageAttachmentResourceTest {

	private static final String BASE_URL = "/{municipalityId}/{namespace}/errands/{errandId}/messageattachments";

	@MockBean
	private MessageService messageServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getMessageAttachment() {
		// Arrange
		final var errandId = 123L;
		final var attachmentId = "attachment123";
		final var messageAttachment = new MessageAttachment();
		when(messageServiceMock.getMessageAttachment(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(messageAttachment);

		// Act
		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{attachmentId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, attachmentId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(MessageAttachment.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		verify(messageServiceMock).getMessageAttachment(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(messageServiceMock);
	}

	@Test
	void getMessageAttachmentStreamed() {
		// Arrange
		final var errandId = 123L;
		final var attachmentId = "attachment123";

		// Act
		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{attachmentId}/streamed").build(MUNICIPALITY_ID, NAMESPACE, errandId, attachmentId))
			.exchange()
			.expectStatus().isOk();

		// Assert
		verify(messageServiceMock).getMessageAttachmentStreamed(eq(errandId), eq(attachmentId), eq(MUNICIPALITY_ID), eq(NAMESPACE), any());
		verifyNoMoreInteractions(messageServiceMock);
	}

}
