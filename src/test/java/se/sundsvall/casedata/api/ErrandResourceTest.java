package se.sundsvall.casedata.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static se.sundsvall.casedata.TestUtil.createDecisionDTO;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.casedata.CaseDataApplication;
import se.sundsvall.casedata.api.model.DecisionDTO;
import se.sundsvall.casedata.service.ErrandService;

@SpringBootTest(classes = CaseDataApplication.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class ErrandResourceTest {

	private static final String PATH = "errands";

	@MockBean
	private ErrandService errandServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void deleteDecisionTest() {
		final var errandId = 123L;
		final var decisionId = 456L;

		doNothing().when(errandServiceMock).deleteDecisionOnErrand(errandId, decisionId);

		webTestClient.delete()
			.uri(uriBuilder -> uriBuilder.path(PATH + "/{errandId}/decisions/{decisionId}")
				.build(Map.of("errandId", errandId, "decisionId", decisionId)))
			.exchange()
			.expectStatus().isNoContent();

		verify(errandServiceMock).deleteDecisionOnErrand(errandId, decisionId);
		verifyNoMoreInteractions(errandServiceMock);
	}

	@Test
	void deleteNoteTest() {
		final var errandId = 123L;
		final var noteId = 456L;

		doNothing().when(errandServiceMock).deleteNoteOnErrand(errandId, noteId);

		webTestClient.delete()
			.uri(uriBuilder -> uriBuilder.path(PATH + "/{errandId}/notes/{noteId}")
				.build(Map.of("errandId", errandId, "noteId", noteId)))
			.exchange()
			.expectStatus().isNoContent();

		verify(errandServiceMock).deleteNoteOnErrand(errandId, noteId);
		verifyNoMoreInteractions(errandServiceMock);
	}

	@Test
	void getDecisionTest() {
		final var errandId = 123L;
		final var decisionDto = createDecisionDTO();
		when(errandServiceMock.findDecisionsOnErrand(errandId)).thenReturn(List.of(decisionDto));

		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(PATH + "/{errandId}/decisions")
				.build(Map.of("errandId", errandId)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(DecisionDTO.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).hasSize(1);
		verify(errandServiceMock).findDecisionsOnErrand(errandId);
		verifyNoMoreInteractions(errandServiceMock);
	}

}
