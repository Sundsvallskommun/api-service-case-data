package se.sundsvall.casedata.apptest;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.lang.Long.parseLong;
import static java.text.MessageFormat.format;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.casedata.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casedata.TestUtil.createDecision;
import static se.sundsvall.casedata.TestUtil.createErrand;
import static se.sundsvall.casedata.TestUtil.createExtraParametersList;
import static se.sundsvall.casedata.TestUtil.createFacility;
import static se.sundsvall.casedata.TestUtil.createNote;
import static se.sundsvall.casedata.TestUtil.createStakeholder;
import static se.sundsvall.casedata.TestUtil.createStatus;
import static se.sundsvall.casedata.TestUtil.getRandomOffsetDateTime;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.LOST_PARKING_PERMIT;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARKING_PERMIT;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARKING_PERMIT_RENEWAL;
import static se.sundsvall.casedata.apptest.util.TestConstants.JWT_HEADER_VALUE;
import static se.sundsvall.casedata.apptest.util.TestConstants.PARKING_PERMIT_START_URL;
import static se.sundsvall.casedata.service.util.Constants.AD_USER_HEADER_KEY;
import static se.sundsvall.casedata.service.util.Constants.X_JWT_ASSERTION_HEADER_KEY;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.Decision;
import se.sundsvall.casedata.api.model.Errand;
import se.sundsvall.casedata.api.model.Note;
import se.sundsvall.casedata.api.model.PatchErrand;
import se.sundsvall.casedata.api.model.Stakeholder;
import se.sundsvall.casedata.api.model.validation.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;
import se.sundsvall.casedata.service.util.Constants;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

// TODO: Rewrite this to follow the same pattern as the other tests
@WireMockAppTestSuite(files = "classpath:/ErrandIT", classes = Application.class)
class ErrandIT extends AbstractAppTest {

	private static final String AD_USER_HEADER_VALUE = "user";

	private static final String[] EXCLUDE_FIELDS = {
		"id",
		"version",
		"created",
		"updated",
		"municipalityId",
		"namespace",
		".*\\.id",
		".*\\.version",
		".*\\.created",
		".*\\.updated",
		"processId",
		"errandNumber",
		"createdByClient",
		"updatedByClient",
		"messageIds",
		"createdBy",
		"updatedBy",
		"stakeholders",
		"relatesTo",
		"status.dateTime",
		"statuses",
		"notes",
		"notifications",
		"facilities",
		"decisions",
		"suspension",
		"extraParameters",
		"appeals",
		"note.*\\.createdBy",
		"note.*\\.updatedBy",
		"appeal.*\\.decisionId"
	};
	final String namespace = "SBK_PARKING_PERMIT";

	@Autowired
	private ErrandRepository errandRepository;

	@Autowired
	private WebTestClient webTestClient;

	@BeforeEach
	void beforeEach() {
		errandRepository.deleteAll();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"2281", "2061", "2062"
	})
	void testPostErrand(final String municipalityId) {
		final Errand inputErrand = createErrand();
		inputErrand.setCaseType(PARKING_PERMIT.name());
		final String id = postErrand(inputErrand, municipalityId);
		final Errand getErrand = webTestClient.get().uri(format("{0}/{1}/errands/{2}", municipalityId, namespace, id))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(Errand.class)
			.returnResult()
			.getResponseBody();

		assertThat(getErrand.getProcessId()).isNotNull();
		assertThat(inputErrand)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				EXCLUDE_FIELDS)
			.isEqualTo(getErrand);

		verify(1, postRequestedFor(urlEqualTo(format(PARKING_PERMIT_START_URL, municipalityId, namespace, id))));
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"2281", "2061", "2062"
	})
	void testPostMinimalErrand(final String municipalityId) {
		final Errand inputErrand = Errand.builder()
			.withCaseType(PARKING_PERMIT.name())
			.withLabels(emptyList())
			.build();
		final String id = postErrand(inputErrand, municipalityId);

		final Errand getErrand = webTestClient.get().uri(format("/{0}/{1}/errands/{2}", municipalityId, namespace, id))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(Errand.class)
			.returnResult()
			.getResponseBody();

		assertThat(getErrand.getProcessId()).isNotNull();
		assertThat(inputErrand)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				EXCLUDE_FIELDS)
			.isEqualTo(getErrand);

		assertThat(getErrand.getRelatesTo()).isEmpty();
		assertThat(getErrand.getStakeholders()).isEmpty();
		assertThat(getErrand.getStatuses()).isEmpty();
		assertThat(getErrand.getNotes()).isEmpty();
		assertThat(getErrand.getFacilities()).isEmpty();
		assertThat(getErrand.getDecisions()).isEmpty();
		assertThat(getErrand.getLabels()).isEmpty();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"2281", "2061", "2062"
	})
	void testPatchErrand(final String municipalityId) {
		final Errand inputPostErrand = createErrand();
		final String id = postErrand(inputPostErrand, municipalityId);

		// Get posted object
		final Errand resultPostErrand = webTestClient.get().uri(format("/{0}/{1}/errands/{2}", municipalityId, namespace, id))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(Errand.class)
			.returnResult()
			.getResponseBody();

		// Create updateStakeholderOnErrand object
		final PatchErrand inputPatchErrand = new PatchErrand();
		inputPatchErrand.setDiaryNumber("A new patched diary number");
		inputPatchErrand.setApplicationReceived(getRandomOffsetDateTime());
		inputPatchErrand.setExtraParameters(createExtraParametersList());
		inputPatchErrand.setFacilities(List.of(createFacility()));
		inputPatchErrand.setLabels(List.of("updated-label-1", "updated-label-1"));

		// Patch the object
		webTestClient.patch().uri(format("/{0}/{1}/errands/{2}", municipalityId, namespace, id))
			.bodyValue(inputPatchErrand)
			.exchange()
			.expectStatus().isNoContent();

		// Get patched object
		final Errand resultPatchErrand = webTestClient.get().uri(format("/{0}/{1}/errands/{2}", municipalityId, namespace, id))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(Errand.class)
			.returnResult()
			.getResponseBody();

		// Update fields of the originally posted object, so we can compare with the patched object.
		assertThat(resultPostErrand).isNotNull();
		resultPostErrand.setDiaryNumber(inputPatchErrand.getDiaryNumber());
		resultPostErrand.setApplicationReceived(inputPatchErrand.getApplicationReceived());
		resultPostErrand.setUpdatedByClient(Constants.UNKNOWN);
		resultPostErrand.setUpdatedBy(Constants.UNKNOWN);
		resultPostErrand.getExtraParameters().addAll(inputPatchErrand.getExtraParameters());
		resultPostErrand.setLabels(inputPatchErrand.getLabels());

		assertThat(resultPostErrand)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				EXCLUDE_FIELDS)
			.isEqualTo(resultPatchErrand);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"2281", "2061", "2062"
	})
	void testGetWithOneQueryParam(final String municipalityId) {

		final Errand inputPostErrand = createErrand();
		inputPostErrand.setCaseType(PARKING_PERMIT.name());
		// Create initial errand
		postErrand(inputPostErrand, municipalityId);

		final Page<Errand> resultList = webTestClient.get().uri(
				uriBuilder -> uriBuilder
					.path(format("/{0}/{1}/errands", municipalityId, namespace))
					.queryParam("filter", "externalCaseId:'%s'".formatted(inputPostErrand.getExternalCaseId()))
					.build())
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(new ParameterizedTypeReference<Page<Errand>>() {

			})
			.returnResult()
			.getResponseBody();

		assertEquals(1, requireNonNull(resultList).getTotalElements());
		final Errand result = resultList.getContent().getFirst();

		assertThat(result.getProcessId()).isNotNull();
		assertThat(inputPostErrand)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				EXCLUDE_FIELDS)
			.isEqualTo(result);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"2281", "2061", "2062"
	})
	void testGetWithFilterOnLabels(final String municipalityId) {

		final var label = "the-label";
		final var inputPostErrand = createErrand();
		inputPostErrand.setCaseType(PARKING_PERMIT.name());
		inputPostErrand.getLabels().add(label);
		// Create initial errand
		postErrand(inputPostErrand, municipalityId);

		final Page<Errand> resultList = webTestClient.get().uri(
				uriBuilder -> uriBuilder
					.path(format("/{0}/{1}/errands", municipalityId, namespace))
					.queryParam("filter", "labels~'%s'".formatted(label))
					.build())
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(new ParameterizedTypeReference<Page<Errand>>() {
			})
			.returnResult()
			.getResponseBody();

		assertEquals(1, requireNonNull(resultList).getTotalElements());
		final Errand result = resultList.getContent().getFirst();

		assertThat(result.getLabels()).contains(label);
		assertThat(inputPostErrand)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				EXCLUDE_FIELDS)
			.isEqualTo(result);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"2281", "2061", "2062"
	})
	void testGetWithExtraParameter(final String municipalityId) {
		final Errand inputPostErrand = createErrand();
		inputPostErrand.setCaseType(PARKING_PERMIT_RENEWAL.name());
		final var extraParameters = createExtraParametersList();
		inputPostErrand.setExtraParameters(extraParameters);
		// Create initial errand
		postErrand(inputPostErrand, municipalityId);

		final Page<Errand> resultList = webTestClient.get().uri(
				uriBuilder -> uriBuilder
					.path(format("/{0}/{1}/errands", municipalityId, namespace))
					.queryParam("extraParameters[key 1]", "value 1")
					.queryParam("extraParameters[key 2]", "value 2")
					.build())
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(new ParameterizedTypeReference<Page<Errand>>() {

			})
			.returnResult()
			.getResponseBody();

		assertEquals(1, requireNonNull(resultList).getTotalElements());
		final Errand result = resultList.getContent().getFirst();

		assertThat(result.getProcessId()).isNotNull();
		assertThat(inputPostErrand)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				EXCLUDE_FIELDS)
			.isEqualTo(result);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"2281", "2061", "2062"
	})
	void testGetWithExtraParameterMismatch(final String municipalityId) {
		final Errand inputPostErrand = createErrand();
		final var extraParameters = createExtraParametersList();
		inputPostErrand.setExtraParameters(extraParameters);
		// Create initial errand
		postErrand(inputPostErrand, municipalityId);

		webTestClient.get().uri(
				uriBuilder -> uriBuilder
					.path(format("/{0}/{1}/errands", municipalityId, namespace))
					.queryParam("extraParameters[key 1]", "value 1")
					// One of the extra parameters is wrong
					.queryParam("extraParameters[key 2]", "value 3")
					.build())
			.exchange()
			.expectStatus().isOk();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"2281", "2061", "2062"
	})
	void testGetWithExtraParameterAndFilterMismatch(final String municipalityId) {
		final Errand inputPostErrand = createErrand();
		final var extraParameters = createExtraParametersList();
		inputPostErrand.setExtraParameters(extraParameters);
		// Create initial errand
		postErrand(inputPostErrand, municipalityId);

		webTestClient.get().uri(
				uriBuilder -> uriBuilder
					.path(format("/{0}/{1}/errands", municipalityId, namespace))
					.queryParam("extraParameters[key 1]", "value 1")
					// Filter is wrong
					.queryParam("filter", "externalCaseId:'%s'".formatted(new Random().nextInt(900000000)))
					.build())
			.exchange()
			.expectStatus().isOk();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"2281", "2061", "2062"
	})
	void testGetWithFilterPageableAndExtraParameters(final String municipalityId) {
		final Errand inputPostErrand = createErrand();
		inputPostErrand.setCaseType(LOST_PARKING_PERMIT.name());
		final var extraParameters = createExtraParametersList();
		inputPostErrand.setExtraParameters(extraParameters);
		// Create initial errand
		postErrand(inputPostErrand, municipalityId);

		final Page<Errand> resultList = webTestClient.get().uri(
				uriBuilder -> uriBuilder
					.path(format("/{0}/{1}/errands", municipalityId, namespace))
					.queryParam("filter", "externalCaseId:'%s'".formatted(inputPostErrand.getExternalCaseId()))
					.queryParam("extraParameters[key 1]", "value 1")
					.queryParam("page", "0")
					.queryParam("size", "10")
					.queryParam("sort", "id,desc")
					.build())
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(new ParameterizedTypeReference<Page<Errand>>() {

			})
			.returnResult()
			.getResponseBody();

		assertEquals(1, requireNonNull(resultList).getTotalElements());
		final Errand result = resultList.getContent().getFirst();

		assertThat(result.getProcessId()).isNotNull();
		assertThat(inputPostErrand)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				EXCLUDE_FIELDS)
			.isEqualTo(result);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"2281", "2061", "2062"
	})
	void testGetWithQueryParamTextContains(final String municipalityId) {

		final String NAME_PREFIX = "abc";
		final String WORD_IN_THE_MIDDLE = "word-in-the-middle";

		final Errand inputPostErrand = createErrand();
		inputPostErrand.getStakeholders().getFirst().setFirstName(NAME_PREFIX + WORD_IN_THE_MIDDLE + new Random().nextInt(900000000));
		postErrand(inputPostErrand, municipalityId);

		final Errand anotherErrandWithSameFirstName = createErrand();
		anotherErrandWithSameFirstName.getStakeholders().getFirst().setFirstName(NAME_PREFIX + WORD_IN_THE_MIDDLE + new Random().nextInt(900000000));
		postErrand(anotherErrandWithSameFirstName, municipalityId);

		final Page<Errand> resultPage = webTestClient.get().uri(
				uriBuilder -> uriBuilder
					.path(format("/{0}/{1}/errands", municipalityId, namespace))
					.queryParam("filter", "stakeholders.firstName ~ '*%s*'".formatted(WORD_IN_THE_MIDDLE))
					.build())
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(new ParameterizedTypeReference<Page<Errand>>() {

			})
			.returnResult()
			.getResponseBody();

		assertEquals(2, requireNonNull(resultPage).getTotalElements());
		final var resultList = resultPage.getContent();

		assertThat(inputPostErrand)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				EXCLUDE_FIELDS)
			.isEqualTo(resultList.stream().min(Comparator.comparing(Errand::getCreated)).orElseThrow());

		assertThat(anotherErrandWithSameFirstName)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				EXCLUDE_FIELDS)
			.isEqualTo(resultList.stream().max(Comparator.comparing(Errand::getCreated)).orElseThrow());
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"2281", "2061", "2062"
	})
	void testGetWithOneQueryParam404(final String municipalityId) {
		webTestClient.get().uri(
				uriBuilder -> uriBuilder
					.path(format("/{0}/{1}/errands", municipalityId, namespace))
					.queryParam("filter", "externalCaseId:'%s'".formatted(UUID.randomUUID()))
					.build())
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"2281", "2061", "2062"
	})
	void testGetWithMultipleQueryParams(final String municipalityId) {
		final Errand inputPostErrand1 = createErrand();
		inputPostErrand1.setCaseType(LOST_PARKING_PERMIT.name());
		final String id = postErrand(inputPostErrand1, municipalityId);

		final Errand resultPostErrand1 = webTestClient.get().uri(format("/{0}/{1}/errands/{2}", municipalityId, namespace, id))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(Errand.class).returnResult().getResponseBody();

		// Get only the first one with query params
		final Page<Errand> resultList = webTestClient.get().uri(
				uriBuilder -> UriComponentsBuilder.fromUri(uriBuilder.build())
					.path(format("/{0}/{1}/errands", municipalityId, namespace))
					.queryParam("filter", "externalCaseId:'%s'".formatted(inputPostErrand1.getExternalCaseId()) +
						"and " +
						"caseType:'%s'".formatted(inputPostErrand1.getCaseType()) +
						"and " +
						"priority:'%s'".formatted(inputPostErrand1.getPriority()) +
						"and " +
						"description:'%s'".formatted(inputPostErrand1.getDescription()) +
						"and " +
						"caseTitleAddition:'%s'".formatted(inputPostErrand1.getCaseTitleAddition()) +
						"and " +
						"applicationReceived:'%s'".formatted("{applicationReceived}") +
						"and " +
						"created:'%s'".formatted("{created}"))
					.encode()
					.buildAndExpand(inputPostErrand1.getApplicationReceived(), requireNonNull(resultPostErrand1).getCreated())
					.toUri())
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(new ParameterizedTypeReference<Page<Errand>>() {

			})
			.returnResult()
			.getResponseBody();

		assertEquals(1, requireNonNull(resultList).getTotalElements());
		final Errand result = resultList.getContent().getFirst();

		assertThat(result.getProcessId()).isNotNull();
		assertThat(inputPostErrand1)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				EXCLUDE_FIELDS)
			.isEqualTo(result);
	}

	/**
	 * One of the fields is wrong, but an errand should be found anyway.
	 */
	@ParameterizedTest
	@ValueSource(strings = {
		"2281", "2061", "2062"
	})
	void testGetWithMultipleQueryParams2(final String municipalityId) {

		final Errand inputPostErrand1 = createErrand();
		inputPostErrand1.setCaseType(PARKING_PERMIT_RENEWAL.name());
		final String id = postErrand(inputPostErrand1, municipalityId);

		final Errand resultPostErrand1 = webTestClient.get().uri(format("/{0}/{1}/errands/{2}", municipalityId, namespace, id))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(Errand.class).returnResult().getResponseBody();

		final Page<Errand> resultList = webTestClient.get().uri(
				uriBuilder -> UriComponentsBuilder.fromUri(uriBuilder.build())
					.path(format("/{0}/{1}/errands", municipalityId, namespace))
					.queryParam("filter", // Random UUID = no match, but uses operator "or" and should find an errand anyway.
						"externalCaseId:'%s'".formatted(UUID.randomUUID()) +
							"or " +
							"caseType:'%s'".formatted(inputPostErrand1.getCaseType()) +
							"and " +
							"priority:'%s'".formatted(inputPostErrand1.getPriority()) +
							"and " +
							"description:'%s'".formatted(inputPostErrand1.getDescription()) +
							"and " +
							"caseTitleAddition:'%s'".formatted(inputPostErrand1.getCaseTitleAddition()) +
							"and " +
							"applicationReceived:'%s'".formatted("{applicationReceived}") +
							"and " +
							"created:'%s'".formatted("{created}"))
					.encode()
					.buildAndExpand(inputPostErrand1.getApplicationReceived(), requireNonNull(resultPostErrand1).getCreated())
					.toUri())
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(new ParameterizedTypeReference<Page<Errand>>() {

			})
			.returnResult()
			.getResponseBody();

		assertEquals(1, requireNonNull(resultList).getTotalElements());
		final Errand result = resultList.getContent().getFirst();

		assertThat(result.getProcessId()).isNotNull();
		assertThat(inputPostErrand1)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				EXCLUDE_FIELDS)
			.isEqualTo(result);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"2281", "2061", "2062"
	})
	void testGetWithStakeholderQueryParams(final String municipalityId) {
		final Errand inputPostErrand1 = createErrand();
		postErrand(inputPostErrand1, municipalityId);

		final Stakeholder person = inputPostErrand1.getStakeholders().stream().filter(stakeholder -> StakeholderType.PERSON.equals(stakeholder.getType())).findFirst()
			.orElseThrow();

		// Get only the first one with query params
		final Page<Errand> resultList = webTestClient.get().uri(
				uriBuilder -> UriComponentsBuilder.fromUri(uriBuilder.build())
					.path(format("/{0}/{1}/errands", municipalityId, namespace))
					.queryParam("filter", "stakeholders.firstName:'%s'".formatted(person.getFirstName()) +
						"and " +
						"stakeholders.lastName:'%s'".formatted(person.getLastName()) +
						"and " +
						"stakeholders.personId:'%s'".formatted(person.getPersonId()))
					.encode()
					.build()
					.toUri())
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(new ParameterizedTypeReference<Page<Errand>>() {

			})
			.returnResult()
			.getResponseBody();

		assertEquals(1, requireNonNull(resultList).getTotalElements());
		final Errand result = resultList.getContent().getFirst();

		assertThat(inputPostErrand1)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				EXCLUDE_FIELDS)
			.isEqualTo(result);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"2281", "2061", "2062"
	})
	void testGetWithStakeholderAddressQueryParams(final String municipalityId) {
		final Errand inputPostErrand1 = createErrand();
		postErrand(inputPostErrand1, municipalityId);

		final Stakeholder person = inputPostErrand1.getStakeholders().stream().filter(stakeholder -> StakeholderType.PERSON.equals(stakeholder.getType())).findFirst()
			.orElseThrow();

		// Get only the first one with query params
		final Page<Errand> resultList = webTestClient.get().uri(
				uriBuilder -> UriComponentsBuilder.fromUri(uriBuilder.build())
					.path(format("/{0}/{1}/errands", municipalityId, namespace))
					.queryParam("filter", "stakeholders.addresses.street:'%s'".formatted(person.getAddresses().getFirst().getStreet()) +
						"and " +
						"stakeholders.addresses.houseNumber:'%s'".formatted(person.getAddresses().getFirst().getHouseNumber()))
					.encode()
					.build()
					.toUri())
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(new ParameterizedTypeReference<Page<Errand>>() {

			})
			.returnResult()
			.getResponseBody();

		assertEquals(1, requireNonNull(resultList).getTotalElements());
		final Errand result = resultList.getContent().getFirst();

		assertThat(inputPostErrand1)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				EXCLUDE_FIELDS)
			.isEqualTo(result);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"2281", "2061", "2062"
	})
	void testGetWithStakeholderQueryParams404(final String municipalityId) {
		final Errand inputPostErrand1 = createErrand();
		postErrand(inputPostErrand1, municipalityId);

		final Stakeholder person = inputPostErrand1.getStakeholders().stream().filter(stakeholder -> StakeholderType.PERSON.equals(stakeholder.getType())).findFirst()
			.orElseThrow();

		webTestClient.get().uri(
				uriBuilder -> UriComponentsBuilder.fromUri(uriBuilder.build())
					.path(format("/{0}/{1}/errands", municipalityId, namespace))
					.queryParam("filter", "stakeholders.firstName:'%s'".formatted(person.getFirstName()) +
						"and " +
						"stakeholders.lastName:'%s'".formatted(person.getLastName()) +
						"and " +
						"stakeholders.personId:'%s'".formatted(UUID.randomUUID()))
					.encode()
					.build()
					.toUri())
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"2281", "2061", "2062"
	})
	void testGetErrandsWithPersonId(final String municipalityId) {
		final Errand inputPostErrand1 = createErrand();
		postErrand(inputPostErrand1, municipalityId);

		final Stakeholder person = inputPostErrand1.getStakeholders().stream().filter(stakeholder -> StakeholderType.PERSON.equals(stakeholder.getType())).findFirst()
			.orElseThrow();
		// Get only the first one with query params
		final Page<Errand> resultList = webTestClient.get().uri(
				uriBuilder -> UriComponentsBuilder.fromUri(uriBuilder.build())
					.path(format("/{0}/{1}/errands", municipalityId, namespace))
					.queryParam("filter", "stakeholders.personId:'%s'".formatted(person.getPersonId()))
					.encode()
					.build()
					.toUri())
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(new ParameterizedTypeReference<Page<Errand>>() {

			})
			.returnResult()
			.getResponseBody();

		assertEquals(1, requireNonNull(resultList).getTotalElements());
		final Errand result = resultList.getContent().getFirst();

		assertThat(inputPostErrand1)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				EXCLUDE_FIELDS)
			.isEqualTo(result);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"2281", "2061", "2062"
	})
	void testGetErrandsWithOrganizationNumber(final String municipalityId) {
		final var errand = createErrand();
		postErrand(errand, municipalityId);

		final Stakeholder organization = errand.getStakeholders().stream()
			.filter(stakeholder -> stakeholder.getType() == StakeholderType.ORGANIZATION)
			.findFirst().orElseThrow();
		// Get only the first one with query params
		final Page<Errand> resultList = webTestClient.get().uri(
				uriBuilder -> UriComponentsBuilder.fromUri(uriBuilder.build())
					.path(format("/{0}/{1}/errands", municipalityId, namespace))
					.queryParam("filter", "stakeholders.organizationNumber:'%s'".formatted(organization.getOrganizationNumber()))
					.encode()
					.build()
					.toUri())
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(new ParameterizedTypeReference<Page<Errand>>() {

			})
			.returnResult()
			.getResponseBody();

		assertEquals(1, requireNonNull(resultList).getTotalElements());
		final Errand result = resultList.getContent().getFirst();

		assertThat(errand)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				EXCLUDE_FIELDS)
			.isEqualTo(result);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"2281", "2061", "2062"
	})
	void testPatchErrandWithNote(final String municipalityId) throws JsonProcessingException {
		final var errand = createErrand();
		final var errandId = postErrand(errand, municipalityId);

		final Note note = createNote();

		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(format("/{0}/{1}/errands/{2}/notes", municipalityId, namespace, errandId))
			.withRequest(OBJECT_MAPPER.writeValueAsString(note))
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of(format("/{0}/{1}/notes/(.*)$", municipalityId, namespace)))
			.sendRequestAndVerifyResponse();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"2281", "2061", "2062"
	})
	void testPatchErrandWithStakeholders(final String municipalityId) throws JsonProcessingException {
		final var errand = createErrand();
		final var errandId = postErrand(errand, municipalityId);

		final var stakeholder = createStakeholder(StakeholderType.ORGANIZATION, List.of(StakeholderRole.DRIVER.name()));

		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(format("/{0}/{1}/errands/{2}/stakeholders", municipalityId, namespace, errandId))
			.withRequest(OBJECT_MAPPER.writeValueAsString(stakeholder))
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of(format("/{0}/{1}/stakeholders/(.*)$", municipalityId, namespace)))
			.sendRequestAndVerifyResponse();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"2281", "2061", "2062"
	})
	void testPatchErrandWithDecision(final String municipalityId) throws JsonProcessingException {
		final var errand = createErrand();
		final var errandId = postErrand(errand, municipalityId);

		final Decision decision = createDecision();

		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(format(format("/{0}/{1}/errands/{2}/decisions", municipalityId, namespace, errandId)))
			.withRequest(OBJECT_MAPPER.writeValueAsString(decision))
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of(format("/{0}/{1}/decisions/(.*)$", municipalityId, namespace)))
			.sendRequestAndVerifyResponse();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"2281", "2061", "2062"
	})
	void testPatchErrandWithStatus(final String municipalityId) {
		final var errand = createErrand();
		final var errandId = postErrand(errand, municipalityId);

		final var status = createStatus();

		webTestClient.patch().uri(format("/{0}/{1}/errands/{2}/statuses", municipalityId, namespace, errandId))
			.bodyValue(status)
			.exchange()
			.expectStatus().isNoContent();

		final Errand result = webTestClient.get().uri(format("/{0}/{1}/errands/{2}", municipalityId, namespace, errandId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(Errand.class)
			.returnResult()
			.getResponseBody();

		assertThat(result).isNotNull().isNotEqualTo(errand);
		assertThat(result.getStatuses()).contains(status);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"2281", "2061", "2062"
	})
	void testPatchErrandWithExtraParameters(final String municipalityId) throws JsonProcessingException {
		final var errand = createErrand();
		final var errandId = postErrand(errand, municipalityId);

		final var extraParameters = createExtraParametersList();

		final var patchErrand = PatchErrand.builder().withExtraParameters(extraParameters).withExternalCaseId("externalCaseId").withPhase("phase").build();

		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(format("/{0}/{1}/errands/{2}", municipalityId, namespace, errandId))
			.withRequest(OBJECT_MAPPER.writeValueAsString(patchErrand))
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();

		final var patchedErrand = errandRepository.findById(parseLong(errandId));
		assertThat(patchedErrand).isPresent();
		assertThat(patchedErrand.get().getUpdatedByClient()).isEqualTo(Constants.UNKNOWN);
		assertThat(patchedErrand.get().getUpdatedBy()).isEqualTo(Constants.UNKNOWN);
		assertThat(patchedErrand.get().getExternalCaseId()).isEqualTo("externalCaseId");
		assertThat(patchedErrand.get().getPhase()).isEqualTo("phase");
		assertThat(patchedErrand.get().getUpdated()).isCloseTo(OffsetDateTime.now(), Assertions.within(2, SECONDS));
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"2281", "2061", "2062"
	})
	void testDelete(final String municipalityId) {

		final Errand inputErrand = createErrand();
		final var id = postErrand(inputErrand, municipalityId);

		webTestClient.delete().uri(format("/{0}/{1}/errands/{2}", municipalityId, namespace, id))
			.exchange()
			.expectStatus().isNoContent();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"2281", "2061", "2062"
	})
	void testDeleteWhenNotFound(final String municipalityId) {

		final var nonExistingId = 666L;

		webTestClient.delete().uri(format("/{0}/{1}/errands/{2}", municipalityId, namespace, nonExistingId))
			.exchange()
			.expectStatus().isNotFound()
			.expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
	}

	private String postErrand(final Errand errand, final String municipalityId) {
		final var location = webTestClient.post().uri(format("/{0}/{1}/errands", municipalityId, namespace))
			.bodyValue(errand)
			.header(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.header(AD_USER_HEADER_KEY, AD_USER_HEADER_VALUE)
			.exchange()
			.expectStatus().isCreated()
			.returnResult(Object.class)
			.getResponseHeaders()
			.getLocation();

		assertThat(location).isNotNull();
		return location.toString().substring(location.toString().lastIndexOf("/") + 1);
	}
}
