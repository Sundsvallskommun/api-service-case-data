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
import static se.sundsvall.casedata.TestUtil.createDecisionDTO;
import static se.sundsvall.casedata.TestUtil.createPatchDecisionDto;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.DecisionDTO;
import se.sundsvall.casedata.service.DecisionService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class DecisionResourceTest {

	private static final String BASE_URL = "/{municipalityId}/{namespace}/errands";

	@MockBean
	private DecisionService decisionServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getDecisionById() {
		final var errandId = 123L;
		final var decisionId = 456L;
		final var decisionDTO = createDecisionDTO();

		when(decisionServiceMock.findByIdAndMunicipalityIdAndNamespace(decisionId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(decisionDTO);

		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/decisions/{decisionId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, decisionId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(DecisionDTO.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		verify(decisionServiceMock).findByIdAndMunicipalityIdAndNamespace(decisionId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(decisionServiceMock);
	}

	@Test
	void getDecision() {
		final var errandId = 123L;
		final var decisionDto = createDecisionDTO();

		when(decisionServiceMock.findDecisionsOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(List.of(decisionDto));

		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/decisions").build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(DecisionDTO.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).hasSize(1);
		verify(decisionServiceMock).findDecisionsOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(decisionServiceMock);
	}

	@Test
	void createDecision() {
		final var errandId = 123L;
		final var decisionId = 456L;
		final var body = createDecisionDTO();
		body.setId(decisionId);

		when(decisionServiceMock.addDecisionToErrand(errandId, MUNICIPALITY_ID, NAMESPACE, body)).thenReturn(body);

		webTestClient.patch()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/decisions").build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL_VALUE)
			.expectHeader().location("/2281/my.namespace/decisions/" + decisionId);

		verify(decisionServiceMock).addDecisionToErrand(errandId, MUNICIPALITY_ID, NAMESPACE, body);
	}

	@Test
	void patchDecision() {
		final var errandId = 123L;
		final var decisionId = 456L;
		final var body = createPatchDecisionDto();

		webTestClient.patch()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/decisions/{decisionId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, decisionId))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		verify(decisionServiceMock).updateDecision(decisionId, MUNICIPALITY_ID, NAMESPACE, body);
	}


	@Test
	void putDecision() {
		final var errandId = 123L;
		final var decisionId = 456L;
		final var body = createDecisionDTO();
		body.setId(decisionId);

		webTestClient.put()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/decisions/{decisionId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, decisionId))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		verify(decisionServiceMock).replaceDecision(decisionId, MUNICIPALITY_ID, NAMESPACE, body);
	}

	@Test
	void deleteDecision() {
		final var errandId = 123L;
		final var decisionId = 456L;

		webTestClient.delete()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/decisions/{decisionId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, decisionId))
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		verify(decisionServiceMock).deleteDecisionOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE, decisionId);
		verifyNoMoreInteractions(decisionServiceMock);
	}

}
