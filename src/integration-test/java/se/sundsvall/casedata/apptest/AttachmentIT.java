package se.sundsvall.casedata.apptest;

import static java.text.MessageFormat.format;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static se.sundsvall.casedata.apptest.util.TestConstants.AD_USER_HEADER_KEY;
import static se.sundsvall.casedata.apptest.util.TestConstants.JWT_HEADER_VALUE;
import static se.sundsvall.casedata.apptest.util.TestConstants.MUNICIPALITY_ID;
import static se.sundsvall.casedata.apptest.util.TestConstants.NAMESPACE;
import static se.sundsvall.casedata.apptest.util.TestConstants.REQUEST_FILE;
import static se.sundsvall.casedata.apptest.util.TestConstants.RESPONSE_FILE;
import static se.sundsvall.casedata.service.util.Constants.X_JWT_ASSERTION_HEADER_KEY;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.casedata.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/AttachmentIT", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/attachmentIT-testdata.sql"
})
class AttachmentIT extends AbstractAppTest {

	private static final Long ERRAND_ID = 1L;
	private static final String ATTACHMENTS_PATH = "/{0}/{1}/errands/{2}/attachments";
	private static final String ATTACHMENT_BY_ID_PATH = "/{0}/{1}/errands/{2}/attachments/{3}";

	@Test
	void test01_getAttachment() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "1"))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_getAttachmentNotFound() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "1000"))
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_putAttachment() {
		setupCall()
			.withHttpMethod(PUT)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "1"))
			.withRequest(REQUEST_FILE)
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(AD_USER_HEADER_KEY, "someUser123")
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequest();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "1"))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_putAttachmentNotFound() {
		setupCall()
			.withHttpMethod(PUT)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "10"))
			.withRequest(REQUEST_FILE)
			.withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_deleteAttachmentOnErrand() {

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "1"))
			.withExpectedResponseStatus(OK)
			.sendRequest();

		setupCall()
			.withHttpMethod(DELETE)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "1"))
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(AD_USER_HEADER_KEY, "user123")
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequest();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "1"))
			.withExpectedResponseStatus(NOT_FOUND)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test06_deleteAttachmentOnErrandNotFound() {
		setupCall()
			.withHttpMethod(DELETE)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "10"))
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test07_createAttachment() {
		final var location = setupCall()
			.withHttpMethod(POST)
			.withServicePath(format(ATTACHMENTS_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID))
			.withHeader(AD_USER_HEADER_KEY, "user123")
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseBodyIsNull()
			.sendRequest()
			.getResponseHeaders().get(LOCATION).getFirst();

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
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "1000"))
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test09_getAttachmentsByErrandId() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENTS_PATH, MUNICIPALITY_ID, NAMESPACE, 2))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test10_patchAttachment() {
		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "1"))
			.withHeader("sentbyuser", "user123")
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequest();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "1"))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}
}
