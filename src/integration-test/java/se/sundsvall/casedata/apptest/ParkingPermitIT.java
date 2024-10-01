package se.sundsvall.casedata.apptest;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;
import static se.sundsvall.casedata.apptest.util.TestConstants.MUNICIPALITY_ID;
import static se.sundsvall.casedata.apptest.util.TestConstants.NAMESPACE;
import static se.sundsvall.casedata.apptest.util.TestConstants.RESPONSE_FILE;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.casedata.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/ParkingPermitIT", classes = Application.class)
@Sql({
	"/db/script/truncate.sql",
	"/db/script/parkingpermitIT-testdata.sql"
})
class ParkingPermitIT extends AbstractAppTest {

	private static final String PATH = "/" + MUNICIPALITY_ID + "/" + NAMESPACE + "/parking-permits";

	@Test
	void test01_getAllParkingPermits() {
		setupCall()
			.withServicePath(PATH)
			.withHttpMethod(GET)
			.withExpectedResponse(RESPONSE_FILE)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_getAllParkingPermitsByPersonId() {
		setupCall()
			.withServicePath(PATH + "?personId=d7af5f83-166a-468b-ab86-da8ca30ea97c")
			.withHttpMethod(GET)
			.withExpectedResponse(RESPONSE_FILE)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();
	}

}
