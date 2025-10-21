package se.sundsvall.casedata.apptest;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.casedata.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/ErrandExtraParameterIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/errandExtraParameterIT-testdata.sql"
})
class ErrandExtraParameterIT extends AbstractAppTest {

	private static final String PATH = "/2281/MY_NAMESPACE/errands/";
	private static final String REQUEST_FILE = "request.json";
	private static final String RESPONSE_FILE = "response.json";
	private static final Long ERRAND_ID = 2L;

	@Test
	void test01_updateErrandParameters() {
		setupCall()
			.withServicePath(PATH + ERRAND_ID + "/extraparameters")
			.withHttpMethod(PATCH)
			.withExpectedResponseStatus(OK)
			.withRequest(REQUEST_FILE)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_readErrandParameter() {
		setupCall()
			.withServicePath(PATH + ERRAND_ID + "/extraparameters/key1")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_findErrandParameters() {
		setupCall()
			.withServicePath(PATH + ERRAND_ID + "/extraparameters")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_updateErrandParameter() {
		setupCall()
			.withServicePath(PATH + ERRAND_ID + "/extraparameters/key1")
			.withHttpMethod(PATCH)
			.withExpectedResponseStatus(OK)
			.withRequest(REQUEST_FILE)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_deleteErrandParameter() {
		final var parameterKey = "key2";

		setupCall()
			.withServicePath(PATH + 1L + "/extraparameters/" + parameterKey)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();

		setupCall()
			.withServicePath(PATH + 1L + "/extraparameters/" + parameterKey)
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();

		setupCall()
			.withServicePath(PATH + 1L + "/extraparameters/" + parameterKey)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test06_updateErrandParametersWithKeyDuplicates() {
		setupCall()
			.withServicePath(PATH + ERRAND_ID + "/extraparameters")
			.withHttpMethod(PATCH)
			.withExpectedResponseStatus(OK)
			.withRequest(REQUEST_FILE)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

}
