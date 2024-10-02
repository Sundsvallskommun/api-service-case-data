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
import static se.sundsvall.casedata.TestUtil.createNoteDTO;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.NoteDTO;
import se.sundsvall.casedata.service.NoteService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class NoteResourceTest {

	private static final String BASE_URL = "/{municipalityId}/{namespace}/errands/{errandId}/notes";

	@MockBean
	private NoteService noteServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getNoteById() {
		final var errandId = 123L;
		final var noteId = 456L;
		final var noteDTO = createNoteDTO();

		when(noteServiceMock.getNoteByIdAndMunicipalityIdAndNamespace(noteId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(noteDTO);

		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{noteId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, noteId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(NoteDTO.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		verify(noteServiceMock).getNoteByIdAndMunicipalityIdAndNamespace(noteId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(noteServiceMock);
	}

	@Test
	void getNotesByErrandId() {
		final var errandId = 123L;
		final var noteDTO = createNoteDTO();

		when(noteServiceMock.getNotesByErrandIdAndMunicipalityIdAndNamespaceAndNoteType(errandId, MUNICIPALITY_ID, NAMESPACE, Optional.empty())).thenReturn(List.of(noteDTO));

		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL).build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(NoteDTO.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).hasSize(1);
		verify(noteServiceMock).getNotesByErrandIdAndMunicipalityIdAndNamespaceAndNoteType(errandId, MUNICIPALITY_ID, NAMESPACE, Optional.empty());
		verifyNoMoreInteractions(noteServiceMock);
	}

	@Test
	void patchNoteOnErrand() {
		final var errandId = 123L;
		final var noteId = 456L;
		final var noteDTO = createNoteDTO();

		webTestClient.patch()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{noteId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, noteId))
			.contentType(APPLICATION_JSON)
			.bodyValue(noteDTO)
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		verify(noteServiceMock).updateNote(noteId, MUNICIPALITY_ID, NAMESPACE, noteDTO);
		verifyNoMoreInteractions(noteServiceMock);
	}

	@Test
	void deleteNote() {
		final var errandId = 123L;
		final var noteId = 456L;

		webTestClient.delete()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{noteId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, noteId))
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		verify(noteServiceMock).deleteNoteOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE, noteId);
		verifyNoMoreInteractions(noteServiceMock);
	}

	@Test
	void patchErrandWithNote() {
		final var errandId = 123L;
		final var noteId = 456L;
		final var noteDTO = createNoteDTO();
		noteDTO.setId(noteId);

		when(noteServiceMock.addNoteToErrand(errandId, MUNICIPALITY_ID, NAMESPACE, noteDTO)).thenReturn(noteDTO);

		webTestClient.patch()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL).build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.contentType(APPLICATION_JSON)
			.bodyValue(noteDTO)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL_VALUE)
			.expectHeader().location("/" + MUNICIPALITY_ID + "/" + NAMESPACE + "/notes/" + noteId);

		verify(noteServiceMock).addNoteToErrand(errandId, MUNICIPALITY_ID, NAMESPACE, noteDTO);
	}

}
