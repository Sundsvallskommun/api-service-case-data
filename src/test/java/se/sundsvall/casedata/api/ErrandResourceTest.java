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
import se.sundsvall.casedata.api.model.FacilityDTO;
import se.sundsvall.casedata.api.model.validation.enums.FacilityType;
import se.sundsvall.casedata.service.ErrandService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class ErrandResourceTest {

	private static final String BASE_URL = "/{municipalityId}/{namespace}/errands";

	@MockBean
	private ErrandService errandServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void deleteErrand() {
		final var errandId = 123L;

		webTestClient.delete()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", errandId)))
			.exchange()
			.expectStatus().isNoContent();

		verify(errandServiceMock).deleteByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(errandServiceMock);
	}

	@Test
	void deleteDecision() {
		final var errandId = 123L;
		final var decisionId = 456L;

		webTestClient.delete()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/decisions/{decisionId}")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", errandId, "decisionId", decisionId)))
			.exchange()
			.expectStatus().isNoContent();

		verify(errandServiceMock).deleteDecisionOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE, decisionId);
		verifyNoMoreInteractions(errandServiceMock);
	}

	@Test
	void deleteNote() {
		final var errandId = 123L;
		final var noteId = 456L;

		webTestClient.delete()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/notes/{noteId}")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", errandId, "noteId", noteId)))
			.exchange()
			.expectStatus().isNoContent();

		verify(errandServiceMock).deleteNoteOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE, noteId);
		verifyNoMoreInteractions(errandServiceMock);
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

		verify(errandServiceMock).deleteFacilityOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE, facilityId);
		verifyNoMoreInteractions(errandServiceMock);
	}

	@Test
	void getDecision() {
		final var errandId = 123L;
		final var decisionDto = createDecisionDTO();

		when(errandServiceMock.findDecisionsOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(List.of(decisionDto));

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/decisions")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", errandId)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(DecisionDTO.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).hasSize(1);
		verify(errandServiceMock).findDecisionsOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(errandServiceMock);
	}

	@Test
	void getFacilities() {
		final var errandId = 123L;
		final var facilityDto = createFacilityDTO();

		when(errandServiceMock.findFacilitiesOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(List.of(facilityDto));

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/facilities")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", errandId)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(FacilityDTO.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).hasSize(1);
		verify(errandServiceMock).findFacilitiesOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(errandServiceMock);
	}

	@Test
	void getFacilitity() {
		final var errandId = 123L;
		final var facilityId = 456L;
		final var facilityDto = createFacilityDTO();

		when(errandServiceMock.findFacilityOnErrand(errandId, facilityId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(facilityDto);

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/facilities/{facilityId}")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", errandId, "facilityId", facilityId)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(FacilityDTO.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		verify(errandServiceMock).findFacilityOnErrand(errandId, facilityId, MUNICIPALITY_ID, NAMESPACE);
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

		when(errandServiceMock.createErrand(body, MUNICIPALITY_ID, NAMESPACE)).thenReturn(body);

		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL).build(MUNICIPALITY_ID, NAMESPACE))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL_VALUE)
			.expectHeader().location("/2281/my.namespace/errands/" + body.getId());

		verify(errandServiceMock).createErrand(body, MUNICIPALITY_ID, NAMESPACE);
	}

	@Test
	void postErrandFacility() {
		final var errandId = 123L;
		final var facilityDTO = createFacilityDTO();
		facilityDTO.setId(456L);

		when(errandServiceMock.createFacility(errandId, MUNICIPALITY_ID, NAMESPACE, facilityDTO)).thenReturn(facilityDTO);

		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/facilities")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", errandId)))
			.contentType(APPLICATION_JSON)
			.bodyValue(facilityDTO)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL_VALUE)
			.expectHeader().location("/2281/my.namespace/errands/" + errandId + "/facilities/" + facilityDTO.getId());

		verify(errandServiceMock).createFacility(errandId, MUNICIPALITY_ID, NAMESPACE, facilityDTO);
	}

	@Test
	void patchErrandFacility() {
		final var errandId = 123L;
		final var facilityId = 456L;
		final var facilityDTO = createFacilityDTO();

		when(errandServiceMock.updateFacilityOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE, facilityId, facilityDTO)).thenReturn(facilityDTO);

		webTestClient.patch()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/facilities/{facilityId}")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", errandId, "facilityId", facilityId)))
			.contentType(APPLICATION_JSON)
			.bodyValue(facilityDTO)
			.exchange()
			.expectStatus().isNoContent();

		verify(errandServiceMock).updateFacilityOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE, facilityId, facilityDTO);
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

		verify(errandServiceMock).replaceFacilitiesOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE, facilityDTOs);
	}

	@Test
	void patchErrandWithAppeal() {
		final var errandId = 123L;
		final var appealId = 456L;
		final var body = createAppealDTO();
		body.setId(appealId);

		when(errandServiceMock.addAppealToErrand(errandId, MUNICIPALITY_ID, NAMESPACE, body)).thenReturn(body);

		webTestClient.patch()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/appeals").build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL_VALUE)
			.expectHeader().location("/2281/my.namespace/appeals/" + appealId);

		verify(errandServiceMock).addAppealToErrand(errandId, MUNICIPALITY_ID, NAMESPACE, body);
	}

}
