package se.sundsvall.casedata.apptest;

import static java.text.MessageFormat.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static se.sundsvall.casedata.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casedata.TestUtil.createErrandDTO;
import static se.sundsvall.casedata.apptest.util.TestConstants.JWT_HEADER_VALUE;
import static se.sundsvall.casedata.apptest.util.TestConstants.MUNICIPALITY_ID;
import static se.sundsvall.casedata.apptest.util.TestConstants.REQUEST_FILE;
import static se.sundsvall.casedata.service.util.Constants.AD_USER_HEADER_KEY;
import static se.sundsvall.casedata.service.util.Constants.UNKNOWN;
import static se.sundsvall.casedata.service.util.Constants.X_JWT_ASSERTION_HEADER_KEY;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.ErrandDTO;
import se.sundsvall.casedata.api.model.validation.enums.CaseType;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.dept44.test.annotation.resource.Load;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;

@WireMockAppTestSuite(files = "classpath:/ErrandListenerIT", classes = Application.class)
@Sql({
	"/db/script/truncate.sql",
	"/db/script/errandIT-testdata.sql"
})
@ExtendWith(ResourceLoaderExtension.class)
class ErrandListenerIT extends CustomAbstractAppTest {

	@Autowired
	private ErrandRepository errandRepository;

	@Test
	void test1_persistErrandUnknown() throws JsonProcessingException {
		setupCall()
			.withHttpMethod(POST)
			.withServicePath(format("/{0}/errands", MUNICIPALITY_ID))
			.withRequest(OBJECT_MAPPER.writeValueAsString(createErrandDTO(MUNICIPALITY_ID)))
			.withExpectedResponseStatus(CREATED)
			.sendRequestAndVerifyResponse();

		final Errand errand = errandRepository.findAll().stream().max(Comparator.comparing(Errand::getCreated)).orElseThrow();

		assertThat(errand).satisfies(e -> {
			assertThat(e.getErrandNumber()).isNotNull();
			assertThat(e.getCreatedByClient()).isEqualTo(UNKNOWN);
			assertThat(e.getCreatedBy()).isEqualTo(UNKNOWN);
			assertThat(e.getUpdatedByClient()).isEqualTo(UNKNOWN);
			assertThat(e.getUpdatedBy()).isEqualTo(UNKNOWN);
		});
	}

	@Test
	void test2_updateErrand(@Load("ErrandListenerIT/__files/test2_updateErrand/request.json") final String request) {
		final var requestJson = JsonParser.parseString(request).getAsJsonObject();
		final var beforePatch = errandRepository.findByIdAndMunicipalityId(1L, MUNICIPALITY_ID).orElseThrow();

		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(format("/{0}/errands/{1}", MUNICIPALITY_ID, 1L))
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(AD_USER_HEADER_KEY, "PatchUser")
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();

		final var afterPatch = errandRepository.findByIdAndMunicipalityId(1L, MUNICIPALITY_ID).orElseThrow();

		assertThat(afterPatch).satisfies(after -> {
			assertThat(after.getDiaryNumber()).isNotEqualTo(beforePatch.getDiaryNumber());
			assertThat(after.getDescription()).isNotEqualTo(beforePatch.getDescription());
			assertThat(after.getPriority()).isNotEqualTo(beforePatch.getPriority());
			assertThat(after.getUpdatedByClient()).isNotEqualTo(beforePatch.getUpdatedByClient());
			assertThat(after.getUpdatedBy()).isNotEqualTo(beforePatch.getUpdatedBy());
		});
		assertThat(afterPatch).satisfies(after -> {
			assertThat(after.getDiaryNumber()).isEqualTo(requestJson.get("diaryNumber").getAsString());
			assertThat(after.getDescription()).isEqualTo(requestJson.get("description").getAsString());
			assertThat(after.getPriority().name()).isEqualTo(requestJson.get("priority").getAsString());
		});
	}

	@Test
	void test3_generateErrandNumberForSameAbbreviation() throws JsonProcessingException {

		final ErrandDTO errandDTO1 = createErrandDTO(MUNICIPALITY_ID);
		errandDTO1.setCaseType(CaseType.PARKING_PERMIT.name());
		setupCall()
			.withHttpMethod(POST)
			.withServicePath(format("/{0}/errands", MUNICIPALITY_ID))
			.withRequest(OBJECT_MAPPER.writeValueAsString(errandDTO1))
			.withExpectedResponseStatus(CREATED)
			.sendRequestAndVerifyResponse();

		final ErrandDTO errandDTO2 = createErrandDTO(MUNICIPALITY_ID);
		errandDTO2.setCaseType(CaseType.LOST_PARKING_PERMIT.name());
		setupCall()
			.withHttpMethod(POST)
			.withServicePath(format("/{0}/errands", MUNICIPALITY_ID))
			.withRequest(OBJECT_MAPPER.writeValueAsString(errandDTO2))
			.withExpectedResponseStatus(CREATED)
			.sendRequestAndVerifyResponse();

		final ErrandDTO errandDTO3 = createErrandDTO(MUNICIPALITY_ID);
		errandDTO3.setCaseType(CaseType.PARKING_PERMIT_RENEWAL.name());
		setupCall()
			.withHttpMethod(POST)
			.withServicePath(format("/{0}/errands", MUNICIPALITY_ID))
			.withRequest(OBJECT_MAPPER.writeValueAsString(errandDTO3))
			.withExpectedResponseStatus(CREATED)
			.sendRequestAndVerifyResponse();

		final var errands = errandRepository.findAll().stream()
			.filter(errand -> errand.getCreated().isAfter(OffsetDateTime.now().minusMinutes(2)))
			.toList();

		for (int i = 0; i < errands.size(); i++) {
			assertThat(errands.get(i).getErrandNumber()).isEqualTo(format("{0}-{1}-{2}", CaseType.PARKING_PERMIT.getAbbreviation(), String.valueOf(LocalDate.now().getYear()), String.format("%06d", i + 1)));
		}
	}

	@ParameterizedTest
	@EnumSource(CaseType.class)
	void test4_generateErrandNumberForDifferentAbbreviation(final CaseType caseType) throws JsonProcessingException {
		final ErrandDTO errandDTO1 = createErrandDTO(MUNICIPALITY_ID);
		errandDTO1.setCaseType(caseType.name());

		setupCall()
			.withHttpMethod(POST)
			.withServicePath(format("/{0}/errands", MUNICIPALITY_ID))
			.withRequest(OBJECT_MAPPER.writeValueAsString(errandDTO1))
			.withExpectedResponseStatus(CREATED)
			.sendRequestAndVerifyResponse();

		final List<Errand> resultList = errandRepository.findAll();
		resultList.forEach(errand -> assertThat(errand.getErrandNumber()).isNotBlank());
	}
}
