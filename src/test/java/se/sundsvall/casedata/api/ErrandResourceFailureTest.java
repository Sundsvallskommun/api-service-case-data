package se.sundsvall.casedata.api;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.sundsvall.casedata.TestUtil.createErrandDTO;
import static se.sundsvall.casedata.TestUtil.createFacilityDTO;

import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.violations.ConstraintViolationProblem;

import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.service.ErrandService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class ErrandResourceFailureTest {

	@MockBean
	private ErrandService errandServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@ParameterizedTest
	@ValueSource(strings = {"", " ", "invalid"})
	void postErrandWithInvalidFacilityType(final String facilityType) {
		final var body = createErrandDTO();
		final var facility = createFacilityDTO();
		facility.setFacilityType(facilityType);
		final var facilities = List.of(facility);
		body.setFacilities(facilities);


		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path("/errands").build())
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		verifyNoInteractions(errandServiceMock);
	}

}
