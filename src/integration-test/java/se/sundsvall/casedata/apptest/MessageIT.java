package se.sundsvall.casedata.apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static se.sundsvall.casedata.apptest.util.TestConstants.MUNICIPALITY_ID;
import static se.sundsvall.casedata.apptest.util.TestConstants.NAMESPACE;
import static se.sundsvall.casedata.apptest.util.TestConstants.REQUEST_FILE;
import static se.sundsvall.casedata.apptest.util.TestConstants.RESPONSE_FILE;

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

	private static final String ERRAND_NUMBER = "ERRAND-NUMBER-1";

	private static final Long ERRAND_ID = 1L;


	private static final String ERRAND_NUMBER_PATH = "/" + MUNICIPALITY_ID + "/" + NAMESPACE + "/messages/" + ERRAND_NUMBER;

	private static final String PATH = "/" + MUNICIPALITY_ID + "/" + NAMESPACE + "/errands/" + ERRAND_ID + "/messages";


	@Autowired
	private MessageRepository messageRepository;

	@Test
	void test01_getMessageOnErrand() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(ERRAND_NUMBER_PATH)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
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
			.withServicePath(ERRAND_NUMBER_PATH)
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

}
