package se.sundsvall.casedata.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.CaseType;
import se.sundsvall.casedata.service.MetadataService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class MetadataCaseTypeResourceTest {

	private static final String BASE_URL = "/{municipalityId}/{namespace}/metadata/casetypes";

	@MockitoBean
	private MetadataService metadataServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getCaseTypes() {
		// Arrange
		final var caseTypes = List.of(CaseType.builder().build());

		when(metadataServiceMock.getCaseTypes(MUNICIPALITY_ID, NAMESPACE))
			.thenReturn(caseTypes);

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL).build(MUNICIPALITY_ID, NAMESPACE))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(CaseType.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull().hasSize(1);
	}

	@Test
	void getCaseType() {
		// Arrange
		final var type = "PARATRANSIT";
		final var displayName = "displayName";
		final var caseType = CaseType.builder()
			.withDisplayName(displayName)
			.withType(type)
			.build();

		when(metadataServiceMock.getCaseType(MUNICIPALITY_ID, NAMESPACE, type))
			.thenReturn(caseType);

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{type}").build(MUNICIPALITY_ID, NAMESPACE, type))
			.exchange()
			.expectStatus().isOk()
			.expectBody(CaseType.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getType()).isEqualTo(type);
		assertThat(response.getDisplayName()).isEqualTo(displayName);
	}

	@Test
	void createCaseType() {
		// Arrange
		final var type = "PARATRANSIT";
		final var body = CaseType.builder()
			.withType(type)
			.withDisplayName("Färdtjänst")
			.build();

		when(metadataServiceMock.createCaseType(MUNICIPALITY_ID, NAMESPACE, body)).thenReturn(type);

		// Act & Assert
		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL).build(MUNICIPALITY_ID, NAMESPACE))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().valueEquals("Location", "/" + MUNICIPALITY_ID + "/" + NAMESPACE + "/metadata/casetypes/" + type)
			.expectHeader().contentType(ALL_VALUE);
	}

	@Test
	void deleteCaseType() {
		// Arrange
		final var type = "PARATRANSIT";

		// Act & Assert
		webTestClient.delete()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{type}").build(MUNICIPALITY_ID, NAMESPACE, type))
			.exchange()
			.expectStatus().isNoContent();
	}
}
