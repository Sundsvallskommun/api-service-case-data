package se.sundsvall.casedata.apptest;

import static java.text.MessageFormat.format;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static se.sundsvall.casedata.apptest.util.TestConstants.AD_USER;
import static se.sundsvall.casedata.apptest.util.TestConstants.JWT_HEADER_VALUE;
import static se.sundsvall.casedata.apptest.util.TestConstants.MUNICIPALITY_ID;
import static se.sundsvall.casedata.apptest.util.TestConstants.NAMESPACE;
import static se.sundsvall.casedata.apptest.util.TestConstants.REQUEST_FILE;
import static se.sundsvall.casedata.apptest.util.TestConstants.RESPONSE_FILE;
import static se.sundsvall.casedata.service.util.Constants.X_JWT_ASSERTION_HEADER_KEY;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.service.util.Constants;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/AttachmentIT", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/attachmentIT-testdata.sql"
})
class AttachmentIT extends AbstractAppTest {

	private static final Long ERRAND_ID = 1L;

	private static final String PATH = "/{0}/{1}/errands/{2}/attachments/{3}";

	@Test
	void test01_GetAttachment() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "1"))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_GetAttachmentNotFound() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "1000"))
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_PutAttachment() {
		setupCall()
			.withHttpMethod(HttpMethod.PUT)
			.withServicePath(format(PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "1"))
			.withRequest(REQUEST_FILE)
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(Constants.AD_USER_HEADER_KEY, AD_USER)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "1"))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_PutAttachmentNotFound() {
		setupCall()
			.withHttpMethod(HttpMethod.PUT)
			.withServicePath(format(PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "10"))
			.withRequest(REQUEST_FILE)
			.withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_DeleteAttachmentOnErrand() {

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "3"))
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();

		setupCall()
			.withHttpMethod(HttpMethod.DELETE)
			.withServicePath(format(PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "3"))
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(Constants.AD_USER_HEADER_KEY, AD_USER)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "3"))
			.withExpectedResponseStatus(NOT_FOUND)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test06_DeleteAttachmentOnErrandNotFound() {
		setupCall()
			.withHttpMethod(HttpMethod.DELETE)
			.withServicePath(format(PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "10"))
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test07_createAttachment() {
		final var location = setupCall()
			.withHttpMethod(POST)
			.withServicePath(format("/{0}/{1}/errands/{2}/attachments", MUNICIPALITY_ID, NAMESPACE, ERRAND_ID))
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse()
			.getResponseHeaders().get("Location").getFirst();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(location)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test08_patchAttachmentNotFound() {
		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(format(PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "1000"))
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test09_getAttachmentsByErrandNumber() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format("/{0}/{1}/attachments/errand/{2}", MUNICIPALITY_ID, NAMESPACE, "ERRAND-NUMBER-2"))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test10_patchAttachment() {
		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(format(PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "1"))
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "1"))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

}
