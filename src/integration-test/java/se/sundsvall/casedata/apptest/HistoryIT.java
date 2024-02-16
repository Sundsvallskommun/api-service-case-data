package se.sundsvall.casedata.apptest;


import java.text.MessageFormat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.casedata.Application;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/HistoryIT", classes = Application.class)
@Sql({
	"/db/script/truncate.sql",
	"/db/script/historyIT-testdata.sql"
})
class HistoryIT extends CustomAbstractAppTest {

	private final String EXPECTED_FILE = "expected.json";

	@Test
	void test1_getErrandHistory() {
		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath(MessageFormat.format("/errands/{0}/history", 1L))
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_getAttachmentHistory() {
		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath(MessageFormat.format("/attachments/{0}/history", 2L))
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test3_getDecisionHistory() {
		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath(MessageFormat.format("/decisions/{0}/history", 3L))
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test4_getFacilitiesHistory() {
		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath(MessageFormat.format("/facilities/{0}/history", 4L))
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test5_getNotesHistory() {
		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath(MessageFormat.format("/notes/{0}/history", 5L))
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test6_getStakeholdersHistory() {
		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath(MessageFormat.format("/stakeholders/{0}/history", 6L))
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}
}
