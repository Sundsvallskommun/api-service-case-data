package se.sundsvall.casedata.apptest;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.text.MessageFormat.format;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.casedata.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casedata.TestUtil.createDecisionDTO;
import static se.sundsvall.casedata.TestUtil.createErrandDTO;
import static se.sundsvall.casedata.TestUtil.createExtraParameters;
import static se.sundsvall.casedata.TestUtil.createFacilityDTO;
import static se.sundsvall.casedata.TestUtil.createNoteDTO;
import static se.sundsvall.casedata.TestUtil.createStakeholderDTO;
import static se.sundsvall.casedata.TestUtil.createStatusDTO;
import static se.sundsvall.casedata.TestUtil.getRandomOffsetDateTime;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.LOST_PARKING_PERMIT;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARKING_PERMIT;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARKING_PERMIT_RENEWAL;
import static se.sundsvall.casedata.apptest.util.TestConstants.JWT_HEADER_VALUE;
import static se.sundsvall.casedata.apptest.util.TestConstants.PARKING_PERMIT_START_URL;
import static se.sundsvall.casedata.service.util.Constants.AD_USER_HEADER_KEY;
import static se.sundsvall.casedata.service.util.Constants.X_JWT_ASSERTION_HEADER_KEY;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.RandomStringUtils;
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
import se.sundsvall.casedata.api.model.DecisionDTO;
import se.sundsvall.casedata.api.model.ErrandDTO;
import se.sundsvall.casedata.api.model.NoteDTO;
import se.sundsvall.casedata.api.model.PatchErrandDTO;
import se.sundsvall.casedata.api.model.StakeholderDTO;
import se.sundsvall.casedata.api.model.validation.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;
import se.sundsvall.casedata.service.util.Constants;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/ErrandIT", classes = Application.class)
class ErrandIT extends CustomAbstractAppTest {

	private static final String AD_USER_HEADER_VALUE = "user";

	private static final String[] EXCLUDE_FIELDS = {
		"id",
		"version",
		"created",
		"updated",
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
		"note.*\\.createdBy",
		"note.*\\.updatedBy",
		"appeal.*\\.decisionId"
	};

	@Autowired
	private ErrandRepository errandRepository;

	@Autowired
	private WebTestClient webTestClient;

	@BeforeEach
	void beforeEach() {
		errandRepository.deleteAll();
	}

	@ParameterizedTest
	@ValueSource(strings = {"2281", "2061", "2062"})
	void testPostErrand(final String municipalityId) {
		final ErrandDTO inputErrandDTO = createErrandDTO(municipalityId);
		inputErrandDTO.setCaseType(PARKING_PERMIT.name());
		final String id = postErrand(inputErrandDTO);

		final ErrandDTO getErrandDTO = webTestClient.get().uri(format("{0}/errands/{1}", municipalityId, id))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(ErrandDTO.class)
			.returnResult()
			.getResponseBody();

		assertNotNull(requireNonNull(getErrandDTO).getProcessId());
		assertThat(inputErrandDTO)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				EXCLUDE_FIELDS)
			.isEqualTo(getErrandDTO);

		verify(1, postRequestedFor(urlEqualTo(PARKING_PERMIT_START_URL + id)));
	}

	@ParameterizedTest
	@ValueSource(strings = {"2281", "2061", "2062"})
	void testPostMinimalErrand(final String municipalityId) {
		final ErrandDTO inputErrandDTO = new ErrandDTO();
		inputErrandDTO.setMunicipalityId(municipalityId);
		inputErrandDTO.setCaseType(PARKING_PERMIT.name());
		final String id = postErrand(inputErrandDTO);

		final ErrandDTO getErrandDTO = webTestClient.get().uri(format("/{0}/errands/{1}", municipalityId, id))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(ErrandDTO.class)
			.returnResult()
			.getResponseBody();

		assertNotNull(requireNonNull(getErrandDTO).getProcessId());

		assertThat(inputErrandDTO)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				EXCLUDE_FIELDS)
			.isEqualTo(getErrandDTO);
	}

	@ParameterizedTest
	@ValueSource(strings = {"2281", "2061", "2062"})
	void testPatchErrand(final String municipalityId) {
		final ErrandDTO inputPostErrandDTO = createErrandDTO(municipalityId);
		final String id = postErrand(inputPostErrandDTO);

		// Get posted object
		final ErrandDTO resultPostErrandDTO = webTestClient.get().uri(format("/{0}/errands/{1}", municipalityId, id))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(ErrandDTO.class)
			.returnResult()
			.getResponseBody();

		// Create patch object
		final PatchErrandDTO inputPatchErrandDTO = new PatchErrandDTO();
		inputPatchErrandDTO.setDiaryNumber("A new patched diary number");
		inputPatchErrandDTO.setApplicationReceived(getRandomOffsetDateTime());
		inputPatchErrandDTO.setExtraParameters(createExtraParameters());
		inputPatchErrandDTO.setFacilities(List.of(createFacilityDTO(municipalityId)));

		// Patch the object
		webTestClient.patch().uri(format("/{0}/errands/{1}", municipalityId, id))
			.bodyValue(inputPatchErrandDTO)
			.exchange()
			.expectStatus().isNoContent();

		// Get patched object
		final ErrandDTO resultPatchErrandDTO = webTestClient.get().uri(format("/{0}/errands/{1}", municipalityId, id))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(ErrandDTO.class)
			.returnResult()
			.getResponseBody();

		// Update fields of the originally posted object, so we can compare with the patched object.
		assertNotNull(resultPostErrandDTO);
		resultPostErrandDTO.setDiaryNumber(inputPatchErrandDTO.getDiaryNumber());
		resultPostErrandDTO.setApplicationReceived(inputPatchErrandDTO.getApplicationReceived());
		resultPostErrandDTO.setUpdatedByClient(Constants.UNKNOWN);
		resultPostErrandDTO.setUpdatedBy(Constants.UNKNOWN);
		resultPostErrandDTO.getExtraParameters().putAll(inputPatchErrandDTO.getExtraParameters());

		assertThat(resultPostErrandDTO)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				EXCLUDE_FIELDS)
			.isEqualTo(resultPatchErrandDTO);
	}

	@ParameterizedTest
	@ValueSource(strings = {"2281", "2061", "2062"})
	void testGetWithOneQueryParam(final String municipalityId) {

		final ErrandDTO inputPostErrandDTO = createErrandDTO(municipalityId);
		inputPostErrandDTO.setCaseType(PARKING_PERMIT.name());
		// Create initial errand
		postErrand(inputPostErrandDTO);

		final Page<ErrandDTO> resultList = webTestClient.get().uri(
				uriBuilder -> uriBuilder
					.path(format("/{0}/errands", municipalityId))
					.queryParam("filter", "externalCaseId:'%s'".formatted(inputPostErrandDTO.getExternalCaseId()))
					.build())
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(new ParameterizedTypeReference<Page<ErrandDTO>>() {

			})
			.returnResult()
			.getResponseBody();

		assertEquals(1, requireNonNull(resultList).getTotalElements());
		final ErrandDTO result = resultList.getContent().getFirst();

		assertNotNull(result.getProcessId());

		assertNotNull(result.getProcessId());

		assertThat(inputPostErrandDTO)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				EXCLUDE_FIELDS)
			.isEqualTo(result);
	}

	@ParameterizedTest
	@ValueSource(strings = {"2281", "2061", "2062"})
	void testGetWithExtraParameter(final String municipalityId) {
		final ErrandDTO inputPostErrandDTO = createErrandDTO(municipalityId);
		inputPostErrandDTO.setCaseType(PARKING_PERMIT_RENEWAL.name());
		final Map<String, String> extraParameters = new HashMap<>();
		extraParameters.put("key 1", "value 1");
		extraParameters.put("key 2", "value 2");
		inputPostErrandDTO.setExtraParameters(extraParameters);
		// Create initial errand
		postErrand(inputPostErrandDTO);

		final Page<ErrandDTO> resultList = webTestClient.get().uri(
				uriBuilder -> uriBuilder
					.path(format("/{0}/errands", municipalityId))
					.queryParam("extraParameters[key 1]", "value 1")
					.queryParam("extraParameters[key 2]", "value 2")
					.build())
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(new ParameterizedTypeReference<Page<ErrandDTO>>() {

			})
			.returnResult()
			.getResponseBody();

		assertEquals(1, requireNonNull(resultList).getTotalElements());
		final ErrandDTO result = resultList.getContent().getFirst();

		assertNotNull(result.getProcessId());

		assertThat(inputPostErrandDTO)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				EXCLUDE_FIELDS)
			.isEqualTo(result);
	}

	@ParameterizedTest
	@ValueSource(strings = {"2281", "2061", "2062"})
	void testGetWithExtraParameterMismatch(final String municipalityId) {
		final ErrandDTO inputPostErrandDTO = createErrandDTO(municipalityId);
		final Map<String, String> extraParameters = new HashMap<>();
		extraParameters.put("key 1", "value 1");
		extraParameters.put("key 2", "value 2");
		inputPostErrandDTO.setExtraParameters(extraParameters);
		// Create initial errand
		postErrand(inputPostErrandDTO);

		webTestClient.get().uri(
				uriBuilder -> uriBuilder
					.path(format("/{0}/errands", municipalityId))
					.queryParam("extraParameters[key 1]", "value 1")
					// One of the extra parameters is wrong
					.queryParam("extraParameters[key 2]", "value 3")
					.build())
			.exchange()
			.expectStatus().isNotFound();
	}

	@ParameterizedTest
	@ValueSource(strings = {"2281", "2061", "2062"})
	void testGetWithExtraParameterAndFilterMismatch(final String municipalityId) {
		final ErrandDTO inputPostErrandDTO = createErrandDTO(municipalityId);
		final Map<String, String> extraParameters = new HashMap<>();
		extraParameters.put("key 1", "value 1");
		extraParameters.put("key 2", "value 2");
		inputPostErrandDTO.setExtraParameters(extraParameters);
		// Create initial errand
		postErrand(inputPostErrandDTO);

		webTestClient.get().uri(
				uriBuilder -> uriBuilder
					.path(format("/{0}/errands", municipalityId))
					.queryParam("extraParameters[key 1]", "value 1")
					// Filter is wrong
					.queryParam("filter", "externalCaseId:'%s'".formatted(RandomStringUtils.randomNumeric(10)))
					.build())
			.exchange()
			.expectStatus().isNotFound();
	}

	@ParameterizedTest
	@ValueSource(strings = {"2281", "2061", "2062"})
	void testGetWithFilterPageableAndExtraParameters(final String municipalityId) {
		final ErrandDTO inputPostErrandDTO = createErrandDTO(municipalityId);
		inputPostErrandDTO.setCaseType(LOST_PARKING_PERMIT.name());
		final Map<String, String> extraParameters = new HashMap<>();
		extraParameters.put("key 1", "value 1");
		inputPostErrandDTO.setExtraParameters(extraParameters);
		// Create initial errand
		postErrand(inputPostErrandDTO);

		final Page<ErrandDTO> resultList = webTestClient.get().uri(
				uriBuilder -> uriBuilder
					.path(format("/{0}/errands", municipalityId))
					.queryParam("filter", "externalCaseId:'%s'".formatted(inputPostErrandDTO.getExternalCaseId()))
					.queryParam("extraParameters[key 1]", "value 1")
					.queryParam("page", "0")
					.queryParam("size", "10")
					.queryParam("sort", "id,desc")
					.build())
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(new ParameterizedTypeReference<Page<ErrandDTO>>() {

			})
			.returnResult()
			.getResponseBody();

		assertEquals(1, requireNonNull(resultList).getTotalElements());
		final ErrandDTO result = resultList.getContent().getFirst();

		assertNotNull(result.getProcessId());

		assertThat(inputPostErrandDTO)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				EXCLUDE_FIELDS)
			.isEqualTo(result);
	}

	@ParameterizedTest
	@ValueSource(strings = {"2281", "2061", "2062"})
	void testGetWithQueryParamTextContains(final String municipalityId) {

		final String NAME_PREFIX = "abc";
		final String WORD_IN_THE_MIDDLE = "word-in-the-middle";

		final ErrandDTO inputPostErrandDTO = createErrandDTO(municipalityId);
		inputPostErrandDTO.getStakeholders().getFirst().setFirstName(NAME_PREFIX + WORD_IN_THE_MIDDLE + RandomStringUtils.random(10));
		postErrand(inputPostErrandDTO);

		final ErrandDTO anotherErrandWithSameFirstName = createErrandDTO(municipalityId);
		anotherErrandWithSameFirstName.getStakeholders().getFirst().setFirstName(NAME_PREFIX + WORD_IN_THE_MIDDLE + RandomStringUtils.random(10));
		postErrand(anotherErrandWithSameFirstName);

		final Page<ErrandDTO> resultPage = webTestClient.get().uri(
				uriBuilder -> uriBuilder
					.path(format("/{0}/errands", municipalityId))
					.queryParam("filter", "stakeholders.firstName ~ '*%s*'".formatted(WORD_IN_THE_MIDDLE))
					.build())
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(new ParameterizedTypeReference<Page<ErrandDTO>>() {

			})
			.returnResult()
			.getResponseBody();

		assertEquals(2, requireNonNull(resultPage).getTotalElements());
		final var resultList = resultPage.getContent();

		assertThat(inputPostErrandDTO)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				EXCLUDE_FIELDS)
			.isEqualTo(resultList.stream().min(Comparator.comparing(ErrandDTO::getCreated)).orElseThrow());

		assertThat(anotherErrandWithSameFirstName)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				EXCLUDE_FIELDS)
			.isEqualTo(resultList.stream().max(Comparator.comparing(ErrandDTO::getCreated)).orElseThrow());
	}

	@ParameterizedTest
	@ValueSource(strings = {"2281", "2061", "2062"})
	void testGetWithOneQueryParam404(final String municipalityId) {
		webTestClient.get().uri(
				uriBuilder -> uriBuilder
					.path(format("/{0}/errands", municipalityId))
					.queryParam("filter", "externalCaseId:'%s'".formatted(UUID.randomUUID()))
					.build())
			.exchange()
			.expectStatus().isNotFound()
			.expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
	}

	@ParameterizedTest
	@ValueSource(strings = {"2281", "2061", "2062"})
	void testGetWithMultipleQueryParams(final String municipalityId) {
		final ErrandDTO inputPostErrandDTO_1 = createErrandDTO(municipalityId);
		inputPostErrandDTO_1.setCaseType(LOST_PARKING_PERMIT.name());
		final String id = postErrand(inputPostErrandDTO_1);

		final ErrandDTO resultPostErrandDTO_1 = webTestClient.get().uri(format("/{0}/errands/{1}", municipalityId, id))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(ErrandDTO.class).returnResult().getResponseBody();

		// Get only the first one with query params
		final Page<ErrandDTO> resultList = webTestClient.get().uri(
				uriBuilder -> UriComponentsBuilder.fromUri(uriBuilder.build())
					.path(format("/{0}/errands", municipalityId))
					.queryParam("filter", "externalCaseId:'%s'".formatted(inputPostErrandDTO_1.getExternalCaseId()) +
						"and " +
						"caseType:'%s'".formatted(inputPostErrandDTO_1.getCaseType()) +
						"and " +
						"priority:'%s'".formatted(inputPostErrandDTO_1.getPriority()) +
						"and " +
						"description:'%s'".formatted(inputPostErrandDTO_1.getDescription()) +
						"and " +
						"caseTitleAddition:'%s'".formatted(inputPostErrandDTO_1.getCaseTitleAddition()) +
						"and " +
						"applicationReceived:'%s'".formatted("{applicationReceived}") +
						"and " +
						"created:'%s'".formatted("{created}"))
					.encode()
					.buildAndExpand(inputPostErrandDTO_1.getApplicationReceived(), requireNonNull(resultPostErrandDTO_1).getCreated())
					.toUri())
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(new ParameterizedTypeReference<Page<ErrandDTO>>() {

			})
			.returnResult()
			.getResponseBody();

		assertEquals(1, requireNonNull(resultList).getTotalElements());
		final ErrandDTO result = resultList.getContent().getFirst();

		assertNotNull(result.getProcessId());
		assertThat(inputPostErrandDTO_1)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				EXCLUDE_FIELDS)
			.isEqualTo(result);
	}

	/**
	 * One of the fields is wrong, but an errand should be found anyway.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"2281", "2061", "2062"})
	void testGetWithMultipleQueryParams2(final String municipalityId) {

		final ErrandDTO inputPostErrandDTO_1 = createErrandDTO(municipalityId);
		inputPostErrandDTO_1.setCaseType(PARKING_PERMIT_RENEWAL.name());
		final String id = postErrand(inputPostErrandDTO_1);

		final ErrandDTO resultPostErrandDTO_1 = webTestClient.get().uri(format("/{0}/errands/{1}", municipalityId, id))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(ErrandDTO.class).returnResult().getResponseBody();

		final Page<ErrandDTO> resultList = webTestClient.get().uri(
				uriBuilder -> UriComponentsBuilder.fromUri(uriBuilder.build())
					.path(format("/{0}/errands", municipalityId))
					.queryParam("filter", // Random UUID = no match, but uses operator "or" and should find an errand anyway.
						"externalCaseId:'%s'".formatted(UUID.randomUUID()) +
							"or " +
							"caseType:'%s'".formatted(inputPostErrandDTO_1.getCaseType()) +
							"and " +
							"priority:'%s'".formatted(inputPostErrandDTO_1.getPriority()) +
							"and " +
							"description:'%s'".formatted(inputPostErrandDTO_1.getDescription()) +
							"and " +
							"caseTitleAddition:'%s'".formatted(inputPostErrandDTO_1.getCaseTitleAddition()) +
							"and " +
							"applicationReceived:'%s'".formatted("{applicationReceived}") +
							"and " +
							"created:'%s'".formatted("{created}"))
					.encode()
					.buildAndExpand(inputPostErrandDTO_1.getApplicationReceived(), requireNonNull(resultPostErrandDTO_1).getCreated())
					.toUri())
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(new ParameterizedTypeReference<Page<ErrandDTO>>() {

			})
			.returnResult()
			.getResponseBody();

		assertEquals(1, requireNonNull(resultList).getTotalElements());
		final ErrandDTO result = resultList.getContent().getFirst();

		assertNotNull(result.getProcessId());
		assertThat(inputPostErrandDTO_1)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				EXCLUDE_FIELDS)
			.isEqualTo(result);
	}

	@ParameterizedTest
	@ValueSource(strings = {"2281", "2061", "2062"})
	void testGetWithStakeholderQueryParams(final String municipalityId) {
		final ErrandDTO inputPostErrandDTO_1 = createErrandDTO(municipalityId);
		postErrand(inputPostErrandDTO_1);

		final StakeholderDTO person = inputPostErrandDTO_1.getStakeholders().stream().filter(stakeholderDTO -> StakeholderType.PERSON.equals(stakeholderDTO.getType())).findFirst().orElseThrow();

		// Get only the first one with query params
		final Page<ErrandDTO> resultList = webTestClient.get().uri(
				uriBuilder -> UriComponentsBuilder.fromUri(uriBuilder.build())
					.path(format("/{0}/errands", municipalityId))
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
			.expectBody(new ParameterizedTypeReference<Page<ErrandDTO>>() {

			})
			.returnResult()
			.getResponseBody();

		assertEquals(1, requireNonNull(resultList).getTotalElements());
		final ErrandDTO result = resultList.getContent().getFirst();

		assertThat(inputPostErrandDTO_1)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				EXCLUDE_FIELDS)
			.isEqualTo(result);
	}

	@ParameterizedTest
	@ValueSource(strings = {"2281", "2061", "2062"})
	void testGetWithStakeholderAddressQueryParams(final String municipalityId) {
		final ErrandDTO inputPostErrandDTO_1 = createErrandDTO(municipalityId);
		postErrand(inputPostErrandDTO_1);

		final StakeholderDTO person = inputPostErrandDTO_1.getStakeholders().stream().filter(stakeholderDTO -> StakeholderType.PERSON.equals(stakeholderDTO.getType())).findFirst().orElseThrow();

		// Get only the first one with query params
		final Page<ErrandDTO> resultList = webTestClient.get().uri(
				uriBuilder -> UriComponentsBuilder.fromUri(uriBuilder.build())
					.path(format("/{0}/errands", municipalityId))
					.queryParam("filter", "stakeholders.addresses.street:'%s'".formatted(person.getAddresses().getFirst().getStreet()) +
						"and " +
						"stakeholders.addresses.houseNumber:'%s'".formatted(person.getAddresses().getFirst().getHouseNumber()))
					.encode()
					.build()
					.toUri())
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(new ParameterizedTypeReference<Page<ErrandDTO>>() {

			})
			.returnResult()
			.getResponseBody();

		assertEquals(1, requireNonNull(resultList).getTotalElements());
		final ErrandDTO result = resultList.getContent().getFirst();

		assertThat(inputPostErrandDTO_1)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				EXCLUDE_FIELDS)
			.isEqualTo(result);
	}

	@ParameterizedTest
	@ValueSource(strings = {"2281", "2061", "2062"})
	void testGetWithStakeholderQueryParams404(final String municipalityId) {
		final ErrandDTO inputPostErrandDTO_1 = createErrandDTO(municipalityId);
		postErrand(inputPostErrandDTO_1);

		final StakeholderDTO person = inputPostErrandDTO_1.getStakeholders().stream().filter(stakeholderDTO -> StakeholderType.PERSON.equals(stakeholderDTO.getType())).findFirst().orElseThrow();

		webTestClient.get().uri(
				uriBuilder -> UriComponentsBuilder.fromUri(uriBuilder.build())
					.path(format("/{0}/errands", municipalityId))
					.queryParam("filter", "stakeholders.firstName:'%s'".formatted(person.getFirstName()) +
						"and " +
						"stakeholders.lastName:'%s'".formatted(person.getLastName()) +
						"and " +
						"stakeholders.personId:'%s'".formatted(UUID.randomUUID()))
					.encode()
					.build()
					.toUri())
			.exchange()
			.expectStatus().isNotFound()
			.expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
	}

	@ParameterizedTest
	@ValueSource(strings = {"2281", "2061", "2062"})
	void testGetErrandsWithPersonId(final String municipalityId) {
		final ErrandDTO inputPostErrandDTO_1 = createErrandDTO(municipalityId);
		postErrand(inputPostErrandDTO_1);

		final StakeholderDTO person = inputPostErrandDTO_1.getStakeholders().stream().filter(stakeholderDTO -> StakeholderType.PERSON.equals(stakeholderDTO.getType())).findFirst().orElseThrow();
		// Get only the first one with query params
		final Page<ErrandDTO> resultList = webTestClient.get().uri(
				uriBuilder -> UriComponentsBuilder.fromUri(uriBuilder.build())
					.path(format("/{0}/errands", municipalityId))
					.queryParam("filter", "stakeholders.personId:'%s'".formatted(person.getPersonId()))
					.encode()
					.build()
					.toUri())
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(new ParameterizedTypeReference<Page<ErrandDTO>>() {

			})
			.returnResult()
			.getResponseBody();

		assertEquals(1, requireNonNull(resultList).getTotalElements());
		final ErrandDTO result = resultList.getContent().getFirst();

		assertThat(inputPostErrandDTO_1)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				EXCLUDE_FIELDS)
			.isEqualTo(result);
	}

	@ParameterizedTest
	@ValueSource(strings = {"2281", "2061", "2062"})
	void testGetErrandsWithOrganizationNumber(final String municipalityId) {
		final var errandDto = createErrandDTO(municipalityId);
		postErrand(errandDto);

		final StakeholderDTO organization = errandDto.getStakeholders().stream()
			.filter(stakeholderDTO -> stakeholderDTO.getType() == StakeholderType.ORGANIZATION)
			.findFirst().orElseThrow();
		// Get only the first one with query params
		final Page<ErrandDTO> resultList = webTestClient.get().uri(
				uriBuilder -> UriComponentsBuilder.fromUri(uriBuilder.build())
					.path(format("/{0}/errands", municipalityId))
					.queryParam("filter", "stakeholders.organizationNumber:'%s'".formatted(organization.getOrganizationNumber()))
					.encode()
					.build()
					.toUri())
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(new ParameterizedTypeReference<Page<ErrandDTO>>() {

			})
			.returnResult()
			.getResponseBody();

		assertEquals(1, requireNonNull(resultList).getTotalElements());
		final ErrandDTO result = resultList.getContent().getFirst();

		assertThat(errandDto)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				EXCLUDE_FIELDS)
			.isEqualTo(result);
	}

	@ParameterizedTest
	@ValueSource(strings = {"2281", "2061", "2062"})
	void testPatchErrandWithNote(final String municipalityId) throws JsonProcessingException {
		final var errandDto = createErrandDTO(municipalityId);
		final var errandId = postErrand(errandDto);

		final NoteDTO noteDTO = createNoteDTO(municipalityId);

		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(format("/{0}/errands/{1}/notes", municipalityId, errandId))
			.withRequest(OBJECT_MAPPER.writeValueAsString(noteDTO))
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of(format("/{0}/notes/(.*)$", municipalityId)))
			.sendRequestAndVerifyResponse();
	}

	@ParameterizedTest
	@ValueSource(strings = {"2281", "2061", "2062"})
	void testPatchErrandWithStakeholders(final String municipalityId) throws JsonProcessingException {
		final var errandDto = createErrandDTO(municipalityId);
		final var errandId = postErrand(errandDto);

		final StakeholderDTO stakeholderDTO = createStakeholderDTO(StakeholderType.ORGANIZATION, List.of(StakeholderRole.DRIVER.name()), municipalityId);

		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(format("/{0}/errands/{1}/stakeholders", municipalityId, errandId))
			.withRequest(OBJECT_MAPPER.writeValueAsString(stakeholderDTO))
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of(format("/{0}/stakeholders/(.*)$", municipalityId)))
			.sendRequestAndVerifyResponse();
	}

	@ParameterizedTest
	@ValueSource(strings = {"2281", "2061", "2062"})
	void testPatchErrandWithDecision(final String municipalityId) throws JsonProcessingException {
		final var errandDto = createErrandDTO(municipalityId);
		final var errandId = postErrand(errandDto);

		final DecisionDTO decisionDTO = createDecisionDTO(municipalityId);

		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(format(format("/{0}/errands/{1}/decisions", municipalityId, errandId)))
			.withRequest(OBJECT_MAPPER.writeValueAsString(decisionDTO))
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of(format("/{0}/decisions/(.*)$", municipalityId)))
			.sendRequestAndVerifyResponse();
	}

	@ParameterizedTest
	@ValueSource(strings = {"2281", "2061", "2062"})
	void testPatchErrandWithStatus(final String municipalityId) {
		final var errandDto = createErrandDTO(municipalityId);
		final var errandId = postErrand(errandDto);

		final var statusDTO = createStatusDTO();

		webTestClient.patch().uri(format("/{0}/errands/{1}/statuses", municipalityId, errandId))
			.bodyValue(statusDTO)
			.exchange()
			.expectStatus().isNoContent();

		final ErrandDTO result = webTestClient.get().uri(format("/{0}/errands/{1}", municipalityId, errandId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(ErrandDTO.class)
			.returnResult()
			.getResponseBody();

		assertThat(result).isNotNull().isNotEqualTo(errandDto);
		assertThat(result.getStatuses()).contains(statusDTO);
	}

	@ParameterizedTest
	@ValueSource(strings = {"2281", "2061", "2062"})
	void testPutErrandWithStatuses(final String municipalityId) {
		final var errandDto = createErrandDTO(municipalityId);
		final var errandId = postErrand(errandDto);

		final var statusDTOList = List.of(createStatusDTO(), createStatusDTO(), createStatusDTO());

		webTestClient.put().uri(format("/{0}/errands/{1}/statuses", municipalityId, errandId))
			.bodyValue(statusDTOList)
			.exchange()
			.expectStatus().isNoContent();

		final ErrandDTO result = webTestClient.get().uri(format("/{0}/errands/{1}", municipalityId, errandId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(ErrandDTO.class).returnResult().getResponseBody();

		assertThat(result).isNotNull().isNotEqualTo(errandDto);
		assertThat(result.getStatuses()).isEqualTo(statusDTOList);
	}

	@ParameterizedTest
	@ValueSource(strings = {"2281", "2061", "2062"})
	void testDelete(final String municipalityId) {

		final ErrandDTO inputErrandDTO = createErrandDTO(municipalityId);
		final var id = postErrand(inputErrandDTO);

		webTestClient.delete().uri(format("/{0}/errands/{1}", municipalityId, id))
			.exchange()
			.expectStatus().isNoContent();
	}

	@ParameterizedTest
	@ValueSource(strings = {"2281", "2061", "2062"})
	void testDeleteWhenNotFound(final String municipalityId) {

		final var nonExistingId = 666L;

		webTestClient.delete().uri(format("/{0}/errands/{1}", municipalityId, nonExistingId))
			.exchange()
			.expectStatus().isNotFound()
			.expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
	}

	private String postErrand(final ErrandDTO errandDTO) {
		final var location = webTestClient.post().uri(format("/{0}/errands", errandDTO.getMunicipalityId()))
			.bodyValue(errandDTO)
			.header(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.header(AD_USER_HEADER_KEY, AD_USER_HEADER_VALUE)
			.exchange()
			.expectStatus().isCreated()
			.returnResult(Object.class)
			.getResponseHeaders()
			.getLocation();

		assertNotNull(location);
		return location.toString().substring(location.toString().lastIndexOf("/") + 1);
	}
}
