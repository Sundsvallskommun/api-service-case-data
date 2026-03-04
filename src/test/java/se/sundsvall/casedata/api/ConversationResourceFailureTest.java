package se.sundsvall.casedata.api;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.conversation.Conversation;
import se.sundsvall.casedata.api.model.conversation.ConversationType;
import se.sundsvall.casedata.api.model.conversation.Message;
import se.sundsvall.casedata.service.ConversationService;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.problem.violations.Violation;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.web.reactive.function.BodyInserters.fromMultipartData;

@AutoConfigureWebTestClient
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class ConversationResourceFailureTest {

	private static final String NAMESPACE = "namespace";
	private static final String MUNICIPALITY_ID = "2281";
	private static final String BASE_URL = "/{municipalityId}/{namespace}/errands/{errandId}/communication/conversations";
	private static final String ERRAND_ID = "123";
	private static final String CONVERSATION_ID = randomUUID().toString();
	private static final String INVALID = "#invalid";
	private static final String MESSAGE_ID = randomUUID().toString();
	private static final String ATTACHMENT_ID = randomUUID().toString();

	@MockitoBean
	private ConversationService conversationServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void createConversationWithEmptyRequestBody() {

		// Call
		final var response = webTestClient.post()
			.uri(builder -> builder.path(BASE_URL).build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID, "errandId", ERRAND_ID)))
			.contentType(APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo(BAD_REQUEST.getReasonPhrase());
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getDetail()).isEqualTo("Failed to read request");

		// Verification
		verifyNoInteractions(conversationServiceMock);
	}

	@Test
	void createConversationWithNoType() {

		// Arrange
		final var request = Conversation.builder()
			.withType(null)
			.withTopic("The topic")
			.build();

		// Call
		final var response = webTestClient.post()
			.uri(builder -> builder.path(BASE_URL).build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID, "errandId", ERRAND_ID)))
			.accept(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("type", "must not be null"));

		// Verification
		verifyNoInteractions(conversationServiceMock);
	}

	@Test
	void createConversationWithBlankTopic() {

		// Arrange
		final var request = Conversation.builder()
			.withType(ConversationType.EXTERNAL)
			.withTopic(" ")
			.build();

		// Call
		final var response = webTestClient.post()
			.uri(builder -> builder.path(BASE_URL).build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID, "errandId", ERRAND_ID)))
			.accept(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("topic", "must not be blank"));

		// Verification
		verifyNoInteractions(conversationServiceMock);
	}

	@Test
	void getConversationsWithInvalidMunicipalityId() {

		// Call
		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_URL).build(Map.of("namespace", NAMESPACE, "municipalityId", INVALID, "errandId", ERRAND_ID)))
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("getConversations.municipalityId", "not a valid municipality ID"));

		// Verification
		verifyNoInteractions(conversationServiceMock);
	}

	@Test
	void getConversationsWithInvalidNamespace() {

		// Call
		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_URL).build(Map.of("namespace", INVALID, "municipalityId", MUNICIPALITY_ID, "errandId", ERRAND_ID)))
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("getConversations.namespace", "can only contain A-Z, a-z, 0-9, - and _"));

		// Verification
		verifyNoInteractions(conversationServiceMock);
	}

	@Test
	void getConversationByIdWithInvalidConversationId() {

		// Call
		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_URL + "/{conversationId}")
				.build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID, "errandId", ERRAND_ID, "conversationId", INVALID)))
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("getConversation.conversationId", "not a valid UUID"));

		// Verification
		verifyNoInteractions(conversationServiceMock);
	}

	@Test
	void updateConversationWithInvalidConversationId() {

		// Arrange
		final var request = Conversation.builder()
			.withType(ConversationType.INTERNAL)
			.withTopic("The topic")
			.build();

		// Call
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(BASE_URL + "/{conversationId}")
				.build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID, "errandId", ERRAND_ID, "conversationId", INVALID)))
			.accept(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("updateConversation.conversationId", "not a valid UUID"));

		// Verification
		verifyNoInteractions(conversationServiceMock);
	}

	@Test
	void updateConversationWithInvalidBody() {

		// Arrange
		final var request = Conversation.builder().build();

		// Call
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(BASE_URL + "/{conversationId}")
				.build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID, "errandId", ERRAND_ID, "conversationId", CONVERSATION_ID)))
			.accept(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactlyInAnyOrder(
				tuple("topic", "must not be blank"),
				tuple("type", "must not be null"));

		// Verification
		verifyNoInteractions(conversationServiceMock);
	}

	@Test
	void updateConversationsEmptyRequestBody() {

		// Call
		final var response = webTestClient.patch()
			.uri(builder -> builder.path(BASE_URL + "/{conversationId}")
				.build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID, "errandId", ERRAND_ID, "conversationId", CONVERSATION_ID)))
			.contentType(APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo(BAD_REQUEST.getReasonPhrase());
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getDetail()).isEqualTo("Failed to read request");

		// Verification
		verifyNoInteractions(conversationServiceMock);
	}

	@Test
	void createMessageEmptyRequestBody() {

		// Call
		final var response = webTestClient.post()
			.uri(builder -> builder.path(BASE_URL + "/{conversationId}/messages")
				.build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID, "errandId", ERRAND_ID, "conversationId", CONVERSATION_ID)))
			.contentType(MULTIPART_FORM_DATA)
			.bodyValue(new MultipartBodyBuilder().build())
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo(BAD_REQUEST.getReasonPhrase());
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getDetail()).isEqualTo("Required part 'message' is not present.");

		// Verification
		verifyNoInteractions(conversationServiceMock);
	}

	@Test
	void createMessageInvalidMessageAttributes() {

		// Arrange
		final var messageRequest = Message.builder()
			.withContent(" ")
			.withInReplyToMessageId("invalid")
			.build();

		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("message", messageRequest).contentType(APPLICATION_JSON);

		// Call
		final var response = webTestClient.post()
			.uri(builder -> builder.path(BASE_URL + "/{conversationId}/messages")
				.build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID, "errandId", ERRAND_ID, "conversationId", CONVERSATION_ID)))
			.contentType(MULTIPART_FORM_DATA)
			.body(fromMultipartData(multipartBodyBuilder.build()))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactlyInAnyOrder(
				tuple("content", "must not be blank"),
				tuple("inReplyToMessageId", "not a valid UUID"));

		// Verification
		verifyNoInteractions(conversationServiceMock);
	}

	@Test
	void getMessageAttachmentsWithInvalidConversationId() {

		// Call
		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_URL + "/{conversationId}/messages/{messageId}/attachments/{attachmentId}")
				.build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID, "errandId", ERRAND_ID, "conversationId", INVALID, "messageId", MESSAGE_ID, "attachmentId", ATTACHMENT_ID)))
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("getConversationMessageAttachment.conversationId", "not a valid UUID"));

		// Verification
		verifyNoInteractions(conversationServiceMock);
	}

	@Test
	void getMessageAttachmentsWithInvalidNamespace() {

		// Call
		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_URL + "/{conversationId}/messages/{messageId}/attachments/{attachmentId}")
				.build(Map.of("namespace", INVALID, "municipalityId", MUNICIPALITY_ID, "errandId", ERRAND_ID, "conversationId", CONVERSATION_ID, "messageId", MESSAGE_ID, "attachmentId", ATTACHMENT_ID)))
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("getConversationMessageAttachment.namespace", "can only contain A-Z, a-z, 0-9, - and _"));

		// Verification
		verifyNoInteractions(conversationServiceMock);

	}

	@Test
	void getMessageAttachmentsWithInvalidMunicipalityId() {
		// Call
		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_URL + "/{conversationId}/messages/{messageId}/attachments/{attachmentId}")
				.build(Map.of("namespace", NAMESPACE, "municipalityId", INVALID, "errandId", ERRAND_ID, "conversationId", CONVERSATION_ID, "messageId", MESSAGE_ID, "attachmentId", ATTACHMENT_ID)))
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("getConversationMessageAttachment.municipalityId", "not a valid municipality ID"));

		// Verification
		verifyNoInteractions(conversationServiceMock);
	}

	@Test
	void getMessageAttachmentsWithInvalidMessageId() {

		// Call
		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_URL + "/{conversationId}/messages/{messageId}/attachments/{attachmentId}")
				.build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID, "errandId", ERRAND_ID, "conversationId", CONVERSATION_ID, "messageId", INVALID, "attachmentId", ATTACHMENT_ID)))
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("getConversationMessageAttachment.messageId", "not a valid UUID"));
		// Verification
		verifyNoInteractions(conversationServiceMock);

	}

	@Test
	void getMessageAttachmentsWithInvalidAttachmentId() {

		// Call
		final var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_URL + "/{conversationId}/messages/{messageId}/attachments/{attachmentId}")
				.build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID, "errandId", ERRAND_ID, "conversationId", CONVERSATION_ID, "messageId", MESSAGE_ID, "attachmentId", INVALID)))
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("getConversationMessageAttachment.attachmentId", "not a valid UUID"));

		// Verification
		verifyNoInteractions(conversationServiceMock);

	}
}
