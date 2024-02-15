package se.sundsvall.casedata.apptest;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.casedata.CaseDataApplication;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/MessageAttachmentIT/", classes = CaseDataApplication.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class MessageAttachmentIT extends CustomAbstractAppTest {

	@Test
	void test01_getMessageAttachmentNotFound() {
		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath("/messageattachments/666")
			.withExpectedResponseStatus(HttpStatus.NOT_FOUND)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_getMessageAttachment() {
		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath("/messageattachments/05b29c30-4512-46c0-9d82-d0f11cb04bae")
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_getMessageAttachmentStreamedNotFound() {
		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath("/messageattachments/666/streamed")
			.withExpectedResponseStatus(HttpStatus.NOT_FOUND)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_getMessageAttachmentStreamed() throws Exception {
		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath("/messageattachments/05b29c30-4512-46c0-9d82-d0f11cb04bae/streamed")
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(IMAGE_PNG_VALUE))
			.withExpectedBinaryResponse("test_image.png")
			.sendRequestAndVerifyResponse();
	}
}
