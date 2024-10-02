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
import static se.sundsvall.casedata.TestUtil.createAppealDTO;
import static se.sundsvall.casedata.TestUtil.createPatchAppealDTO;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.AppealDTO;
import se.sundsvall.casedata.service.AppealService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class AppealResourceTest {

	private static final String BASE_URL = "/{municipalityId}/{namespace}/errands";


	@MockBean
	private AppealService appealServiceMock;

	@Autowired
	private WebTestClient webTestClient;


	@Test
	void getErrandAppeals() {
		// Arrange
		final var errandId = 123L;
		final var appealDTO = createAppealDTO();
		when(appealServiceMock.findByErrandIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(List.of(appealDTO));

		// Act
		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/appeals").build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(AppealDTO.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).hasSize(1);
		verify(appealServiceMock).findByErrandIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(appealServiceMock);
	}

	@Test
	void getErrandAppeal() {
		// Arrange
		final var errandId = 123L;
		final var appealId = 456L;
		final var appealDTO = createAppealDTO();
		when(appealServiceMock.findByIdAndMunicipalityIdAndNamespace(appealId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(appealDTO);

		// Act
		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/appeals/{appealId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, appealId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(AppealDTO.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		verify(appealServiceMock).findByIdAndMunicipalityIdAndNamespace(appealId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(appealServiceMock);
	}

	@Test
	void patchErrandWithAppeal() {
		// Arrange
		final var errandId = 123L;
		final var appealId = 456L;
		final var body = createAppealDTO();
		body.setId(appealId);

		when(appealServiceMock.addAppealToErrand(errandId, MUNICIPALITY_ID, NAMESPACE, body)).thenReturn(body);

		// Act
		webTestClient.patch()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/appeals").build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL_VALUE)
			.expectHeader().location("/2281/my.namespace/appeals/" + appealId);

		// Assert
		verify(appealServiceMock).addAppealToErrand(errandId, MUNICIPALITY_ID, NAMESPACE, body);
	}

	@Test
	void updateAppealOnErrand() {
		// Arrange
		final var errandId = 123L;
		final var appealId = 456L;
		final var body = createPatchAppealDTO();
		body.setId(appealId);

		// Act
		webTestClient.patch()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/appeals/{appealId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, appealId))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		// Assert
		verify(appealServiceMock).updateAppeal(appealId, MUNICIPALITY_ID, NAMESPACE, body);
	}


	@Test
	void replaceAppealOnErrand() {
		// Arrange
		final var errandId = 123L;
		final var appealId = 456L;
		final var body = createAppealDTO();
		body.setId(appealId);

		// Act
		webTestClient.put()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/appeals/{appealId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, appealId))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		// Assert
		verify(appealServiceMock).replaceAppeal(appealId, MUNICIPALITY_ID, NAMESPACE, body);
	}


	@Test
	void deleteAppealOnErrand() {
		// Arrange
		final var errandId = 123L;
		final var appealId = 456L;

		// Act
		webTestClient.delete()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/appeals/{appealId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, appealId))
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		// Assert
		verify(appealServiceMock).deleteAppealOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE, appealId);

	}

}
