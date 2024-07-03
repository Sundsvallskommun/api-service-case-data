package se.sundsvall.casedata.apptest;

import static java.text.MessageFormat.format;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;
import static se.sundsvall.casedata.apptest.util.TestConstants.MUNICIPALITY_ID;
import static se.sundsvall.casedata.apptest.util.TestConstants.RESPONSE_FILE;

import org.junit.jupiter.api.Test;
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

	@Test
	void test01_getErrandHistory() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format("/{0}/errands/{1}/history", MUNICIPALITY_ID, 1L))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_getAttachmentHistory() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format("/{0}/attachments/{1}/history", MUNICIPALITY_ID, 2L))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_getDecisionHistory() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format("/{0}/decisions/{1}/history", MUNICIPALITY_ID, 3L))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_getFacilitiesHistory() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format("/{0}/facilities/{1}/history", MUNICIPALITY_ID, 4L))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_getNotesHistory() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format("/{0}/notes/{1}/history", MUNICIPALITY_ID, 5L))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test06_getStakeholdersHistory() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format("/{0}/stakeholders/{1}/history", MUNICIPALITY_ID, 6L))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}
}
