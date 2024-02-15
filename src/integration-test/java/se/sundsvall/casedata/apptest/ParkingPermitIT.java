package se.sundsvall.casedata.apptest;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.casedata.CaseDataApplication;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/ParkingPermitIT", classes = CaseDataApplication.class)
class ParkingPermitIT extends CustomAbstractAppTest {

	private static final String PATH = "/parking-permits";
	private static final String EXPECTED_FILE = "expected.json";

	@Test
	@Sql({
		"/db/script/truncate.sql",
		"/db/script/parkingpermitIT-testdata.sql"
	})
	void test1_getAllParkingPermits() {
		setupCall()
			.withServicePath(PATH)
			.withHttpMethod(GET)
			.withExpectedResponse(EXPECTED_FILE)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();
	}

	@Test
	@Sql({
		"/db/script/truncate.sql",
		"/db/script/parkingpermitIT-testdata.sql"
	})
	void test2_getAllParkingPermitsByPersonId() {
		setupCall()
			.withServicePath(PATH + "?personId=d7af5f83-166a-468b-ab86-da8ca30ea97c")
			.withHttpMethod(GET)
			.withExpectedResponse(EXPECTED_FILE)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();
	}
}
