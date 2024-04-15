package se.sundsvall.casedata.apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static se.sundsvall.casedata.apptest.util.TestConstants.AD_USER;
import static se.sundsvall.casedata.apptest.util.TestConstants.JWT_HEADER_VALUE;
import static se.sundsvall.casedata.integration.db.model.enums.DecisionOutcome.DISMISSAL;
import static se.sundsvall.casedata.integration.db.model.enums.DecisionType.FINAL;
import static se.sundsvall.casedata.service.util.Constants.AD_USER_HEADER_KEY;
import static se.sundsvall.casedata.service.util.Constants.X_JWT_ASSERTION_HEADER_KEY;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.integration.db.DecisionRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(
	files = "classpath:/DecisionIT/",
	classes = Application.class)
@Sql({
	"/db/script/truncate.sql",
	"/db/script/decisionIT-testdata.sql"
})
class DecisionIT extends CustomAbstractAppTest {

	private static final Long DECISION_ID = 1L;
	private static final Long ERRAND_ID = 1L;

	private static final String PATH = "/decisions";
	private static final String REQUEST_FILE = "request.json";
	private static final String EXPECTED_FILE = "expected.json";

	@Autowired
	private DecisionRepository decisionRepository;

	@Autowired
	private ErrandRepository errandRepository;

	@Test
	void test1_getDecisionById() {
		setupCall()
			.withServicePath(PATH + "/" + DECISION_ID)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_patchDecision() {
		setupCall()
			.withServicePath(PATH + "/" + DECISION_ID)
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(AD_USER_HEADER_KEY, AD_USER)
			.withHttpMethod(PATCH)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		final var updatedDecision = decisionRepository.findById(DECISION_ID).orElseThrow();
		assertThat(updatedDecision).satisfies(decision1 -> {
			assertThat(decision1.getDescription()).isEqualTo("new description");
			assertThat(decision1.getDecisionType()).isEqualTo(FINAL);
			assertThat(decision1.getDecisionOutcome()).isEqualTo(DISMISSAL);
		});
		final var updatedErrand = errandRepository.findById(ERRAND_ID).orElseThrow();
		assertThat(updatedErrand.getUpdatedByClient()).isEqualTo("WSO2_MS_caseManagement");
	}

	@Test
	void test3_putDecision() {
		setupCall()
			.withServicePath(PATH + "/" + DECISION_ID)
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(AD_USER_HEADER_KEY, AD_USER)
			.withHttpMethod(PUT)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		final var replacedDecision = decisionRepository.findById(DECISION_ID).orElseThrow();
		assertThat(replacedDecision).satisfies(decision1 -> {
			assertThat(decision1.getDescription()).isEqualTo("This is a put description");
			assertThat(decision1.getDecisionType()).isEqualTo(FINAL);
			assertThat(decision1.getDecisionOutcome()).isEqualTo(DISMISSAL);
		});
		final var updatedErrand = errandRepository.findById(ERRAND_ID).orElseThrow();
		assertThat(updatedErrand.getUpdatedByClient()).isEqualTo("WSO2_MS_caseManagement");
	}
}
