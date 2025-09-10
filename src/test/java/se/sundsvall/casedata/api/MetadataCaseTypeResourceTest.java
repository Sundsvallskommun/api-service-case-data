package se.sundsvall.casedata.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.CaseType;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class MetadataCaseTypeResourceTest {

	private static final String BASE_URL = "/{municipalityId}/{namespace}/metadata/casetypes";

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getCaseTypes() {
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
		assertThat(response).isNotNull().isEmpty();
	}

	@Test
	void getCaseType() {
		// Arrange
		final var type = "PARATRANSIT";

		// Act & Assert
		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{type}").build(MUNICIPALITY_ID, NAMESPACE, type))
			.exchange()
			.expectStatus().isOk()
			.expectBody().isEmpty();
	}

	@Test
	void createCaseType() {
		// Arrange
		final var body = CaseType.builder()
			.withType("PARATRANSIT")
			.withDisplayName("Färdtjänst")
			.build();

		// Act & Assert
		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL).build(MUNICIPALITY_ID, NAMESPACE))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().valueEquals("Location", "/" + MUNICIPALITY_ID + "/metadata/casetypes/" + body.getType())
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
