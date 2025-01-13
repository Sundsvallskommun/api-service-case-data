package se.sundsvall.casedata.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.TestUtil.createErrand;
import static se.sundsvall.casedata.TestUtil.createFacility;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.validation.enums.FacilityType;
import se.sundsvall.casedata.service.ErrandService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class ErrandResourceTest {

	private static final String BASE_URL = "/{municipalityId}/{namespace}/errands";

	@MockitoBean
	private ErrandService errandServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void deleteErrand() {
		// Arrange
		final var errandId = 123L;

		// Act
		webTestClient.delete()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", errandId)))
			.exchange()
			.expectStatus().isNoContent();

		// Assert
		verify(errandServiceMock).delete(errandId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(errandServiceMock);
	}

	@ParameterizedTest
	@EnumSource(FacilityType.class)
	void postErrandWithFacilityType(final FacilityType facilityType) {
		// Arrange
		final var body = createErrand();
		body.setId(123L);
		final var facility = createFacility();
		facility.setFacilityType(facilityType.name());
		final var facilities = List.of(facility);
		body.setFacilities(facilities);

		when(errandServiceMock.create(body, MUNICIPALITY_ID, NAMESPACE)).thenReturn(body);

		// Act
		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL).build(MUNICIPALITY_ID, NAMESPACE))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL_VALUE)
			.expectHeader().location("/2281/my.namespace/errands/" + body.getId());

		// Assert
		verify(errandServiceMock).create(body, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(errandServiceMock);
	}

}
