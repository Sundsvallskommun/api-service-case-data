package se.sundsvall.casedata.apptest;

import static java.text.MessageFormat.format;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static se.sundsvall.casedata.apptest.util.TestConstants.AD_USER;
import static se.sundsvall.casedata.apptest.util.TestConstants.JWT_HEADER_VALUE;
import static se.sundsvall.casedata.apptest.util.TestConstants.MUNICIPALITY_ID;
import static se.sundsvall.casedata.apptest.util.TestConstants.NAMESPACE;
import static se.sundsvall.casedata.apptest.util.TestConstants.REQUEST_FILE;
import static se.sundsvall.casedata.apptest.util.TestConstants.RESPONSE_FILE;
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

	private static final Long ERRAND_ID = 1L;

	private static final String PATH = "/{0}/{1}/errands/{2}/stakeholders";

	@Test
	void test01_getStakeholderById() {
		setupCall()
			.withServicePath(format(PATH + "/{3}", MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, 1L))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_getAllStakeholders() {
		setupCall()
			.withServicePath(format(PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_getStakeholdersByRole() {
		final var requestParam = "?stakeholderRole=APPLICANT";
		setupCall()
			.withServicePath(format(PATH + "{3}", MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, requestParam))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_patchStakeholder() {
		setupCall()
			.withServicePath(format(PATH + "/{3}", MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, 1L))
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(AD_USER_HEADER_KEY, AD_USER)
			.withHttpMethod(PATCH)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		setupCall()
			.withServicePath(format(PATH + "/{3}", MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, 1L))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_putStakeholder() {
		setupCall()
			.withServicePath(format(PATH + "/{3}", MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, 1L))
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(AD_USER_HEADER_KEY, AD_USER)
			.withHttpMethod(PUT)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		setupCall()
			.withServicePath(format(PATH + "/{3}", MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, 1L))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test06_putStakeholders() {
		setupCall()
			.withServicePath(format(PATH, MUNICIPALITY_ID, NAMESPACE, 1L))
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(AD_USER_HEADER_KEY, AD_USER)
			.withHttpMethod(PUT)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequest();

		setupCall()
			.withServicePath(format(PATH, MUNICIPALITY_ID, NAMESPACE, 1L))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequest();

		verifyAllStubs();
	}

}
