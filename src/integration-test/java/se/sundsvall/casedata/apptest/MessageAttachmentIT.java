package se.sundsvall.casedata.apptest;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;
import static se.sundsvall.casedata.apptest.util.TestConstants.MUNICIPALITY_ID;
import static se.sundsvall.casedata.apptest.util.TestConstants.NAMESPACE;
import static se.sundsvall.casedata.apptest.util.TestConstants.RESPONSE_FILE;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.casedata.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/MessageAttachmentIT/", classes = Application.class)
@Sql({
	"/db/script/truncate.sql",
	"/db/script/attachmentIT-testdata.sql"
})
class MessageAttachmentIT extends AbstractAppTest {

	private static final String PATH = "/" + MUNICIPALITY_ID + "/" + NAMESPACE + "/messageattachments/";

	@Test
	void test01_getMessageAttachmentNotFound() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(PATH + "666")
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_getMessageAttachment() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(PATH + "05b29c30-4512-46c0-9d82-d0f11cb04bae")
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_getMessageAttachmentStreamedNotFound() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(PATH + "666/streamed")
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_getMessageAttachmentStreamed() throws Exception {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(PATH + "05b29c30-4512-46c0-9d82-d0f11cb04bae/streamed")
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(IMAGE_PNG_VALUE))
			.withExpectedBinaryResponse("test_image.png")
			.sendRequestAndVerifyResponse();
	}

}
