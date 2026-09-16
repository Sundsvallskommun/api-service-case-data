package se.sundsvall.casedata.api;

import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.BulkEmailRequest;
import se.sundsvall.casedata.api.model.MessageRequest;
import se.sundsvall.casedata.service.MessageService;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;

@AutoConfigureWebTestClient
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class MessageResourceFailureTest {

	private static final String PATH = "/{municipalityId}/{namespace}/errands/{errandId}/messages";

	@MockitoBean
	private MessageService messageServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void patchMessageWithNoBody() {
		// Act
		final var response = webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(PATH).build(MUNICIPALITY_ID, NAMESPACE, 1L))
			.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON_VALUE)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		verifyNoInteractions(messageServiceMock);
		assertThat(response.getTitle()).isEqualTo("Bad Request");
		assertThat(response.getDetail()).isEqualTo("Failed to read request");
	}

	@Test
	void postBulkEmailWithNoRecipients() {
		// Arrange
		final var request = BulkEmailRequest.builder()
			.withRecipients(List.of())
			.withSubject("Subject")
			.withMessage("Message")
			.withDepartmentName("CONVERSATION")
			.build();

		// Act
		final var response = webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(PATH + "/email/batch").build(MUNICIPALITY_ID, NAMESPACE, 1L))
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON_VALUE)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		verifyNoInteractions(messageServiceMock);
		assertThat(response.getViolations())
			.extracting("field")
			.contains("recipients");
	}

	@Test
	void postBulkEmailWithTooManyRecipients() {
		// Arrange - recipients is capped since each is sent as a synchronous call to Messaging's batch endpoint
		final var request = BulkEmailRequest.builder()
			.withRecipients(IntStream.range(0, 201)
				.mapToObj(i -> "recipient" + i + "@example.com")
				.toList())
			.withSubject("Subject")
			.withMessage("Message")
			.withDepartmentName("CONVERSATION")
			.build();

		// Act
		final var response = webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(PATH + "/email/batch").build(MUNICIPALITY_ID, NAMESPACE, 1L))
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON_VALUE)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		verifyNoInteractions(messageServiceMock);
		assertThat(response.getViolations())
			.extracting("field")
			.contains("recipients");
	}

	@Test
	void postBulkEmailWithInvalidAttachment() {
		// Arrange - attachment content is blank and its name is missing, both of which are only caught if
		// BulkEmailRequest.attachments cascades validation into each AttachmentRequest via @Valid
		final var request = BulkEmailRequest.builder()
			.withRecipients(List.of("recipient@example.com"))
			.withSubject("Subject")
			.withMessage("Message")
			.withDepartmentName("CONVERSATION")
			.withAttachments(List.of(MessageRequest.AttachmentRequest.builder()
				.withContent("")
				.build()))
			.build();

		// Act
		final var response = webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(PATH + "/email/batch").build(MUNICIPALITY_ID, NAMESPACE, 1L))
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON_VALUE)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		verifyNoInteractions(messageServiceMock);
		assertThat(response.getViolations())
			.extracting("field")
			.contains("attachments[0].content", "attachments[0].name");
	}

}
