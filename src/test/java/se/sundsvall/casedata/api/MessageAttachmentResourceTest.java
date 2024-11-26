package se.sundsvall.casedata.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.service.MessageService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class MessageAttachmentResourceTest {

	private static final String PATH = "/{municipalityId}/{namespace}/errands/{errandId}/messageattachments/{attachmentId}/streamed";

	@MockBean
	private MessageService messageServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getMessageAttachmentStreamed() {
		// Arrange
		final var errandId = 123L;
		final var attachmentId = "attachment123";

		// Act
		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(PATH).build(MUNICIPALITY_ID, NAMESPACE, errandId, attachmentId))
			.exchange()
			.expectStatus().isOk();

		// Assert
		verify(messageServiceMock).getMessageAttachmentStreamed(eq(errandId), eq(attachmentId), eq(MUNICIPALITY_ID), eq(NAMESPACE), any());
		verifyNoMoreInteractions(messageServiceMock);
	}
}
