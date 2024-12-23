package se.sundsvall.casedata.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.TestUtil.createErrand;
import static se.sundsvall.casedata.TestUtil.createErrandEntity;
import static se.sundsvall.casedata.TestUtil.createFacilityEntity;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.Decision;
import se.sundsvall.casedata.integration.db.model.ExtraParameterEntity;
import se.sundsvall.casedata.integration.db.model.enums.DecisionType;
import se.sundsvall.casedata.service.ErrandService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class ErrandResourceFailureTest {

	private static final String BASE_URL = "/{municipalityId}/{namespace}/errands";

	@MockitoBean
	private ErrandService errandServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void postErrandWithExtraParameterTooLong() {
		// Arrange
		final var body = createErrandEntity();
		final String longExtraParameter = String.join("", Collections.nCopies(9000, "a")); // This creates a string longer than 8192 characters
		body.setExtraParameters(List.of(ExtraParameterEntity.builder().withKey("longParameter").withValues(List.of(longExtraParameter)).build()));

		// Act
		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL).build(MUNICIPALITY_ID, NAMESPACE))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		verifyNoInteractions(errandServiceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"", " ", "invalid"
	})
	void postErrandWithInvalidFacilityType(final String facilityType) {
		// Arrange
		final var body = createErrandEntity();
		final var facility = createFacilityEntity();
		facility.setFacilityType(facilityType);
		final var facilities = List.of(facility);
		body.setFacilities(facilities);

		// Act
		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL).build(MUNICIPALITY_ID, NAMESPACE))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		verifyNoInteractions(errandServiceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"", " ", "invalid"
	})
	void postFacilityWithInvalidFacilityType(final String facilityType) {
		// Arrange
		final var errandId = 123L;
		final var facility = createFacilityEntity();
		facility.setFacilityType(facilityType);

		// Act
		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/facilities").build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.contentType(APPLICATION_JSON)
			.bodyValue(facility)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		verifyNoInteractions(errandServiceMock);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"", " ", "invalid"
	})
	void putFacilityWithInvalidFacilityType(final String facilityType) {
		// Arrange
		final var errandId = 123L;
		final var facilityId = 456L;
		final var facility = createFacilityEntity();
		facility.setId(facilityId);
		facility.setFacilityType(facilityType);
		final var facilities = List.of(facility);

		// Act
		webTestClient.put()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/facilities").build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.contentType(APPLICATION_JSON)
			.bodyValue(facilities)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		verifyNoInteractions(errandServiceMock);
	}

	@Test
	void postErrandWithDuplicateDecisionTypes() {
		// Arrange
		final var body = createErrand();
		final var decision1 = Decision.builder()
			.withDecisionType(DecisionType.FINAL)
			.build();
		final var decision2 = Decision.builder()
			.withDecisionType(DecisionType.FINAL)
			.build();
		body.setDecisions(List.of(decision1, decision2));

		// Act
		final var result = webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL).build(MUNICIPALITY_ID, NAMESPACE))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		verifyNoInteractions(errandServiceMock);
		assertThat(result).isNotNull();
		assertThat(result.getViolations()).hasSize(1);
		assertThat(result.getViolations().getFirst().getField()).isEqualTo("decisions");
		assertThat(result.getViolations().getFirst().getMessage()).isEqualTo("Errand can contain one decision of each DecisionType");
	}

}
