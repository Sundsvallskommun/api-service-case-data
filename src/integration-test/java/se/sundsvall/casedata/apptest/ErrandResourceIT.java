package se.sundsvall.casedata.apptest;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
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
import static se.sundsvall.casedata.TestUtil.createNoteDTO;
import static se.sundsvall.casedata.TestUtil.createStakeholderDTO;
import static se.sundsvall.casedata.TestUtil.createStatusDTO;
import static se.sundsvall.casedata.TestUtil.getRandomOffsetDateTime;
import static se.sundsvall.casedata.apptest.util.TestConstants.JWT_HEADER_VALUE;
import static se.sundsvall.casedata.apptest.util.TestConstants.PARKING_PERMIT_START_URL;
import static se.sundsvall.casedata.api.model.enums.CaseType.LOST_PARKING_PERMIT;
import static se.sundsvall.casedata.api.model.enums.CaseType.PARKING_PERMIT;
import static se.sundsvall.casedata.api.model.enums.CaseType.PARKING_PERMIT_RENEWAL;
import static se.sundsvall.casedata.service.util.Constants.AD_USER_HEADER_KEY;
import static se.sundsvall.casedata.service.util.Constants.X_JWT_ASSERTION_HEADER_KEY;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.api.model.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;
import se.sundsvall.casedata.service.util.Constants;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/ErrandResourceIT", classes = Application.class)
class ErrandResourceIT extends CustomAbstractAppTest {

	private static final String AD_USER_HEADER_VALUE = "user";

	@Autowired
	private ErrandRepository errandRepository;

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
		"note.*\\.updatedBy"
	};

	@Autowired
	private WebTestClient webTestClient;

	@BeforeEach
	void beforeEach() {
		errandRepository.deleteAll();
	}

	@Test
	void testPostErrand() {
		final ErrandDTO inputErrandDTO = createErrandDTO();
		inputErrandDTO.setCaseType(PARKING_PERMIT);
		final String id = postErrand(inputErrandDTO);

		final ErrandDTO getErrandDTO = webTestClient.get().uri("/errands/{id}", id)
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

	@Test
	void testPostMinimalErrand() {
		final ErrandDTO inputErrandDTO = new ErrandDTO();
		inputErrandDTO.setCaseType(PARKING_PERMIT);
		final String id = postErrand(inputErrandDTO);

		final ErrandDTO getErrandDTO = webTestClient.get().uri("/errands/{id}", id)
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

	@Test
	void testPatchErrand() {
		final ErrandDTO inputPostErrandDTO = createErrandDTO();
		final String id = postErrand(inputPostErrandDTO);

		// Get posted object
		final ErrandDTO resultPostErrandDTO = webTestClient.get().uri("/errands/{id}", id)
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

		// Patch the object
		webTestClient.patch().uri("/errands/{id}", id)
			.bodyValue(inputPatchErrandDTO)
			.exchange()
			.expectStatus().isNoContent();

		// Get patched object
		final ErrandDTO resultPatchErrandDTO = webTestClient.get().uri("/errands/{id}", id)
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

	@Test
	void testGetWithOneQueryParam() {

		final ErrandDTO inputPostErrandDTO = createErrandDTO();
		inputPostErrandDTO.setCaseType(PARKING_PERMIT);
		// Create initial errand
		postErrand(inputPostErrandDTO);

		final Page<ErrandDTO> resultList = webTestClient.get().uri(
				uriBuilder -> uriBuilder
					.path("errands")
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


	@Test
	void testGetWithExtraParameter() {
		final ErrandDTO inputPostErrandDTO = createErrandDTO();
		inputPostErrandDTO.setCaseType(PARKING_PERMIT_RENEWAL);
		final Map<String, String> extraParameters = new HashMap<>();
		extraParameters.put("key 1", "value 1");
		extraParameters.put("key 2", "value 2");
		inputPostErrandDTO.setExtraParameters(extraParameters);
		// Create initial errand
		postErrand(inputPostErrandDTO);

		final Page<ErrandDTO> resultList = webTestClient.get().uri(
				uriBuilder -> uriBuilder
					.path("errands")
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

	@Test
	void testGetWithExtraParameterMismatch() {
		final ErrandDTO inputPostErrandDTO = createErrandDTO();
		final Map<String, String> extraParameters = new HashMap<>();
		extraParameters.put("key 1", "value 1");
		extraParameters.put("key 2", "value 2");
		inputPostErrandDTO.setExtraParameters(extraParameters);
		// Create initial errand
		postErrand(inputPostErrandDTO);

		webTestClient.get().uri(
				uriBuilder -> uriBuilder
					.path("errands")
					.queryParam("extraParameters[key 1]", "value 1")
					// One of the extra parameters is wrong
					.queryParam("extraParameters[key 2]", "value 3")
					.build())
			.exchange()
			.expectStatus().isNotFound();
	}

	@Test
	void testGetWithExtraParameterAndFilterMismatch() {
		final ErrandDTO inputPostErrandDTO = createErrandDTO();
		final Map<String, String> extraParameters = new HashMap<>();
		extraParameters.put("key 1", "value 1");
		extraParameters.put("key 2", "value 2");
		inputPostErrandDTO.setExtraParameters(extraParameters);
		// Create initial errand
		postErrand(inputPostErrandDTO);

		webTestClient.get().uri(
				uriBuilder -> uriBuilder
					.path("errands")
					.queryParam("extraParameters[key 1]", "value 1")
					// Filter is wrong
					.queryParam("filter", "externalCaseId:'%s'".formatted(RandomStringUtils.randomNumeric(10)))
					.build())
			.exchange()
			.expectStatus().isNotFound();
	}

	@Test
	void testGetWithFilterPageableAndExtraParameters() {
		final ErrandDTO inputPostErrandDTO = createErrandDTO();
		inputPostErrandDTO.setCaseType(LOST_PARKING_PERMIT);
		final Map<String, String> extraParameters = new HashMap<>();
		extraParameters.put("key 1", "value 1");
		inputPostErrandDTO.setExtraParameters(extraParameters);
		// Create initial errand
		postErrand(inputPostErrandDTO);

		final Page<ErrandDTO> resultList = webTestClient.get().uri(
				uriBuilder -> uriBuilder
					.path("errands")
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

	@Test
	void testGetWithQueryParamTextContains() {

		final String NAME_PREFIX = "abc";
		final String WORD_IN_THE_MIDDLE = "word-in-the-middle";

		final ErrandDTO inputPostErrandDTO = createErrandDTO();
		inputPostErrandDTO.getStakeholders().getFirst().setFirstName(NAME_PREFIX + WORD_IN_THE_MIDDLE + RandomStringUtils.random(10));
		postErrand(inputPostErrandDTO);

		final ErrandDTO anotherErrandWithSameFirstName = createErrandDTO();
		anotherErrandWithSameFirstName.getStakeholders().getFirst().setFirstName(NAME_PREFIX + WORD_IN_THE_MIDDLE + RandomStringUtils.random(10));
		postErrand(anotherErrandWithSameFirstName);

		final Page<ErrandDTO> resultPage = webTestClient.get().uri(
				uriBuilder -> uriBuilder
					.path("errands")
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

	@Test
	void testGetWithOneQueryParam404() {
		webTestClient.get().uri(
				uriBuilder -> uriBuilder
					.path("errands")
					.queryParam("filter", "externalCaseId:'%s'".formatted(UUID.randomUUID()))
					.build())
			.exchange()
			.expectStatus().isNotFound()
			.expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
	}

	@Test
	void testGetWithMultipleQueryParams() {
		final ErrandDTO inputPostErrandDTO_1 = createErrandDTO();
		inputPostErrandDTO_1.setCaseType(LOST_PARKING_PERMIT);
		final String id = postErrand(inputPostErrandDTO_1);

		final ErrandDTO resultPostErrandDTO_1 = webTestClient.get().uri("/errands/{id}", id)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(ErrandDTO.class).returnResult().getResponseBody();

		// Get only the first one with query params
		final Page<ErrandDTO> resultList = webTestClient.get().uri(
				uriBuilder -> UriComponentsBuilder.fromUri(uriBuilder.build())
					.path("errands")
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
	@Test
	void testGetWithMultipleQueryParams2() {

		final ErrandDTO inputPostErrandDTO_1 = createErrandDTO();
		inputPostErrandDTO_1.setCaseType(PARKING_PERMIT_RENEWAL);
		final String id = postErrand(inputPostErrandDTO_1);

		final ErrandDTO resultPostErrandDTO_1 = webTestClient.get().uri("/errands/{id}", id)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(ErrandDTO.class).returnResult().getResponseBody();

		final Page<ErrandDTO> resultList = webTestClient.get().uri(
				uriBuilder -> UriComponentsBuilder.fromUri(uriBuilder.build())
					.path("errands")
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

	@Test
	void testGetWithStakeholderQueryParams() {
		final ErrandDTO inputPostErrandDTO_1 = createErrandDTO();
		postErrand(inputPostErrandDTO_1);

		final StakeholderDTO person = inputPostErrandDTO_1.getStakeholders().stream().filter(stakeholderDTO -> stakeholderDTO.getType().equals(StakeholderType.PERSON)).findFirst().orElseThrow();

		// Get only the first one with query params
		final Page<ErrandDTO> resultList = webTestClient.get().uri(
			uriBuilder -> UriComponentsBuilder.fromUri(uriBuilder.build())
				.path("errands")
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

	@Test
	void testGetWithStakeholderAddressQueryParams() {
		final ErrandDTO inputPostErrandDTO_1 = createErrandDTO();
		postErrand(inputPostErrandDTO_1);

		final StakeholderDTO person = inputPostErrandDTO_1.getStakeholders().stream().filter(stakeholderDTO -> StakeholderType.PERSON.equals(stakeholderDTO.getType())).findFirst().orElseThrow();

		// Get only the first one with query params
		final Page<ErrandDTO> resultList = webTestClient.get().uri(
				uriBuilder -> UriComponentsBuilder.fromUri(uriBuilder.build())
					.path("errands")
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

	@Test
	void testGetWithStakeholderQueryParams404() {
		final ErrandDTO inputPostErrandDTO_1 = createErrandDTO();
		postErrand(inputPostErrandDTO_1);

		final StakeholderDTO person = inputPostErrandDTO_1.getStakeholders().stream().filter(stakeholderDTO -> StakeholderType.PERSON.equals(stakeholderDTO.getType())).findFirst().orElseThrow();

		webTestClient.get().uri(
			uriBuilder -> UriComponentsBuilder.fromUri(uriBuilder.build())
				.path("errands")
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

	@Test
	void testGetErrandsWithPersonId() {
		final ErrandDTO inputPostErrandDTO_1 = createErrandDTO();
		postErrand(inputPostErrandDTO_1);

		final StakeholderDTO person = inputPostErrandDTO_1.getStakeholders().stream().filter(stakeholderDTO -> StakeholderType.PERSON.equals(stakeholderDTO.getType())).findFirst().orElseThrow();
		// Get only the first one with query params
		final Page<ErrandDTO> resultList = webTestClient.get().uri(
			uriBuilder -> UriComponentsBuilder.fromUri(uriBuilder.build())
				.path("errands")
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

	@Test
	void testGetErrandsWithOrganizationNumber() {
		final var errandDto = createErrandDTO();
		postErrand(errandDto);

		final StakeholderDTO organization = errandDto.getStakeholders().stream()
			.filter(stakeholderDTO -> stakeholderDTO.getType() == StakeholderType.ORGANIZATION)
			.findFirst().orElseThrow();
		// Get only the first one with query params
		final Page<ErrandDTO> resultList = webTestClient.get().uri(
			uriBuilder -> UriComponentsBuilder.fromUri(uriBuilder.build())
				.path("errands")
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

	@Test
	void testPatchErrandWithNote() throws JsonProcessingException {
		final var errandDto = createErrandDTO();
		final var errandId = postErrand(errandDto);

		final NoteDTO noteDTO = createNoteDTO();

		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(MessageFormat.format("/errands/{0}/notes", errandId))
			.withRequest(OBJECT_MAPPER.writeValueAsString(noteDTO))
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^http://localhost:(\\d){2,5}/notes/(.*)$"))
			.sendRequestAndVerifyResponse();
	}

	@Test
	void testPatchErrandWithStakeholders() throws JsonProcessingException {
		final var errandDto = createErrandDTO();
		final var errandId = postErrand(errandDto);

		final StakeholderDTO stakeholderDTO = createStakeholderDTO(StakeholderType.ORGANIZATION, List.of(StakeholderRole.DRIVER));

		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(MessageFormat.format("/errands/{0}/stakeholders", errandId))
			.withRequest(OBJECT_MAPPER.writeValueAsString(stakeholderDTO))
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^http://localhost:(\\d){2,5}/stakeholders/(.*)$"))
			.sendRequestAndVerifyResponse();
	}

	@Test
	void testPatchErrandWithDecision() throws JsonProcessingException {
		final var errandDto = createErrandDTO();
		final var errandId = postErrand(errandDto);

		final DecisionDTO decisionDTO = createDecisionDTO();

		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(MessageFormat.format("/errands/{0}/decisions", errandId))
			.withRequest(OBJECT_MAPPER.writeValueAsString(decisionDTO))
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^http://localhost:(\\d){2,5}/decisions/(.*)$"))
			.sendRequestAndVerifyResponse();
	}

	@Test
	void testPatchErrandWithStatus() {
		final var errandDto = createErrandDTO();
		final var errandId = postErrand(errandDto);

		final var statusDTO = createStatusDTO();

		webTestClient.patch().uri("/errands/{id}/statuses", errandId)
			.bodyValue(statusDTO)
			.exchange()
			.expectStatus().isNoContent();

		final ErrandDTO result = webTestClient.get().uri("errands/{id}", errandId)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(ErrandDTO.class)
			.returnResult()
			.getResponseBody();

		assertThat(result).isNotNull().isNotEqualTo(errandDto);
		assertThat(result.getStatuses()).contains(statusDTO);
	}

	@Test
	void testPutErrandWithStatuses() {
		final var errandDto = createErrandDTO();
		final var errandId = postErrand(errandDto);

		final var statusDTOList = List.of(createStatusDTO(), createStatusDTO(), createStatusDTO());

		webTestClient.put().uri("/errands/{id}/statuses", errandId)
			.bodyValue(statusDTOList)
			.exchange()
			.expectStatus().isNoContent();

		final ErrandDTO result = webTestClient.get().uri("errands/{id}", errandId)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON_VALUE)
			.expectBody(ErrandDTO.class).returnResult().getResponseBody();

		assertThat(result).isNotNull().isNotEqualTo(errandDto);
		assertThat(result.getStatuses()).isEqualTo(statusDTOList);
	}

	private String postErrand(final ErrandDTO errandDTO) {
		final var location = webTestClient.post().uri("/errands")
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
