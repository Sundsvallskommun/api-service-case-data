package se.sundsvall.casedata.api;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;
import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.ExtraParameter;
import se.sundsvall.casedata.service.ErrandExtraParameterService;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.zalando.problem.Status.BAD_REQUEST;
import static org.zalando.problem.Status.NOT_FOUND;

@DirtiesContext

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class ErrandExtraParameterResourceFailureTest {

	private static final String NAMESPACE = "namespace";
	private static final String MUNICIPALITY_ID = "2281";
	private static final Long ERRAND_ID = 1L;
	private static final String PARAMETER_KEY = randomUUID().toString();
	private static final String INVALID = "#invalid#";
	private static final String PATH = "/{municipalityId}/{namespace}/errands/{errandId}/extraparameters";

	@Autowired
	private WebTestClient webTestClient;

	@MockitoBean
	private ErrandExtraParameterService errandParameterServiceMock;

	@Test
	void updateErrandParametersInvalidNamespace() {

		final var requestBody = List.of(ExtraParameter.builder().withKey("key").withValues(List.of("value")).build());

		final var response = webTestClient.patch()
			.uri(builder -> builder.path(PATH).build(Map.of("namespace", INVALID, "municipalityId", MUNICIPALITY_ID, "errandId", ERRAND_ID)))
			.contentType(APPLICATION_JSON)
			.bodyValue(requestBody)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("updateErrandParameters.namespace", "can only contain A-Z, a-z, 0-9, - and _"));

		verifyNoInteractions(errandParameterServiceMock);
	}

	@Test
	void updateErrandParametersInvalidMunicipalityId() {

		final var requestBody = List.of(ExtraParameter.builder().withKey("key").withValues(List.of("value")).build());

		final var response = webTestClient.patch()
			.uri(builder -> builder.path(PATH).build(Map.of("namespace", NAMESPACE, "municipalityId", INVALID, "errandId", ERRAND_ID)))
			.contentType(APPLICATION_JSON)
			.bodyValue(requestBody)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("updateErrandParameters.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(errandParameterServiceMock);
	}

	@Test
	void updateErrandParametersInvalidErrandId() {

		final var requestBody = List.of(ExtraParameter.builder().withKey("key").withValues(List.of("value")).build());

		final var response = webTestClient.patch()
			.uri(builder -> builder.path(PATH).build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID, "errandId", INVALID)))
			.contentType(APPLICATION_JSON)
			.bodyValue(requestBody)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Bad Request");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getDetail()).contains("Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'");
		assertThat(response.getDetail()).contains("For input string: \"invalid#\"");

		verifyNoInteractions(errandParameterServiceMock);

		verifyNoInteractions(errandParameterServiceMock);
	}

	@Test
	void readErrandParameterWithInvalidNamespace() {

		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH.concat("/{parameterKey}")).build(Map.of("namespace", INVALID, "municipalityId", MUNICIPALITY_ID, "errandId", ERRAND_ID, "parameterKey", PARAMETER_KEY)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("readErrandParameter.namespace", "can only contain A-Z, a-z, 0-9, - and _"));

		verifyNoInteractions(errandParameterServiceMock);
	}

	@Test
	void readErrandParameterWithInvalidMunicipalityKey() {

		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH.concat("/{parameterKey}")).build(Map.of("namespace", NAMESPACE, "municipalityId", INVALID, "errandId", ERRAND_ID, "parameterKey", PARAMETER_KEY)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("readErrandParameter.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(errandParameterServiceMock);
	}

	@Test
	void readErrandParameterWithInvalidKey() {

		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH.concat("/{parameterKey}")).build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID, "errandId", INVALID, "parameterKey", PARAMETER_KEY)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Bad Request");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getDetail()).contains("Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'");
		assertThat(response.getDetail()).contains("For input string: \"invalid#\"");

		verifyNoInteractions(errandParameterServiceMock);
	}

	@Test
	void readErrandParameterWithInvalidParameterKey() {

		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH.concat("/{parameterKey}")).build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID, "errandId", ERRAND_ID, "parameterKey", "")))
			.exchange()
			.expectStatus().isNotFound()
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Not Found");
		assertThat(response.getStatus()).isEqualTo(NOT_FOUND);
		assertThat(response.getDetail()).isEqualTo("No endpoint GET /" + MUNICIPALITY_ID + "/" + NAMESPACE + "/errands/" + ERRAND_ID + "/extraparameters/.");

		verifyNoInteractions(errandParameterServiceMock);
	}

	@Test
	void findErrandParametersWithInvalidNamespace() {

		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH).queryParam("limit", "1").build(Map.of("namespace", INVALID, "municipalityId", MUNICIPALITY_ID, "errandId", ERRAND_ID)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("findErrandParameters.namespace", "can only contain A-Z, a-z, 0-9, - and _"));

		verifyNoInteractions(errandParameterServiceMock);
	}

	@Test
	void findErrandParametersWithInvalidMunicipalityId() {

		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH).build(Map.of("namespace", NAMESPACE, "municipalityId", INVALID, "errandId", ERRAND_ID)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("findErrandParameters.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(errandParameterServiceMock);
	}

	@Test
	void updateErrandParameterInvalidNamespace() {

		final var requestBody = List.of("value");

		final var response = webTestClient.patch()
			.uri(builder -> builder.path(PATH.concat("/{parameterKey}")).build(Map.of("namespace", INVALID, "municipalityId", MUNICIPALITY_ID, "errandId", ERRAND_ID, "parameterKey", PARAMETER_KEY)))
			.contentType(APPLICATION_JSON)
			.accept(APPLICATION_JSON)
			.bodyValue(requestBody)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("updateErrandParameter.namespace", "can only contain A-Z, a-z, 0-9, - and _"));

		verifyNoInteractions(errandParameterServiceMock);
	}

	@Test
	void updateErrandParameterInvalidMunicipalityId() {

		final var requestBody = List.of("value");

		final var response = webTestClient.patch()
			.uri(builder -> builder.path(PATH.concat("/{parameterKey}")).build(Map.of("namespace", NAMESPACE, "municipalityId", INVALID, "errandId", ERRAND_ID, "parameterKey", PARAMETER_KEY)))
			.contentType(APPLICATION_JSON)
			.accept(APPLICATION_JSON)
			.bodyValue(requestBody)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("updateErrandParameter.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(errandParameterServiceMock);
	}

	@Test
	void updateErrandParameterInvalidErrandId() {

		final var requestBody = List.of("value");

		final var response = webTestClient.patch()
			.uri(builder -> builder.path(PATH.concat("/{parameterKey}")).build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID, "errandId", INVALID, "parameterKey", PARAMETER_KEY)))
			.contentType(APPLICATION_JSON)
			.accept(APPLICATION_JSON)
			.bodyValue(requestBody)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Bad Request");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getDetail()).contains("Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'");
		assertThat(response.getDetail()).contains("For input string: \"invalid#\"");

		verifyNoInteractions(errandParameterServiceMock);
	}

	@Test
	void updateErrandParameterInvalidParameterKey() {

		final var requestBody = List.of("value");

		final var response = webTestClient.patch()
			.uri(builder -> builder.path(PATH.concat("/{parameterKey}")).build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID, "errandId", ERRAND_ID, "parameterKey", "")))
			.contentType(APPLICATION_JSON)
			.accept(APPLICATION_JSON)
			.bodyValue(requestBody)
			.exchange()
			.expectStatus().isNotFound()
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Not Found");
		assertThat(response.getStatus()).isEqualTo(NOT_FOUND);
		assertThat(response.getDetail()).isEqualTo("No endpoint PATCH /" + MUNICIPALITY_ID + "/" + NAMESPACE + "/errands/" + ERRAND_ID + "/extraparameters/.");

		verifyNoInteractions(errandParameterServiceMock);
	}

	@Test
	void deleteErrandParameterWithInvalidNamespace() {

		final var response = webTestClient.delete()
			.uri(builder -> builder.path(PATH.concat("/{parameterKey}")).build(Map.of("namespace", INVALID, "municipalityId", MUNICIPALITY_ID, "errandId", ERRAND_ID, "parameterKey", PARAMETER_KEY)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("deleteErrandParameter.namespace", "can only contain A-Z, a-z, 0-9, - and _"));

		verifyNoInteractions(errandParameterServiceMock);
	}

	@Test
	void deleteErrandParameterWithInvalidMunicipalityId() {

		final var response = webTestClient.delete()
			.uri(builder -> builder.path(PATH.concat("/{parameterKey}")).build(Map.of("namespace", NAMESPACE, "municipalityId", INVALID, "errandId", ERRAND_ID, "parameterKey", PARAMETER_KEY)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("deleteErrandParameter.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(errandParameterServiceMock);
	}

	@Test
	void deleteErrandParameterWithInvalidErrandId() {

		final var response = webTestClient.delete()
			.uri(builder -> builder.path(PATH.concat("/{parameterKey}")).build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID, "errandId", INVALID, "parameterKey", PARAMETER_KEY)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Bad Request");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getDetail()).contains("Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'");
		assertThat(response.getDetail()).contains("For input string: \"invalid#\"");

		verifyNoInteractions(errandParameterServiceMock);
	}

	@Test
	void deleteErrandParameterWithInvalidParameterKey() {

		final var response = webTestClient.delete()
			.uri(builder -> builder.path(PATH.concat("/{parameterKey}")).build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID, "errandId", ERRAND_ID, "parameterKey", "")))
			.exchange()
			.expectStatus().isNotFound()
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Not Found");
		assertThat(response.getStatus()).isEqualTo(NOT_FOUND);
		assertThat(response.getDetail()).isEqualTo("No endpoint DELETE /" + MUNICIPALITY_ID + "/" + NAMESPACE + "/errands/" + ERRAND_ID + "/extraparameters/.");

		verifyNoInteractions(errandParameterServiceMock);
	}

}
