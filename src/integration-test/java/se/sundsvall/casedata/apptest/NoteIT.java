package se.sundsvall.casedata.apptest;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.casedata.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

import static java.text.MessageFormat.format;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static se.sundsvall.casedata.apptest.util.TestConstants.AD_USER;
import static se.sundsvall.casedata.apptest.util.TestConstants.JWT_HEADER_VALUE;
import static se.sundsvall.casedata.apptest.util.TestConstants.MUNICIPALITY_ID;
import static se.sundsvall.casedata.apptest.util.TestConstants.REQUEST_FILE;
import static se.sundsvall.casedata.apptest.util.TestConstants.RESPONSE_FILE;
import static se.sundsvall.casedata.service.util.Constants.AD_USER_HEADER_KEY;
import static se.sundsvall.casedata.service.util.Constants.X_JWT_ASSERTION_HEADER_KEY;

@WireMockAppTestSuite(files = "classpath:/NoteIT/", classes = Application.class)
@Sql({
	"/db/script/truncate.sql",
	"/db/script/noteIT-testdata.sql"
})
class NoteIT extends AbstractAppTest {

	@Test
	void test01_getNoteById() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format("/{0}/notes/{1}", MUNICIPALITY_ID, 1L))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_getNotesByErrandId() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format("/{0}/notes/errand/{1}", MUNICIPALITY_ID, 1L))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_patchNoteOnErrand() {

		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(format("/{0}/notes/{1}", MUNICIPALITY_ID, 1L))
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(AD_USER_HEADER_KEY, AD_USER)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequest();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format("/{0}/notes/{1}", MUNICIPALITY_ID, 1L))
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(AD_USER_HEADER_KEY, AD_USER)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();

	}

	@Test
	void test04_deleteNoteById() {
		setupCall()
			.withHttpMethod(DELETE)
			.withServicePath(format("/{0}/notes/{1}", MUNICIPALITY_ID, 1L))
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();
	}
}
