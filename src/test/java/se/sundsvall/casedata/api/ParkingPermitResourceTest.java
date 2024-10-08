package se.sundsvall.casedata.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.TestUtil;
import se.sundsvall.casedata.api.model.GetParkingPermit;
import se.sundsvall.casedata.service.ParkingPermitErrandService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class ParkingPermitResourceTest {

	private static final String BASE_URL = "/{municipalityId}/{namespace}/parking-permits";

	@MockBean
	private ParkingPermitErrandService parkingPermitServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getAllParkingPermits() {
		// Arrange
		final var personId = "person123";
		final var parkingPermit = TestUtil.createGetParkingPermitDTO();
		when(parkingPermitServiceMock.findAllByPersonIdAndMunicipalityId(personId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(List.of(parkingPermit));

		// Act
		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL).queryParam("personId", personId).build(MUNICIPALITY_ID, NAMESPACE))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(GetParkingPermit.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).hasSize(1);
		verify(parkingPermitServiceMock).findAllByPersonIdAndMunicipalityId(personId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(parkingPermitServiceMock);
	}

}
