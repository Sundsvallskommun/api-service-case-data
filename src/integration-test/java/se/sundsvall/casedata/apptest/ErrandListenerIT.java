package se.sundsvall.casedata.apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static se.sundsvall.casedata.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casedata.TestUtil.createErrandDTO;
import static se.sundsvall.casedata.apptest.util.TestConstants.JWT_HEADER_VALUE;
import static se.sundsvall.casedata.service.util.Constants.UNKNOWN;
import static se.sundsvall.casedata.service.util.Constants.X_JWT_ASSERTION_HEADER_KEY;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.core.JsonProcessingException;

import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.ErrandDTO;
import se.sundsvall.casedata.api.model.PatchErrandDTO;
import se.sundsvall.casedata.api.model.validation.enums.CaseType;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.service.util.Constants;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/ErrandListenerIT", classes = Application.class)
class ErrandListenerIT extends CustomAbstractAppTest {

	@Autowired
	private ErrandRepository errandRepository;

	@BeforeEach
	void beforeEach() {
		errandRepository.deleteAll();
	}

	@Test
	void test1_persistErrandUnknown() throws JsonProcessingException {
		setupCall()
			.withHttpMethod(POST)
			.withServicePath("/errands")
			.withRequest(OBJECT_MAPPER.writeValueAsString(createErrandDTO()))
			.withExpectedResponseStatus(CREATED)
			.sendRequestAndVerifyResponse();

		final Errand errand = errandRepository.findAll().stream().max(Comparator.comparing(Errand::getCreated)).orElseThrow();

		assertEquals(UNKNOWN, errand.getCreatedByClient());
		assertEquals(UNKNOWN, errand.getCreatedBy());
		assertNotNull(errand.getErrandNumber());
		// The errand gets an update directly because we update with processId
		assertEquals(UNKNOWN, errand.getUpdatedByClient());
		assertEquals(UNKNOWN, errand.getUpdatedBy());
	}

	@Test
	void test2_updateErrand() throws JsonProcessingException {
		setupCall()
			.withHttpMethod(POST)
			.withServicePath("/errands")
			.withRequest(OBJECT_MAPPER.writeValueAsString(createErrandDTO()))
			.withExpectedResponseStatus(CREATED)
			.sendRequestAndVerifyResponse();

		final Errand errandBeforePatch = errandRepository.findAll().stream().max(Comparator.comparing(Errand::getCreated)).orElseThrow();

		final PatchErrandDTO patchErrandDTO = new PatchErrandDTO();
		patchErrandDTO.setDiaryNumber("Patch");

		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(MessageFormat.format("/errands/{0}", errandBeforePatch.getId()))
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(Constants.AD_USER_HEADER_KEY, "PatchUser")
			.withRequest(OBJECT_MAPPER.writeValueAsString(patchErrandDTO))
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();

		final Errand errandAfterPatch = errandRepository.findAll().stream().max(Comparator.comparing(Errand::getCreated)).orElseThrow();

		assertNotEquals(errandBeforePatch.getUpdatedByClient(), errandAfterPatch.getUpdatedByClient());
		assertNotEquals(errandBeforePatch.getUpdatedBy(), errandAfterPatch.getUpdatedBy());
	}

	@Test
	void test3_generateErrandNumberForSameAbbreviation() throws JsonProcessingException {

		final ErrandDTO errandDTO1 = createErrandDTO();
		errandDTO1.setCaseType(CaseType.PARKING_PERMIT.name());
		setupCall()
			.withHttpMethod(POST)
			.withServicePath("/errands")
			.withRequest(OBJECT_MAPPER.writeValueAsString(errandDTO1))
			.withExpectedResponseStatus(CREATED)
			.sendRequestAndVerifyResponse();

		final ErrandDTO errandDTO2 = createErrandDTO();
		errandDTO2.setCaseType(CaseType.LOST_PARKING_PERMIT.name());
		setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath("/errands")
			.withRequest(OBJECT_MAPPER.writeValueAsString(errandDTO2))
			.withExpectedResponseStatus(CREATED)
			.sendRequestAndVerifyResponse();

		final ErrandDTO errandDTO3 = createErrandDTO();
		errandDTO3.setCaseType(CaseType.PARKING_PERMIT_RENEWAL.name());
		setupCall()
			.withHttpMethod(POST)
			.withServicePath("/errands")
			.withRequest(OBJECT_MAPPER.writeValueAsString(errandDTO3))
			.withExpectedResponseStatus(CREATED)
			.sendRequestAndVerifyResponse();

		final List<Errand> resultList = errandRepository.findAll();

		for (int i = 0; i < resultList.size(); i++) {
			assertEquals(MessageFormat.format("{0}-{1}-{2}", CaseType.PARKING_PERMIT.getAbbreviation(), String.valueOf(LocalDate.now().getYear()), String.format("%06d", i + 1)), resultList.get(i).getErrandNumber());
		}
		resultList.forEach(errand -> assertTrue(errand.getErrandNumber().startsWith(CaseType.PARKING_PERMIT.getAbbreviation())));
	}

	@ParameterizedTest
	@EnumSource(CaseType.class)
	void test4_generateErrandNumberForDifferentAbbreviation(final CaseType caseType) throws JsonProcessingException {
		final ErrandDTO errandDTO1 = createErrandDTO();
		errandDTO1.setCaseType(caseType.name());

		setupCall()
			.withHttpMethod(POST)
			.withServicePath("/errands")
			.withRequest(OBJECT_MAPPER.writeValueAsString(errandDTO1))
			.withExpectedResponseStatus(CREATED)
			.sendRequestAndVerifyResponse();

		final List<Errand> resultList = errandRepository.findAll();
		resultList.forEach(errand -> assertThat(errand.getErrandNumber()).isNotBlank());
	}
}
