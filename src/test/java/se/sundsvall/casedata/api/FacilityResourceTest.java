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
import static se.sundsvall.casedata.TestUtil.createFacilityDTO;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.FacilityDTO;
import se.sundsvall.casedata.service.FacilityService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class FacilityResourceTest {

	private static final String BASE_URL = "/{municipalityId}/{namespace}/errands";

	@MockBean
	private FacilityService facilityServiceMock;

	@Autowired
	private WebTestClient webTestClient;


	@Test
	void getFacilities() {
		final var errandId = 123L;
		final var facilityDto = createFacilityDTO();

		when(facilityServiceMock.findFacilitiesOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(List.of(facilityDto));

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/facilities")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", errandId)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(FacilityDTO.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).hasSize(1);
		verify(facilityServiceMock).findFacilitiesOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(facilityServiceMock);
	}

	@Test
	void getFacilitity() {
		final var errandId = 123L;
		final var facilityId = 456L;
		final var facilityDto = createFacilityDTO();

		when(facilityServiceMock.findFacilityOnErrand(errandId, facilityId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(facilityDto);

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/facilities/{facilityId}")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", errandId, "facilityId", facilityId)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(FacilityDTO.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		verify(facilityServiceMock).findFacilityOnErrand(errandId, facilityId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(facilityServiceMock);
	}

	@Test
	void deleteFacility() {
		final var errandId = 123L;
		final var facilityId = 456L;

		webTestClient.delete()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/facilities/{facilityId}")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", errandId, "facilityId", facilityId)))
			.exchange()
			.expectStatus().isNoContent();

		verify(facilityServiceMock).deleteFacilityOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE, facilityId);
		verifyNoMoreInteractions(facilityServiceMock);
	}

	@Test
	void postErrandFacility() {
		final var errandId = 123L;
		final var facilityDTO = createFacilityDTO();
		facilityDTO.setId(456L);

		when(facilityServiceMock.createFacility(errandId, MUNICIPALITY_ID, NAMESPACE, facilityDTO)).thenReturn(facilityDTO);

		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/facilities")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", errandId)))
			.contentType(APPLICATION_JSON)
			.bodyValue(facilityDTO)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL_VALUE)
			.expectHeader().location("/2281/my.namespace/errands/" + errandId + "/facilities/" + facilityDTO.getId());

		verify(facilityServiceMock).createFacility(errandId, MUNICIPALITY_ID, NAMESPACE, facilityDTO);
	}


	@Test
	void patchErrandFacility() {
		final var errandId = 123L;
		final var facilityId = 456L;
		final var facilityDTO = createFacilityDTO();

		when(facilityServiceMock.updateFacilityOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE, facilityId, facilityDTO)).thenReturn(facilityDTO);

		webTestClient.patch()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/facilities/{facilityId}")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", errandId, "facilityId", facilityId)))
			.contentType(APPLICATION_JSON)
			.bodyValue(facilityDTO)
			.exchange()
			.expectStatus().isNoContent();

		verify(facilityServiceMock).updateFacilityOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE, facilityId, facilityDTO);
	}

	@Test
	void putErrandFacilities() {
		final var errandId = 123L;
		final var facilityId1 = 456L;
		final var facilityDTO1 = createFacilityDTO();
		facilityDTO1.setId(facilityId1);
		final var facilityId2 = 789L;
		final var facilityDTO2 = createFacilityDTO();
		facilityDTO2.setId(facilityId2);

		final var facilityDTOs = List.of(facilityDTO1, facilityDTO2);

		webTestClient.put()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/facilities")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", errandId)))
			.contentType(APPLICATION_JSON)
			.bodyValue(facilityDTOs)
			.exchange()
			.expectStatus().isNoContent();

		verify(facilityServiceMock).replaceFacilitiesOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE, facilityDTOs);
	}


}
