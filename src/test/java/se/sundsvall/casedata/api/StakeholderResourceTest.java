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
import static se.sundsvall.casedata.TestUtil.createStakeholderDTO;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.StakeholderDTO;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;
import se.sundsvall.casedata.service.StakeholderService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class StakeholderResourceTest {

	private static final String BASE_URL = "/{municipalityId}/{namespace}/errands/{errandId}/stakeholders";

	@MockBean
	private StakeholderService stakeholderServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getStakeholdersById() {
		final var errandId = 123L;
		final var stakeholderId = 456L;
		final var stakeholderDTO = createStakeholderDTO(StakeholderType.ORGANIZATION, List.of("SomeRole"));

		when(stakeholderServiceMock.findByIdAndMunicipalityIdAndNamespace(stakeholderId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(stakeholderDTO);

		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{stakeholderId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, stakeholderId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(StakeholderDTO.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		verify(stakeholderServiceMock).findByIdAndMunicipalityIdAndNamespace(stakeholderId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(stakeholderServiceMock);
	}

	@Test
	void getStakeholders() {
		final var errandId = 123L;
		final var stakeholderDTO = createStakeholderDTO(StakeholderType.ORGANIZATION, List.of("SomeRole"));

		when(stakeholderServiceMock.findAllStakeholdersByMunicipalityIdAndNamespace(MUNICIPALITY_ID, NAMESPACE)).thenReturn(List.of(stakeholderDTO));

		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL).build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(StakeholderDTO.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).hasSize(1);
		verify(stakeholderServiceMock).findAllStakeholdersByMunicipalityIdAndNamespace(MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(stakeholderServiceMock);
	}

	@Test
	void patchStakeholder() {
		final var errandId = 123L;
		final var stakeholderId = 456L;
		final var stakeholderDTO = createStakeholderDTO(StakeholderType.PERSON, List.of("DRIVER"));

		webTestClient.patch()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{stakeholderId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, stakeholderId))
			.contentType(APPLICATION_JSON)
			.bodyValue(stakeholderDTO)
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		verify(stakeholderServiceMock).patch(stakeholderId, MUNICIPALITY_ID, NAMESPACE, stakeholderDTO);
		verifyNoMoreInteractions(stakeholderServiceMock);
	}

	@Test
	void putStakeholder() {
		final var errandId = 123L;
		final var stakeholderId = 456L;
		final var stakeholderDTO = createStakeholderDTO(StakeholderType.PERSON, List.of("CONTROL_OFFICIAL"));

		webTestClient.put()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{stakeholderId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, stakeholderId))
			.contentType(APPLICATION_JSON)
			.bodyValue(stakeholderDTO)
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		verify(stakeholderServiceMock).put(stakeholderId, MUNICIPALITY_ID, NAMESPACE, stakeholderDTO);
		verifyNoMoreInteractions(stakeholderServiceMock);
	}

	@Test
	void patchErrandWithStakeholder() {
		final var errandId = 123L;
		final var stakeholderId = 456L;
		final var stakeholderDTO = createStakeholderDTO(StakeholderType.PERSON, List.of("OPERATOR"));
		stakeholderDTO.setId(stakeholderId);

		when(stakeholderServiceMock.addStakeholderToErrand(errandId, MUNICIPALITY_ID, NAMESPACE, stakeholderDTO)).thenReturn(stakeholderDTO);

		webTestClient.patch()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL).build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.contentType(APPLICATION_JSON)
			.bodyValue(stakeholderDTO)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL_VALUE)
			.expectHeader().location("/2281/my.namespace/stakeholders/" + stakeholderId);

		verify(stakeholderServiceMock).addStakeholderToErrand(errandId, MUNICIPALITY_ID, NAMESPACE, stakeholderDTO);
	}

	@Test
	void putStakeholdersOnErrand() {
		final var errandId = 123L;
		final var stakeholderDTOList = List.of(createStakeholderDTO(StakeholderType.ORGANIZATION, List.of("DELEGATE", "FELLOW_APPLICANT")));

		webTestClient.put()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL).build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.contentType(APPLICATION_JSON)
			.bodyValue(stakeholderDTOList)
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		verify(stakeholderServiceMock).replaceStakeholdersOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE, stakeholderDTOList);
		verifyNoMoreInteractions(stakeholderServiceMock);
	}

	@Test
	void deleteStakeholder() {
		final var errandId = 123L;
		final var stakeholderId = 456L;

		webTestClient.delete()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{stakeholderId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, stakeholderId))
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		verify(stakeholderServiceMock).deleteStakeholderOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE, stakeholderId);
		verifyNoMoreInteractions(stakeholderServiceMock);
	}

}
