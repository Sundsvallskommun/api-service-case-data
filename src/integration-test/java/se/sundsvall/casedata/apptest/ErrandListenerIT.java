package se.sundsvall.casedata.apptest;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.casedata.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

import static java.text.MessageFormat.format;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static se.sundsvall.casedata.apptest.util.TestConstants.JWT_HEADER_VALUE;
import static se.sundsvall.casedata.apptest.util.TestConstants.MUNICIPALITY_ID;
import static se.sundsvall.casedata.apptest.util.TestConstants.NAMESPACE;
import static se.sundsvall.casedata.service.util.Constants.AD_USER_HEADER_KEY;
import static se.sundsvall.casedata.service.util.Constants.X_JWT_ASSERTION_HEADER_KEY;

@WireMockAppTestSuite(files = "classpath:/ErrandListenerIT", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/errandIT-testdata.sql"
})
class ErrandListenerIT extends AbstractAppTest {

	private static final int ERRAND_ID = 3;

	private static final int PATCH_ERRAND_ID = 2;
	private static final String REQUEST_FILE = "request.json";
	private static final String EXPECTED_FILE = "expected.json";
	private String namespace;

	private String getPath() {
		return format("/{0}/{1}/errands", MUNICIPALITY_ID, namespace);
	}

	@Test
	void test01_persistErrandUnknown() {
		namespace = "SBK_PARKINGPERMIT";
		setupCall()
			.withHttpMethod(POST)
			.withServicePath(getPath())
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.sendRequest();

		setupCall()
			.withServicePath(getPath() + "/" + ERRAND_ID)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_updateErrand() {
		namespace = NAMESPACE;
		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(getPath() + "/" + PATCH_ERRAND_ID)
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(AD_USER_HEADER_KEY, "PatchUser")
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequest();

		setupCall()
			.withServicePath(getPath() + "/" + PATCH_ERRAND_ID)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();

	}

	@Test
	void test03_generateErrandNumberForParkingPermit() {
		namespace = "SBK_PARKINGPERMIT";
		setupCall()
			.withHttpMethod(POST)
			.withServicePath(getPath())
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.sendRequest();

		setupCall()
			.withServicePath(getPath() + "/" + ERRAND_ID)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_generateErrandNumberForLostParkingPermit() {
		namespace = "SBK_PARKINGPERMIT";
		setupCall()
			.withHttpMethod(POST)
			.withServicePath(getPath())
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.sendRequest();

		setupCall()
			.withServicePath(getPath() + "/" + ERRAND_ID)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_generateErrandNumberForMEX() {
		namespace = "SBK_MEX";
		setupCall()
			.withHttpMethod(POST)
			.withServicePath(getPath())
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.sendRequest();

		setupCall()
			.withServicePath(getPath() + "/" + ERRAND_ID)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

}
