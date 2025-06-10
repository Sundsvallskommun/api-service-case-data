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
import static se.sundsvall.casedata.TestUtil.createFacility;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.Facility;
import se.sundsvall.casedata.service.FacilityService;
import se.sundsvall.casedata.service.ProcessService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class FacilityResourceTest {

	private static final String BASE_URL = "/{municipalityId}/{namespace}/errands";

	@MockitoBean
	private FacilityService facilityServiceMock;

	@MockitoBean
	private ProcessService processServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getFacilities() {
		// Arrange
		final var errandId = 123L;
		final var facility = createFacility();
		when(facilityServiceMock.findFacilities(errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(List.of(facility));

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/facilities")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", errandId)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(Facility.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).hasSize(1);
		verify(facilityServiceMock).findFacilities(errandId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(facilityServiceMock, processServiceMock);
	}

	@Test
	void getFacilitity() {
		// Arrange
		final var errandId = 123L;
		final var facilityId = 456L;
		final var facility = createFacility();
		when(facilityServiceMock.findFacility(errandId, facilityId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(facility);

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/facilities/{facilityId}")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", errandId, "facilityId", facilityId)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(Facility.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		verify(facilityServiceMock).findFacility(errandId, facilityId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(facilityServiceMock, processServiceMock);
	}

	@Test
	void deleteFacility() {
		// Arrange
		final var errandId = 123L;
		final var facilityId = 456L;

		// Act
		webTestClient.delete()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/facilities/{facilityId}")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", errandId, "facilityId", facilityId)))
			.exchange()
			.expectStatus().isNoContent();

		// Assert
		verify(facilityServiceMock).delete(errandId, MUNICIPALITY_ID, NAMESPACE, facilityId);
		verify(processServiceMock).updateProcess(errandId);
		verifyNoMoreInteractions(facilityServiceMock, processServiceMock);
	}

	@Test
	void postErrandFacility() {
		// Arrange
		final var errandId = 123L;
		final var facility = createFacility();
		facility.setId(456L);
		when(facilityServiceMock.create(errandId, MUNICIPALITY_ID, NAMESPACE, facility)).thenReturn(facility);

		// Act
		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/facilities")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", errandId)))
			.contentType(APPLICATION_JSON)
			.bodyValue(facility)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL_VALUE)
			.expectHeader().location("/2281/MY_NAMESPACE/errands/" + errandId + "/facilities/" + facility.getId());

		// Assert
		verify(facilityServiceMock).create(errandId, MUNICIPALITY_ID, NAMESPACE, facility);
		verify(processServiceMock).updateProcess(errandId);
		verifyNoMoreInteractions(facilityServiceMock, processServiceMock);
	}

	@Test
	void patchErrandFacility() {
		// Arrange
		final var errandId = 123L;
		final var facilityId = 456L;
		final var facility = createFacility();
		when(facilityServiceMock.update(errandId, MUNICIPALITY_ID, NAMESPACE, facilityId, facility)).thenReturn(facility);

		// Act
		webTestClient.patch()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/facilities/{facilityId}")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", errandId, "facilityId", facilityId)))
			.contentType(APPLICATION_JSON)
			.bodyValue(facility)
			.exchange()
			.expectStatus().isNoContent();

		// Assert
		verify(facilityServiceMock).update(errandId, MUNICIPALITY_ID, NAMESPACE, facilityId, facility);
		verify(processServiceMock).updateProcess(errandId);
		verifyNoMoreInteractions(facilityServiceMock, processServiceMock);
	}

	@Test
	void putErrandFacilities() {
		// Arrange
		final var errandId = 123L;
		final var facilityId1 = 456L;
		final var facility1 = createFacility();
		facility1.setId(facilityId1);
		final var facilityId2 = 789L;
		final var facility2 = createFacility();
		facility2.setId(facilityId2);
		final var facilities = List.of(facility1, facility2);

		// Act
		webTestClient.put()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/facilities")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", errandId)))
			.contentType(APPLICATION_JSON)
			.bodyValue(facilities)
			.exchange()
			.expectStatus().isNoContent();

		// Assert
		verify(facilityServiceMock).replaceFacilities(errandId, MUNICIPALITY_ID, NAMESPACE, facilities);
		verify(processServiceMock).updateProcess(errandId);
		verifyNoMoreInteractions(facilityServiceMock, processServiceMock);
	}

}
