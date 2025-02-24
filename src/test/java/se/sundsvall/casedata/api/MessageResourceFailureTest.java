package se.sundsvall.casedata.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.Problem;
import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.service.MessageService;

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
			.header(CONTENT_TYPE, MULTIPART_FORM_DATA_VALUE)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON_VALUE)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		verifyNoInteractions(messageServiceMock);
		assertThat(response.getTitle()).isEqualTo("Bad Request");
		assertThat(response.getDetail()).isEqualTo(
			"Failed to parse multipart servlet request");
	}

}
