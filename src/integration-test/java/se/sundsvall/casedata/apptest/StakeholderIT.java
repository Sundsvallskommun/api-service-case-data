package se.sundsvall.casedata.apptest;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static se.sundsvall.casedata.apptest.util.TestConstants.AD_USER;
import static se.sundsvall.casedata.apptest.util.TestConstants.JWT_HEADER_VALUE;
import static se.sundsvall.casedata.service.util.Constants.AD_USER_HEADER_KEY;
import static se.sundsvall.casedata.service.util.Constants.X_JWT_ASSERTION_HEADER_KEY;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.casedata.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/StakeholderIT/", classes = Application.class)
@Sql({
	"/db/script/truncate.sql",
	"/db/script/stakeholderIT-testdata.sql"
})
class StakeholderIT extends AbstractAppTest {

	private static final Long STAKEHOLDER_ID = 1L;

	private static final String PATH = "/stakeholders";
	private static final String REQUEST_FILE = "request.json";
	private static final String EXPECTED_FILE = "expected.json";

	@Test
	void test01_getStakeholderById() {
		setupCall()
			.withServicePath(PATH + "/" + STAKEHOLDER_ID)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_getAllStakeholders() {
		setupCall()
			.withServicePath(PATH)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_getStakeholdersByRole() {
		final var requestParam = "?stakeholderRole=APPLICANT";
		setupCall()
			.withServicePath(PATH + requestParam)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_patchStakeholder() {
		setupCall()
			.withServicePath(PATH + "/" + STAKEHOLDER_ID)
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(AD_USER_HEADER_KEY, AD_USER)
			.withHttpMethod(PATCH)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		setupCall()
			.withServicePath(PATH + "/" + STAKEHOLDER_ID)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_putStakeholder() {
		setupCall()
			.withServicePath(PATH + "/" + STAKEHOLDER_ID)
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(AD_USER_HEADER_KEY, AD_USER)
			.withHttpMethod(PUT)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		setupCall()
			.withServicePath(PATH + "/" + STAKEHOLDER_ID)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}
}
