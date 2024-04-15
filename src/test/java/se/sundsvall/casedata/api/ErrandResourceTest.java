package se.sundsvall.casedata.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.sundsvall.casedata.TestUtil.createDecisionDTO;
import static se.sundsvall.casedata.TestUtil.createErrandDTO;
import static se.sundsvall.casedata.TestUtil.createFacilityDTO;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.DecisionDTO;
import se.sundsvall.casedata.api.model.validation.enums.FacilityType;
import se.sundsvall.casedata.service.ErrandService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class ErrandResourceTest {

	private static final String PATH = "errands";

	@MockBean
	private ErrandService errandServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void deleteErrand() {
		final var errandId = 123L;

		webTestClient.delete()
			.uri(uriBuilder -> uriBuilder.path(PATH + "/{errandId}")
				.build(Map.of("errandId", errandId)))
			.exchange()
			.expectStatus().isNoContent();

		verify(errandServiceMock).deleteById(errandId);
		verifyNoMoreInteractions(errandServiceMock);
	}

	@Test
	void deleteDecision() {
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
	void deleteNote() {
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
	void getDecision() {
		final var errandId = 123L;
		final var decisionDto = createDecisionDTO();
		when(errandServiceMock.findDecisionsOnErrand(errandId)).thenReturn(List.of(decisionDto));

		final var response = webTestClient.get()
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

	@ParameterizedTest
	@EnumSource(FacilityType.class)
	void postErrandWithFacilityType(final FacilityType facilityType) {
		final var body = createErrandDTO();
		body.setId(123L);
		final var facility = createFacilityDTO();
		facility.setFacilityType(facilityType.name());
		final var facilities = List.of(facility);
		body.setFacilities(facilities);

		when(errandServiceMock.createErrand(body)).thenReturn(body);

		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path("/errands").build())
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL_VALUE)
			.expectHeader().location("/errands/" + body.getId());

		verify(errandServiceMock).createErrand(body);

	}
}
