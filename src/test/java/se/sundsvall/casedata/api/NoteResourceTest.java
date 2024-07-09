package se.sundsvall.casedata.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.createNoteDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.NoteDTO;
import se.sundsvall.casedata.integration.db.model.enums.NoteType;
import se.sundsvall.casedata.service.NoteService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class NoteResourceTest {

	private static final String BASE_URL = "/{municipalityId}/notes";

	@MockBean
	private NoteService mockService;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getNoteByIdTest() {
		final var id = 153L;
		final var dto = createNoteDTO();
		when(mockService.getNoteByIdAndMunicipalityId(id, MUNICIPALITY_ID)).thenReturn(dto);

		var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_URL + "/{id}").build(Map.of("municipalityId", MUNICIPALITY_ID, "id", id)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(NoteDTO.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isEqualTo(dto);
		verify(mockService).getNoteByIdAndMunicipalityId(id, MUNICIPALITY_ID);
	}

	@Test
	void getNotesByErrandIdTest() {
		final var errandId = 146L;
		final Optional<NoteType> noteType = Optional.empty();
		final var dto1 = createNoteDTO();
		final var dto2 = createNoteDTO();
		when(mockService.getNotesByErrandIdAndMunicipalityIdAndNoteType(errandId, MUNICIPALITY_ID, noteType)).thenReturn(List.of(dto1, dto2));

		var response = webTestClient.get()
			.uri(builder -> builder.path(BASE_URL + "/errand/{errandId}")
				.queryParam("noteType", noteType)
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "errandId", errandId)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(NoteDTO.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).containsExactly(dto1, dto2).hasSize(2);
		verify(mockService).getNotesByErrandIdAndMunicipalityIdAndNoteType(errandId, MUNICIPALITY_ID, noteType);
	}

	@Test
	void deleteNoteByIdTest() {
		final var id = 153L;
		doNothing().when(mockService).deleteNoteByIdAndMunicipalityId(id, MUNICIPALITY_ID);

		webTestClient.delete()
			.uri(builder -> builder.path(BASE_URL + "/{id}")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "id", id)))
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		verify(mockService).deleteNoteByIdAndMunicipalityId(id, MUNICIPALITY_ID);
	}

	@Test
	void patchNoteOnErrand() {
		final var id = 153L;
		final var dto = createNoteDTO();
		doNothing().when(mockService).updateNote(id, MUNICIPALITY_ID, dto);

		webTestClient.patch()
			.uri(builder -> builder.path(BASE_URL + "/{id}")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "id", id)))
			.bodyValue(dto)
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		verify(mockService).updateNote(id, MUNICIPALITY_ID, dto);
	}

}
