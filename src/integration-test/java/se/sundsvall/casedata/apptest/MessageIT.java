package se.sundsvall.casedata.apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

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
	"/db/script/truncate.sql",
	"/db/script/messageIT-testdata.sql"
})
class MessageIT extends AbstractAppTest {

	private static final String MESSAGE_ID = "a8883fb9-60b4-4f38-9f48-642070ff49ee";
	private static final String ERRAND_NUMBER = "ERRAND-NUMBER-1";
	private static final String PATH = "/2281/messages";
	private static final String REQUEST_FILE = "request.json";
	private static final String EXPECTED_FILE = "expected.json";

	@Autowired
	private MessageRepository messageRepository;

	@Test
	void test01_getMessageOnErrand() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(PATH + "/" + ERRAND_NUMBER)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_patchErrandWithMessage() {
		setupCall()
			.withServicePath(PATH)
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(PATH + "/" + ERRAND_NUMBER)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
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
}
