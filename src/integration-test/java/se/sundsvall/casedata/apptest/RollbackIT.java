package se.sundsvall.casedata.apptest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@WireMockAppTestSuite(files = "classpath:/RollbackIT", classes = Application.class)
@Sql({
	"/db/script/truncate.sql"
})
class RollbackIT extends AbstractAppTest {

	private static final String REQUEST_FILE = "request.json";
	private static final String EXPECTED_FILE = "expected.json";

	@Autowired
	private ErrandRepository errandRepository;

	// Simulate HTTP 500 response from POST for starting process to ParkingPermit. No errand should be persisted.
	@Test
	void test01_500rollback() {

		setupCall()
			.withHttpMethod(POST)
			.withServicePath("/2281/errands")
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(SERVICE_UNAVAILABLE)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequest();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath("/2281/errands/1")
			.withExpectedResponseStatus(NOT_FOUND)
			.sendRequestAndVerifyResponse();
	}
}
