package se.sundsvall.casedata.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.service.HistoryService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class HistoryResourceTest {

	private static final String BASE_URL = "/{municipalityId}/{namespace}/errands/{errandId}";

	@MockBean
	private HistoryService historyServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getAttachmentHistory() {
		final var errandId = 123L;
		final var attachmentId = 456L;
		final var history = "attachment history";

		when(historyServiceMock.findAttachmentHistory(attachmentId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(history);

		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/attachments/{attachmentId}/history").build(MUNICIPALITY_ID, NAMESPACE, errandId, attachmentId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(String.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isEqualTo(history);
		verify(historyServiceMock).findAttachmentHistory(attachmentId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(historyServiceMock);
	}

	@Test
	void getDecisionHistory() {
		final var errandId = 123L;
		final var decisionId = 456L;
		final var history = "decision history";

		when(historyServiceMock.findDecisionHistory(decisionId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(history);

		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/decisions/{decisionId}/history").build(MUNICIPALITY_ID, NAMESPACE, errandId, decisionId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(String.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isEqualTo(history);
		verify(historyServiceMock).findDecisionHistory(decisionId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(historyServiceMock);
	}

	@Test
	void getErrandHistory() {
		final var errandId = 123L;
		final var history = "errand history";

		when(historyServiceMock.findErrandHistory(errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(history);

		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/history").build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(String.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isEqualTo(history);
		verify(historyServiceMock).findErrandHistory(errandId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(historyServiceMock);
	}

	@Test
	void getFacilityHistory() {
		final var errandId = 123L;
		final var facilityId = 456L;
		final var history = "facility history";

		when(historyServiceMock.findFacilityHistory(facilityId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(history);

		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/facilities/{facilityId}/history").build(MUNICIPALITY_ID, NAMESPACE, errandId, facilityId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(String.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isEqualTo(history);
		verify(historyServiceMock).findFacilityHistory(facilityId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(historyServiceMock);
	}

	@Test
	void getNoteHistory() {
		final var errandId = 123L;
		final var noteId = 456L;
		final var history = "note history";

		when(historyServiceMock.findNoteHistory(noteId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(history);

		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/notes/{noteId}/history").build(MUNICIPALITY_ID, NAMESPACE, errandId, noteId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(String.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isEqualTo(history);
		verify(historyServiceMock).findNoteHistory(noteId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(historyServiceMock);
	}

	@Test
	void getStakeholderHistory() {
		final var errandId = 123L;
		final var stakeholderId = 456L;
		final var history = "stakeholder history";

		when(historyServiceMock.findStakeholderHistory(stakeholderId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(history);

		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/stakeholders/{stakeholderId}/history").build(MUNICIPALITY_ID, NAMESPACE, errandId, stakeholderId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(String.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isEqualTo(history);
		verify(historyServiceMock).findStakeholderHistory(stakeholderId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(historyServiceMock);
	}

}
