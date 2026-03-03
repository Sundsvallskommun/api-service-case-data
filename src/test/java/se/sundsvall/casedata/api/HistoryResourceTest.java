package se.sundsvall.casedata.api;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.history.History;
import se.sundsvall.casedata.service.HistoryService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class HistoryResourceTest {

	private static final String BASE_URL = "/{municipalityId}/{namespace}/errands/{errandId}";

	@MockitoBean
	private HistoryService historyServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getAttachmentHistory() {
		// Arrange
		final var errandId = 123L;
		final var attachmentId = 456L;
		final var history = new History();
		final var historyList = List.of(history);
		when(historyServiceMock.findAttachmentHistoryOnErrand(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(historyList);

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/attachments/{attachmentId}/history").build(MUNICIPALITY_ID, NAMESPACE, errandId, attachmentId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(History.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull().hasSize(1);
		assertThat(response.getFirst()).isEqualTo(history);
		verify(historyServiceMock).findAttachmentHistoryOnErrand(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(historyServiceMock);
	}

	@Test
	void getDecisionHistory() {
		// Arrange
		final var errandId = 123L;
		final var decisionId = 456L;
		final var history = new History();
		final var historyList = List.of(history);
		when(historyServiceMock.findDecisionHistoryOnErrand(errandId, decisionId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(historyList);

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/decisions/{decisionId}/history").build(MUNICIPALITY_ID, NAMESPACE, errandId, decisionId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(History.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull().hasSize(1);
		assertThat(response.getFirst()).isEqualTo(history);
		verify(historyServiceMock).findDecisionHistoryOnErrand(errandId, decisionId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(historyServiceMock);
	}

	@Test
	void getErrandHistory() {
		// Arrange
		final var errandId = 123L;
		final var history = new History();
		final var historyList = List.of(history);
		when(historyServiceMock.findErrandHistory(errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(historyList);

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/history").build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(History.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull().hasSize(1);
		assertThat(response.getFirst()).isEqualTo(history);
		verify(historyServiceMock).findErrandHistory(errandId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(historyServiceMock);
	}

	@Test
	void getFacilityHistory() {
		// Arrange
		final var errandId = 123L;
		final var facilityId = 456L;
		final var history = new History();
		final var historyList = List.of(history);
		when(historyServiceMock.findFacilityHistoryOnErrand(errandId, facilityId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(historyList);

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/facilities/{facilityId}/history").build(MUNICIPALITY_ID, NAMESPACE, errandId, facilityId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(History.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull().hasSize(1);
		assertThat(response.getFirst()).isEqualTo(history);
		verify(historyServiceMock).findFacilityHistoryOnErrand(errandId, facilityId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(historyServiceMock);
	}

	@Test
	void getNoteHistory() {
		// Arrange
		final var errandId = 123L;
		final var noteId = 456L;
		final var history = new History();
		final var historyList = List.of(history);
		when(historyServiceMock.findNoteHistoryOnErrand(errandId, noteId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(historyList);

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/notes/{noteId}/history").build(MUNICIPALITY_ID, NAMESPACE, errandId, noteId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(History.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull().hasSize(1);
		assertThat(response.getFirst()).isEqualTo(history);
		verify(historyServiceMock).findNoteHistoryOnErrand(errandId, noteId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(historyServiceMock);
	}

	@Test
	void getStakeholderHistory() {
		// Arrange
		final var errandId = 123L;
		final var stakeholderId = 456L;
		final var history = new History();
		final var historyList = List.of(history);
		when(historyServiceMock.findStakeholderHistoryOnErrand(errandId, stakeholderId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(historyList);

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/stakeholders/{stakeholderId}/history").build(MUNICIPALITY_ID, NAMESPACE, errandId, stakeholderId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(History.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull().hasSize(1);
		assertThat(response.getFirst()).isEqualTo(history);
		verify(historyServiceMock).findStakeholderHistoryOnErrand(errandId, stakeholderId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(historyServiceMock);
	}

}
