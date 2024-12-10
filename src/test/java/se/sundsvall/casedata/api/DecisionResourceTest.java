package se.sundsvall.casedata.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.TestUtil.createPatchDecision;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.TestUtil;
import se.sundsvall.casedata.api.model.Decision;
import se.sundsvall.casedata.service.DecisionService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class DecisionResourceTest {

	private static final String BASE_URL = "/{municipalityId}/{namespace}/errands";

	@MockitoBean
	private DecisionService decisionServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getDecisionById() {
		// Arrange
		final var errandId = 123L;
		final var decisionId = 456L;
		final var decisionDTO = TestUtil.createDecision();
		when(decisionServiceMock.findDecisionOnErrand(errandId, decisionId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(decisionDTO);

		// Act
		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/decisions/{decisionId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, decisionId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(Decision.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		verify(decisionServiceMock).findDecisionOnErrand(errandId, decisionId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(decisionServiceMock);
	}

	@Test
	void getDecision() {
		// Arrange
		final var errandId = 123L;
		final var decisionDto = TestUtil.createDecision();
		when(decisionServiceMock.findDecisionsOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(List.of(decisionDto));

		// Act
		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/decisions").build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(Decision.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).hasSize(1);
		verify(decisionServiceMock).findDecisionsOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(decisionServiceMock);
	}

	@Test
	void createDecision() {
		// Arrange
		final var errandId = 123L;
		final var decisionId = 456L;
		final var body = TestUtil.createDecision();
		body.setId(decisionId);
		when(decisionServiceMock.addDecisionToErrand(errandId, MUNICIPALITY_ID, NAMESPACE, body)).thenReturn(body);

		// Act
		webTestClient.patch()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/decisions").build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL_VALUE)
			.expectHeader().location("/2281/my.namespace/decisions/" + decisionId);

		// Assert
		verify(decisionServiceMock).addDecisionToErrand(errandId, MUNICIPALITY_ID, NAMESPACE, body);
	}

	@Test
	void patchDecision() {
		// Arrange
		final var errandId = 123L;
		final var decisionId = 456L;
		final var body = createPatchDecision();

		// Act
		webTestClient.patch()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/decisions/{decisionId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, decisionId))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		// Assert
		verify(decisionServiceMock).updateDecisionOnErrand(errandId, decisionId, MUNICIPALITY_ID, NAMESPACE, body);
	}

	@Test
	void putDecision() {
		// Arrange
		final var errandId = 123L;
		final var decisionId = 456L;
		final var body = TestUtil.createDecision();
		body.setId(decisionId);

		// Act
		webTestClient.put()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/decisions/{decisionId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, decisionId))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		// Assert
		verify(decisionServiceMock).replaceDecisionOnErrand(errandId, decisionId, MUNICIPALITY_ID, NAMESPACE, body);
	}

	@Test
	void deleteDecision() {
		// Arrange
		final var errandId = 123L;
		final var decisionId = 456L;

		// Act
		webTestClient.delete()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/decisions/{decisionId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, decisionId))
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		// Assert
		verify(decisionServiceMock).deleteDecisionOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE, decisionId);
		verifyNoMoreInteractions(decisionServiceMock);
	}

}
