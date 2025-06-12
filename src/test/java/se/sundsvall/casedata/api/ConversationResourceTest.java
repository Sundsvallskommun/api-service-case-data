package se.sundsvall.casedata.api;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.web.reactive.function.BodyInserters.fromMultipartData;
import static se.sundsvall.casedata.api.model.conversation.ConversationType.INTERNAL;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.MessageRequest;
import se.sundsvall.casedata.api.model.conversation.Conversation;
import se.sundsvall.casedata.api.model.conversation.Identifier;
import se.sundsvall.casedata.api.model.conversation.KeyValues;
import se.sundsvall.casedata.api.model.conversation.Message;
import se.sundsvall.casedata.service.ConversationService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class ConversationResourceTest {

	private static final String NAMESPACE = "namespace";
	private static final String MUNICIPALITY_ID = "2281";
	private static final String BASE_URL = "/{municipalityId}/{namespace}/errands/{errandId}/communication/conversations";
	private static final Long ERRAND_ID = 123L;
	private static final String CONVERSATION_ID = randomUUID().toString();

	@MockitoBean
	private ConversationService conversationServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	private static Conversation conversation() {
		return Conversation.builder()
			.withMetadata(List.of(KeyValues.builder()
				.withKey("theMetadata")
				.withValues(List.of("metadataValue"))
				.build()))
			.withParticipants(List.of(Identifier.builder()
				.withType("adAccount")
				.withValue("joe01doe")
				.build()))
			.withTopic("The topic")
			.withType(INTERNAL)
			.build();
	}

	@Test
	void createConversation() {

		final var request = conversation();
		final var conversationId = "0";

		when(conversationServiceMock.createConversation(MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, request))
			.thenReturn(conversationId);

		webTestClient.post()
			.uri(builder -> builder.path(BASE_URL)
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", ERRAND_ID)))
			.accept(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL)
			.expectHeader().location("/" + MUNICIPALITY_ID + "/" + NAMESPACE + "/errands/" + ERRAND_ID + "/communication/conversations/" + conversationId)
			.expectHeader().contentType(ALL_VALUE)
			.expectBody().isEmpty();

		verify(conversationServiceMock).createConversation(MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, request);
		verifyNoMoreInteractions(conversationServiceMock);
	}

	@Test
	void getConversation() {

		// Arrange
		when(conversationServiceMock.getConversation(MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, CONVERSATION_ID))
			.thenReturn(conversation());

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_URL + "/{conversationId}")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", ERRAND_ID, "conversationId", CONVERSATION_ID)))
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(Conversation.class)
			.returnResult();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getResponseBody()).isNotNull();

		verify(conversationServiceMock).getConversation(MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, CONVERSATION_ID);
		verifyNoMoreInteractions(conversationServiceMock);
	}

	@Test
	void getConversations() {

		// Arrange
		when(conversationServiceMock.getConversations(MUNICIPALITY_ID, NAMESPACE, ERRAND_ID))
			.thenReturn(List.of(conversation()));

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_URL)
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", ERRAND_ID)))
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(Conversation.class)
			.returnResult();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getResponseBody()).isNotNull().hasSize(1);

		verify(conversationServiceMock).getConversations(MUNICIPALITY_ID, NAMESPACE, ERRAND_ID);
		verifyNoMoreInteractions(conversationServiceMock);
	}

	@Test
	void updateConversation() {

		// Arrange
		final var conversationId = randomUUID().toString();
		final var request = conversation();

		when(conversationServiceMock.updateConversation(MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, conversationId, request))
			.thenReturn(request);
		// Act
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(BASE_URL + "/{conversationId}")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", ERRAND_ID, "conversationId", conversationId)))
			.accept(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(Conversation.class)
			.returnResult();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getResponseBody()).isNotNull();

		verify(conversationServiceMock).updateConversation(MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, conversationId, request);
		verifyNoMoreInteractions(conversationServiceMock);
	}

	@Test
	void createMessage() {

		// Arrange
		final var messageRequest = Message.builder()
			.withContent("content")
			.build();

		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("attachments", "file-content").filename("test1.txt").contentType(TEXT_PLAIN);
		multipartBodyBuilder.part("attachments", "file-content").filename("tesst2.txt").contentType(TEXT_PLAIN);
		multipartBodyBuilder.part("message", messageRequest);

		// Act
		final var response = webTestClient.post()
			.uri(builder -> builder.path(BASE_URL + "/{conversationId}/messages")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", ERRAND_ID, "conversationId", CONVERSATION_ID)))
			.contentType(MULTIPART_FORM_DATA)
			.body(fromMultipartData(multipartBodyBuilder.build()))
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE)
			.expectBody().isEmpty();

		// Assert
		assertThat(response).isNotNull();

		verify(conversationServiceMock).createMessage(eq(MUNICIPALITY_ID), eq(NAMESPACE), eq(ERRAND_ID), eq(CONVERSATION_ID), eq(messageRequest), any());
		verifyNoMoreInteractions(conversationServiceMock);
	}

	@Test
	void getMessages() {

		// Arrange
		final var messageRequest = Message.builder()
			.build();
		final var page = new PageImpl<>(List.of(messageRequest));

		when(conversationServiceMock.getMessages(eq(MUNICIPALITY_ID), eq(NAMESPACE), eq(ERRAND_ID), eq(CONVERSATION_ID), any()))
			.thenReturn(page);

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_URL + "/{conversationId}/messages")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", ERRAND_ID, "conversationId", CONVERSATION_ID)))
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(MessageRequest.class)
			.returnResult();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getResponseBody()).isNotNull().hasSize(1);

		verify(conversationServiceMock).getMessages(eq(MUNICIPALITY_ID), eq(NAMESPACE), eq(ERRAND_ID), eq(CONVERSATION_ID), any());
		verifyNoMoreInteractions(conversationServiceMock);
	}
}
