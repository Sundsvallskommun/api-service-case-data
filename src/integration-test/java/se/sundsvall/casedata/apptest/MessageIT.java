package se.sundsvall.casedata.apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;
import static se.sundsvall.casedata.apptest.util.TestConstants.MUNICIPALITY_ID;
import static se.sundsvall.casedata.apptest.util.TestConstants.NAMESPACE;
import static se.sundsvall.casedata.apptest.util.TestConstants.REQUEST_FILE;
import static se.sundsvall.casedata.apptest.util.TestConstants.RESPONSE_FILE;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.integration.db.MessageRepository;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(
	files = "classpath:/MessageIT/",
	classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/messageIT-testdata.sql"
})
class MessageIT extends AbstractAppTest {

	private static final String MESSAGE_ID = "a8883fb9-60b4-4f38-9f48-642070ff49ee";
	private static final String MESSAGE_ATTACHMENT_ID = "05b29c30-4512-46c0-9d82-d0f11cb04bae";
	private static final Long ERRAND_ID = 1L;
	private static final String PATH = "/" + MUNICIPALITY_ID + "/" + NAMESPACE + "/errands/" + ERRAND_ID + "/messages";

	@Autowired
	private MessageRepository messageRepository;

	@Test
	void test01_getMessage() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(PATH)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_postMessage() {
		final var location = setupCall()
			.withServicePath(PATH)
			.withHttpMethod(POST)
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
	void test03_updateViewedStatus() {
		final var viewed = false;
		setupCall()
			.withServicePath(PATH + "/" + MESSAGE_ID + "/viewed/" + viewed)
			.withHttpMethod(PUT)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		assertThat(messageRepository.findById(MESSAGE_ID).orElseThrow().isViewed()).isFalse();
	}

	@Test
	void test04_getMessageAttachmentStreamed() throws Exception {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(PATH + "/" + MESSAGE_ID + "/attachments/" + MESSAGE_ATTACHMENT_ID)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(IMAGE_PNG_VALUE))
			.withExpectedBinaryResponse("test_image.png")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_getMessageAttachmentStreamedNotFound() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(PATH + "/" + MESSAGE_ID + "/attachments/nonexistingid")
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}
}
