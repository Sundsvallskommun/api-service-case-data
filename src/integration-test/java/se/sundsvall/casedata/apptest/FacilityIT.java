package se.sundsvall.casedata.apptest;

import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.apptest.util.TestConstants.REQUEST_FILE;
import static se.sundsvall.casedata.apptest.util.TestConstants.RESPONSE_FILE;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.casedata.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(
	files = "classpath:/FacilityIT/",
	classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/facilityIT-testdata.sql"
})
class FacilityIT extends AbstractAppTest {

	private static final Long ERRAND_ID = 1L;

	private static final Long FACILITY_ID = 1L;

	private static final String FACILITIES_PATH = "/{municipalityId}/{namespace}/errands/{id}/facilities";

	private static final String FACILITY_PATH = "/{municipalityId}/{namespace}/errands/{id}/facilities/{facilityId}";


	@Test
	void test01_getFacilitiesByErrandId() {
		setupCall()
			.withServicePath(builder -> fromPath(FACILITIES_PATH)
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "id", ERRAND_ID)))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_getFacilityByErrandIdAndFacilityId() {
		setupCall()
			.withServicePath(builder -> fromPath(FACILITY_PATH)
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "id", ERRAND_ID, "facilityId", FACILITY_ID)))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_deleteFacilityByErrandIdAndFacilityId() {
		setupCall()
			.withServicePath(builder -> fromPath(FACILITY_PATH)
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "id", ERRAND_ID, "facilityId", FACILITY_ID)))
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_createFacilityByErrandId() {
		final var location = setupCall()
			.withServicePath(builder -> fromPath(FACILITIES_PATH)
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "id", ERRAND_ID)))
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withRequest(REQUEST_FILE)
			.sendRequestAndVerifyResponse()
			.getResponseHeaders().get(LOCATION).getFirst();

		setupCall()
			.withServicePath(location)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_updateFacilityByErrandIdAndFacilityId() {
		setupCall()
			.withServicePath(builder -> fromPath(FACILITY_PATH)
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "id", ERRAND_ID, "facilityId", FACILITY_ID)))
			.withHttpMethod(PATCH)
			.withExpectedResponseStatus(NO_CONTENT)
			.withRequest(REQUEST_FILE)
			.sendRequestAndVerifyResponse();

		setupCall()
			.withServicePath(builder -> fromPath(FACILITY_PATH)
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "id", ERRAND_ID, "facilityId", FACILITY_ID)))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test06_putFacilitiesByErrandId() {
		setupCall()
			.withServicePath(builder -> fromPath(FACILITIES_PATH)
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "id", ERRAND_ID)))
			.withHttpMethod(PUT)
			.withExpectedResponseStatus(NO_CONTENT)
			.withRequest(REQUEST_FILE)
			.sendRequestAndVerifyResponse();

		setupCall()
			.withServicePath(builder -> fromPath(FACILITIES_PATH)
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "id", ERRAND_ID)))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

}
