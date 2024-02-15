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

import se.sundsvall.casedata.CaseDataApplication;
import se.sundsvall.casedata.integration.db.MessageRepository;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(
	files = "classpath:/MessageIT/",
	classes = CaseDataApplication.class
)
@Sql({
	"/db/script/truncate.sql",
	"/db/script/messageIT-testdata.sql"
})
class MessageIT extends CustomAbstractAppTest {

	private static final String MESSAGE_ID = "a8883fb9-60b4-4f38-9f48-642070ff49ee";
	private static final String ERRAND_NUMBER = "ERRAND-NUMBER-1";

	private static final String PATH = "/messages";
	private static final String REQUEST_FILE = "request.json";
	private static final String EXPECTED_FILE = "expected.json";

	@Autowired
	private MessageRepository messageRepository;

	@Test
	void test1_getMessageOnErrand() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(PATH + "/" + ERRAND_NUMBER)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_patchErrandWithMessage() {
		var requestMessageId = "a1883fb9-60b4-4f38-9f48-642070ff49ee";
		setupCall()
			.withServicePath(PATH)
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		var patchedMessage = messageRepository.findById(requestMessageId).orElseThrow();
		assertThat(patchedMessage).satisfies(message -> {
			assertThat(message.getErrandNumber()).isEqualTo("ERRAND-NUMBER-2");
			assertThat(message.getMessageID()).isEqualTo(requestMessageId);
			assertThat(message.isViewed()).isFalse();
			assertThat(message.getFamilyID()).isEqualTo("123");
		});
	}

	@Test
	void test3_updateViewedStatus() {
		var viewed = false;
		setupCall()
			.withServicePath(PATH + "/" + MESSAGE_ID + "/viewed/" + viewed)
			.withHttpMethod(PUT)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();
		var updatedMessage = messageRepository.findById(MESSAGE_ID).orElseThrow();
		assertThat(updatedMessage.isViewed()).isFalse();
	}
}
