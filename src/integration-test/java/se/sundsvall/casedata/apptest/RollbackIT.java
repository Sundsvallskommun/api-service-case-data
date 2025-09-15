package se.sundsvall.casedata.apptest;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static se.sundsvall.casedata.apptest.util.TestConstants.MUNICIPALITY_ID;
import static se.sundsvall.casedata.apptest.util.TestConstants.REQUEST_FILE;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.casedata.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/RollbackIT", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/errandIT-testdata.sql"
})
class RollbackIT extends AbstractAppTest {

	private static final String EXPECTED_FILE = "expected.json";

	private static final String PATH = "/" + MUNICIPALITY_ID + "/SBK_PARKING_PERMIT/errands";

	// Simulate HTTP 500 response from POST for starting process to ParkingPermit. No errand should be persisted.
	@Test
	void test01_500rollback() {

		setupCall()
			.withHttpMethod(POST)
			.withServicePath(PATH)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(SERVICE_UNAVAILABLE)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequest();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(PATH + "/1")
			.withExpectedResponseStatus(NOT_FOUND)
			.sendRequestAndVerifyResponse();
	}

}
