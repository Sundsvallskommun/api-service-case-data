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
import static se.sundsvall.casedata.TestUtil.createNote;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.Note;
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
		// Arrange
		final var errandId = 123L;
		final var noteId = 456L;
		final var note = createNote();

		when(noteServiceMock.getNoteOnErrand(errandId, noteId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(note);

		// Act
		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{noteId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, noteId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(Note.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		verify(noteServiceMock).getNoteOnErrand(errandId, noteId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(noteServiceMock);
	}

	@Test
	void getNotesByErrandId() {
		// Arrange
		final var errandId = 123L;
		final var note = createNote();

		when(noteServiceMock.getAllNotesOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE, Optional.empty())).thenReturn(List.of(note));

		// Act
		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL).build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(Note.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).hasSize(1);
		verify(noteServiceMock).getAllNotesOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE, Optional.empty());
		verifyNoMoreInteractions(noteServiceMock);
	}

	@Test
	void updateNoteOnErrand() {
		// Arrange
		final var errandId = 123L;
		final var noteId = 456L;
		final var note = createNote();

		// Act
		webTestClient.patch()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{noteId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, noteId))
			.contentType(APPLICATION_JSON)
			.bodyValue(note)
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		// Assert
		verify(noteServiceMock).updateNoteOnErrand(errandId, noteId, MUNICIPALITY_ID, NAMESPACE, note);
		verifyNoMoreInteractions(noteServiceMock);
	}

	@Test
	void deleteNoteOnErrand() {
		// Arrange
		final var errandId = 123L;
		final var noteId = 456L;

		// Act
		webTestClient.delete()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{noteId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, noteId))
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		// Assert
		verify(noteServiceMock).deleteNoteOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE, noteId);
		verifyNoMoreInteractions(noteServiceMock);
	}

	@Test
	void updateErrandWithNote() {
		// Arrange
		final var errandId = 123L;
		final var noteId = 456L;
		final var note = createNote();
		note.setId(noteId);

		when(noteServiceMock.addNoteToErrand(errandId, MUNICIPALITY_ID, NAMESPACE, note)).thenReturn(note);

		// Act
		webTestClient.patch()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL).build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.contentType(APPLICATION_JSON)
			.bodyValue(note)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL_VALUE)
			.expectHeader().location("/" + MUNICIPALITY_ID + "/" + NAMESPACE + "/notes/" + noteId);

		// Assert
		verify(noteServiceMock).addNoteToErrand(errandId, MUNICIPALITY_ID, NAMESPACE, note);
	}

}
