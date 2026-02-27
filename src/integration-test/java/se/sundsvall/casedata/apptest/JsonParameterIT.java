package se.sundsvall.casedata.apptest;

import static java.text.MessageFormat.format;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static se.sundsvall.casedata.apptest.util.TestConstants.JWT_HEADER_VALUE;
import static se.sundsvall.casedata.apptest.util.TestConstants.MUNICIPALITY_ID;
import static se.sundsvall.casedata.apptest.util.TestConstants.NAMESPACE;
import static se.sundsvall.casedata.apptest.util.TestConstants.REQUEST_FILE;
import static se.sundsvall.casedata.apptest.util.TestConstants.RESPONSE_FILE;
import static se.sundsvall.casedata.service.util.Constants.X_JWT_ASSERTION_HEADER_KEY;
import static se.sundsvall.dept44.support.Identifier.HEADER_NAME;

import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.casedata.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/JsonParameterIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/jsonParameterIT-testdata.sql"
})
class JsonParameterIT extends AbstractAppTest {

	private static final String PATH = "/{0}/{1}/errands";

	@Test
	void test01_createErrandWithJsonParameters() {

		final var location = Objects.requireNonNull(setupCall()
			.withHttpMethod(POST)
			.withServicePath(format(PATH, MUNICIPALITY_ID, "SBK_PARKING_PERMIT"))
			.withHeader(HEADER_NAME, "type=adAccount; user123")
			.withExpectedResponseStatus(CREATED)
			.withRequest(REQUEST_FILE)
			.sendRequest()
			.getResponseHeaders().get(LOCATION)).getFirst();

		setupCall()
			.withServicePath(location)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_updateErrandWithJsonParameters() {

		setupCall()
			.withServicePath(format(PATH + "/{2}", MUNICIPALITY_ID, NAMESPACE, 2))
			.withHttpMethod(PATCH)
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(HEADER_NAME, "type=adAccount; user123")
			.withExpectedResponseStatus(NO_CONTENT)
			.withRequest(REQUEST_FILE)
			.sendRequest();

		setupCall()
			.withServicePath(format(PATH + "/{2}", MUNICIPALITY_ID, NAMESPACE, 2))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_deleteJsonParameters() {

		setupCall()
			.withServicePath(format(PATH + "/{2}", MUNICIPALITY_ID, NAMESPACE, 1))
			.withHttpMethod(PATCH)
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(HEADER_NAME, "type=adAccount; user123")
			.withExpectedResponseStatus(NO_CONTENT)
			.withRequest(REQUEST_FILE)
			.sendRequest();

		setupCall()
			.withServicePath(format(PATH + "/{2}", MUNICIPALITY_ID, NAMESPACE, 1))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_getErrandWithJsonParameters() {
		setupCall()
			.withServicePath(format(PATH + "/{2}", MUNICIPALITY_ID, NAMESPACE, 1))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_createErrandWithInvalidJsonParameters() {
		setupCall()
			.withHttpMethod(POST)
			.withServicePath(format(PATH, MUNICIPALITY_ID, "SBK_PARKING_PERMIT"))
			.withHeader(HEADER_NAME, "type=adAccount; user123")
			.withExpectedResponseStatus(BAD_REQUEST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test06_updateErrandWithInvalidJsonParameters() {
		setupCall()
			.withServicePath(format(PATH + "/{2}", MUNICIPALITY_ID, NAMESPACE, 2))
			.withHttpMethod(PATCH)
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(HEADER_NAME, "type=adAccount; user123")
			.withExpectedResponseStatus(BAD_REQUEST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

}
