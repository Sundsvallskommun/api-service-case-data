package se.sundsvall.casedata.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.TestUtil.createStatus;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.service.StatusService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class StatusResourceTest {

	private static final String BASE_URL = "/{municipalityId}/{namespace}/errands/{errandId}/statuses";

	@MockitoBean
	private StatusService statusServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void patchErrandWithStatus() {
		// Arrange
		final var errandId = 123L;
		final var status = createStatus();

		// Act
		webTestClient.patch()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL).build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.contentType(APPLICATION_JSON)
			.bodyValue(status)
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		// Assert
		verify(statusServiceMock).addToErrand(errandId, MUNICIPALITY_ID, NAMESPACE, status);
		verifyNoMoreInteractions(statusServiceMock);
	}

	@Test
	void replaceStatusOnErrand() {
		// Arrange
		final var errandId = 123L;
		final var statusList = List.of(createStatus());

		// Act
		webTestClient.put()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL).build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.contentType(APPLICATION_JSON)
			.bodyValue(statusList)
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		// Assert
		verify(statusServiceMock).replaceOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE, statusList);
		verifyNoMoreInteractions(statusServiceMock);
	}

}
