package se.sundsvall.casedata.apptest;

import static org.springframework.http.HttpStatus.OK;

import java.text.MessageFormat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.casedata.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/HistoryIT", classes = Application.class)
@Sql({
	"/db/script/truncate.sql",
	"/db/script/historyIT-testdata.sql"
})
class HistoryIT extends AbstractAppTest {

	private final String EXPECTED_FILE = "expected.json";

	@Test
	void test01_getErrandHistory() {
		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath(MessageFormat.format("/errands/{0}/history", 1L))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_getAttachmentHistory() {
		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath(MessageFormat.format("/attachments/{0}/history", 2L))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_getDecisionHistory() {
		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath(MessageFormat.format("/decisions/{0}/history", 3L))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_getFacilitiesHistory() {
		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath(MessageFormat.format("/facilities/{0}/history", 4L))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_getNotesHistory() {
		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath(MessageFormat.format("/notes/{0}/history", 5L))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test06_getStakeholdersHistory() {
		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath(MessageFormat.format("/stakeholders/{0}/history", 6L))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}
}
