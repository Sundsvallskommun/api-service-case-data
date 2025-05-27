package se.sundsvall.casedata.api;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.conversation.Conversation;
import se.sundsvall.casedata.api.model.conversation.ConversationRequest;
import se.sundsvall.casedata.api.model.conversation.Identifier;
import se.sundsvall.casedata.api.model.conversation.KeyValues;
import se.sundsvall.casedata.api.model.conversation.MessageRequest;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class ConversationResourceTest {

	private static final String NAMESPACE = "namespace";
	private static final String MUNICIPALITY_ID = "2281";
	private static final String BASE_URL = "/{municipalityId}/{namespace}/errands/{errandId}/communication/conversations";
	private static final String ERRAND_ID = "123";
	private static final String CONVERSATION_ID = randomUUID().toString();

	@Autowired
	private WebTestClient webTestClient;

	private static ConversationRequest conversationRequest() {
		return ConversationRequest.builder()
			.withExternalReferences(List.of(KeyValues.builder()
				.withKey("theKey")
				.withValues(List.of("externalReferenceValue"))
				.build()))
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

		final var request = conversationRequest();

		webTestClient.post()
			.uri(builder -> builder.path(BASE_URL)
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", ERRAND_ID)))
			.accept(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL)
			.expectHeader().location("/" + MUNICIPALITY_ID + "/" + NAMESPACE + "/errands/" + ERRAND_ID + "/communication/conversations/0")
			.expectHeader().contentType(ALL_VALUE)
			.expectBody().isEmpty();

		// TODO: Verify service call
	}

	@Test
	void getConversation() {

		// Arrange
		// TODO: Mock service call to return a conversation

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

		// TODO: Verify service call

	}

	@Test
	void getConversations() {

		// Arrange
		// TODO: Mock service call to return a list of conversations

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

		// TODO: Verify service call
	}

	@Test
	void updateConversation() {

		// Arrange
		final var conversationId = randomUUID().toString();
		final var request = conversationRequest();
		// TODO: Mock service.

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

		// TODO: Verify service call

	}

	@Test
	void createMessage() {

		// Arrange
		final var messageRequest = MessageRequest.builder()
			.withContent("content")
			.build();

		// TODO: Mock service.

		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("attachments", "file-content").filename("test1.txt").contentType(TEXT_PLAIN);
		multipartBodyBuilder.part("attachments", "file-content").filename("tesst2.txt").contentType(TEXT_PLAIN);
		multipartBodyBuilder.part("message", messageRequest);

		// TODO: Mock service.

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

		// TODO: Verification of service call.

	}

	@Test
	void getMessages() {

		// Arrange
		// TODO: Mock service call to return a list of messages

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
	}
}
