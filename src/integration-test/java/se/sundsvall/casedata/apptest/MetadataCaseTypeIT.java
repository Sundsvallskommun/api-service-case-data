package se.sundsvall.casedata.apptest;

import static java.text.MessageFormat.format;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static se.sundsvall.casedata.apptest.util.TestConstants.MUNICIPALITY_ID;
import static se.sundsvall.casedata.apptest.util.TestConstants.NAMESPACE;
import static se.sundsvall.casedata.apptest.util.TestConstants.REQUEST_FILE;
import static se.sundsvall.casedata.apptest.util.TestConstants.RESPONSE_FILE;

import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.casedata.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/MetadataCaseTypeIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/metadataCaseTypeIT-testdata.sql"
})
class MetadataCaseTypeIT extends AbstractAppTest {

	private static final String PATH = "/{0}/{1}/metadata/casetypes";
	private static final String TYPE = "type-1";

	@Test
	void test01_getCaseTypes() {
		setupCall()
			.withServicePath(format(PATH, MUNICIPALITY_ID, NAMESPACE))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_getCaseType() {
		setupCall()
			.withServicePath(format(PATH + "/{2}", MUNICIPALITY_ID, NAMESPACE, TYPE))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_createCaseType() {

		final var location = Objects.requireNonNull(setupCall()
			.withServicePath(format(PATH, MUNICIPALITY_ID, NAMESPACE))
			.withHttpMethod(POST)
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
	void test04_deleteCaseType() {

		setupCall()
			.withServicePath(format(PATH + "/{2}", MUNICIPALITY_ID, NAMESPACE, TYPE))
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT);

		setupCall()
			.withServicePath(format(PATH + "/{2}", MUNICIPALITY_ID, NAMESPACE, TYPE))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponseBodyIsNull();

	}

}
