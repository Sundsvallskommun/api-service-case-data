package se.sundsvall.casedata.apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static se.sundsvall.casedata.apptest.util.TestConstants.AD_USER;
import static se.sundsvall.casedata.apptest.util.TestConstants.JWT_HEADER_VALUE;
import static se.sundsvall.casedata.service.util.Constants.X_JWT_ASSERTION_HEADER_KEY;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.integration.db.AttachmentRepository;
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

	@Autowired
	private AttachmentRepository attachmentRepository;

	@Test
	void test01_GetAttachment() {
		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath("/attachments/1")
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_GetAttachmentNotFound() {
		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath("/attachments/1000")
			.withExpectedResponseStatus(HttpStatus.NOT_FOUND)
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
			.withExpectedResponseStatus(HttpStatus.NO_CONTENT)
			.sendRequestAndVerifyResponse();

		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath("/attachments/1")
			.withExpectedResponseStatus(HttpStatus.OK)
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
			.withExpectedResponseStatus(HttpStatus.NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_DeleteAttachmentOnErrand() {
		assertThat(attachmentRepository.findById(3L)).isPresent();

		setupCall()
			.withHttpMethod(HttpMethod.DELETE)
			.withServicePath("/attachments/3")
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(Constants.AD_USER_HEADER_KEY, AD_USER)
			.withExpectedResponseStatus(HttpStatus.NO_CONTENT)
			.sendRequestAndVerifyResponse();

		assertThat(attachmentRepository.findById(3L)).isEmpty();
	}

	@Test
	void test06_DeleteAttachmentOnErrandNotFound() {
		setupCall()
			.withHttpMethod(HttpMethod.DELETE)
			.withServicePath("/attachments/10")
			.withExpectedResponseStatus(HttpStatus.NOT_FOUND)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test07_createAttachment() {

		var location = setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath("/attachments")
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(HttpStatus.CREATED)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse()
			.getResponseHeaders().get("Location").getFirst();

		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath(location)
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test08_patchAttachmentNotFound() {
		setupCall()
			.withHttpMethod(HttpMethod.PATCH)
			.withServicePath("/attachments/1000")
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(HttpStatus.NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test09_getAttachmentsByErrandNumber() {

		final var result = setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath("/attachments/errand/ERRAND-NUMBER-2")
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}
}