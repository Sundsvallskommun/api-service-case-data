package se.sundsvall.casedata.apptest;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static se.sundsvall.casedata.apptest.util.TestConstants.AD_USER;
import static se.sundsvall.casedata.apptest.util.TestConstants.JWT_HEADER_VALUE;
import static se.sundsvall.casedata.service.util.Constants.X_JWT_ASSERTION_HEADER_KEY;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.service.util.Constants;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/AttachmentResourceIT", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class AttachmentResourceIT extends AbstractAppTest {

	private static final String REQUEST_FILE = "request.json";
	private static final String RESPONSE_FILE = "response.json";

	@Test
	void test01_GetAttachment() {
		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath("/attachments/1")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_GetAttachmentNotFound() {
		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath("/attachments/1000")
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_PutAttachment() {
		setupCall()
			.withHttpMethod(HttpMethod.PUT)
			.withServicePath("/attachments/1")
			.withRequest(REQUEST_FILE)
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(Constants.AD_USER_HEADER_KEY, AD_USER)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();

		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath("/attachments/1")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_PutAttachmentNotFound() {
		setupCall()
			.withHttpMethod(HttpMethod.PUT)
			.withServicePath("/attachments/10")
			.withRequest(REQUEST_FILE)
			.withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_DeleteAttachmentOnErrand() {

		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath("/attachments/3")
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();

		setupCall()
			.withHttpMethod(HttpMethod.DELETE)
			.withServicePath("/attachments/3")
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(Constants.AD_USER_HEADER_KEY, AD_USER)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();

		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath("/attachments/3")
			.withExpectedResponseStatus(NOT_FOUND)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test06_DeleteAttachmentOnErrandNotFound() {
		setupCall()
			.withHttpMethod(HttpMethod.DELETE)
			.withServicePath("/attachments/10")
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test07_createAttachment() {
		final var location = setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath("/attachments")
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse()
			.getResponseHeaders().get("Location").getFirst();

		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath(location)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test08_patchAttachmentNotFound() {
		setupCall()
			.withHttpMethod(HttpMethod.PATCH)
			.withServicePath("/attachments/1000")
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test09_getAttachmentsByErrandNumber() {
		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath("/attachments/errand/ERRAND-NUMBER-2")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}
}
