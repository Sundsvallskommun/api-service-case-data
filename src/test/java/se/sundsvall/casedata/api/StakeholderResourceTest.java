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
import static se.sundsvall.casedata.TestUtil.createStakeholder;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.Stakeholder;
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
		// Arrange
		final var errandId = 123L;
		final var stakeholderId = 456L;
		final var stakeholder = createStakeholder(StakeholderType.ORGANIZATION, List.of("SomeRole"));

		when(stakeholderServiceMock.findStakeholderOnErrand(errandId, stakeholderId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(stakeholder);

		// Act
		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{stakeholderId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, stakeholderId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(Stakeholder.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		verify(stakeholderServiceMock).findStakeholderOnErrand(errandId, stakeholderId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(stakeholderServiceMock);
	}

	@Test
	void findAllStakeholdersOnErrand() {
		// Arrange
		final var errandId = 123L;
		final var stakeholder = createStakeholder(StakeholderType.ORGANIZATION, List.of("SomeRole"));

		when(stakeholderServiceMock.findAllStakeholdersOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(List.of(stakeholder));

		// Act
		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL).build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(Stakeholder.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).hasSize(1);
		verify(stakeholderServiceMock).findAllStakeholdersOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(stakeholderServiceMock);
	}

	@Test
	void updateStakeholderOnErrand() {
		// Arrange
		final var errandId = 123L;
		final var stakeholderId = 456L;
		final var stakeholder = createStakeholder(StakeholderType.PERSON, List.of("DRIVER"));

		// Act
		webTestClient.patch()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{stakeholderId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, stakeholderId))
			.contentType(APPLICATION_JSON)
			.bodyValue(stakeholder)
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		// Assert
		verify(stakeholderServiceMock).updateStakeholderOnErrand(errandId, stakeholderId, MUNICIPALITY_ID, NAMESPACE, stakeholder);
		verifyNoMoreInteractions(stakeholderServiceMock);
	}

	@Test
	void putStakeholder() {
		// Arrange
		final var errandId = 123L;
		final var stakeholderId = 456L;
		final var stakeholder = createStakeholder(StakeholderType.PERSON, List.of("CONTROL_OFFICIAL"));

		// Act
		webTestClient.put()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{stakeholderId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, stakeholderId))
			.contentType(APPLICATION_JSON)
			.bodyValue(stakeholder)
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		// Assert
		verify(stakeholderServiceMock).replaceStakeholderOnErrand(errandId, stakeholderId, MUNICIPALITY_ID, NAMESPACE, stakeholder);
		verifyNoMoreInteractions(stakeholderServiceMock);
	}

	@Test
	void updateErrandWithStakeholder() {
		// Arrange
		final var errandId = 123L;
		final var stakeholderId = 456L;
		final var stakeholder = createStakeholder(StakeholderType.PERSON, List.of("OPERATOR"));
		stakeholder.setId(stakeholderId);

		when(stakeholderServiceMock.addStakeholderToErrand(errandId, MUNICIPALITY_ID, NAMESPACE, stakeholder)).thenReturn(stakeholder);

		// Act
		webTestClient.patch()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL).build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.contentType(APPLICATION_JSON)
			.bodyValue(stakeholder)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL_VALUE)
			.expectHeader().location("/2281/my.namespace/stakeholders/" + stakeholderId);

		// Assert
		verify(stakeholderServiceMock).addStakeholderToErrand(errandId, MUNICIPALITY_ID, NAMESPACE, stakeholder);
	}

	@Test
	void replaceStakeholdersOnErrand() {
		// Arrange
		final var errandId = 123L;
		final var stakeholderList = List.of(createStakeholder(StakeholderType.ORGANIZATION, List.of("DELEGATE", "FELLOW_APPLICANT")));

		// Act
		webTestClient.put()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL).build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.contentType(APPLICATION_JSON)
			.bodyValue(stakeholderList)
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		// Assert
		verify(stakeholderServiceMock).replaceStakeholdersOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE, stakeholderList);
		verifyNoMoreInteractions(stakeholderServiceMock);
	}

	@Test
	void deleteStakeholder() {
		// Arrange
		final var errandId = 123L;
		final var stakeholderId = 456L;

		// Act
		webTestClient.delete()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{stakeholderId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, stakeholderId))
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		// Assert
		verify(stakeholderServiceMock).deleteStakeholderOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE, stakeholderId);
		verifyNoMoreInteractions(stakeholderServiceMock);
	}

}
