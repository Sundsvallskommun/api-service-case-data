package se.sundsvall.casedata.apptest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.integration.db.AppealRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.enums.AppealStatus;
import se.sundsvall.casedata.integration.db.model.enums.TimelinessReview;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static se.sundsvall.casedata.apptest.util.TestConstants.AD_USER;
import static se.sundsvall.casedata.apptest.util.TestConstants.JWT_HEADER_VALUE;
import static se.sundsvall.casedata.service.util.Constants.AD_USER_HEADER_KEY;
import static se.sundsvall.casedata.service.util.Constants.X_JWT_ASSERTION_HEADER_KEY;

@WireMockAppTestSuite(
	files = "classpath:/AppealIT/",
	classes = Application.class)
@Sql({
	"/db/script/truncate.sql",
	"/db/script/appealIT-testdata.sql"
})
class AppealIT extends CustomAbstractAppTest {

	private static final Long APPEAL_ID = 1L;
	private static final Long ERRAND_ID = 1L;

	private static final String PATH = "/appeals";
	private static final String REQUEST_FILE = "request.json";
	private static final String EXPECTED_FILE = "expected.json";

	@Autowired
	private AppealRepository appealRepository;

	@Autowired
	private ErrandRepository errandRepository;

	@Test
	void test1_getAppealById() {
		setupCall()
			.withServicePath(PATH + "/" + APPEAL_ID)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_patchAppeal() {
		setupCall()
			.withServicePath(PATH + "/" + APPEAL_ID)
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(AD_USER_HEADER_KEY, AD_USER)
			.withHttpMethod(PATCH)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		final var updatedAppeal = appealRepository.findById(APPEAL_ID).orElseThrow();
		assertThat(updatedAppeal).satisfies(appeal -> {
			assertThat(appeal.getDescription()).isEqualTo("new description");
			assertThat(appeal.getStatus()).isEqualTo(AppealStatus.SENT_TO_COURT);
			assertThat(appeal.getTimelinessReview()).isEqualTo(TimelinessReview.NOT_RELEVANT);
		});
		final var updatedErrand = errandRepository.findById(ERRAND_ID).orElseThrow();
		assertThat(updatedErrand.getUpdatedByClient()).isEqualTo("WSO2_MS_caseManagement");
	}

	@Test
	void test3_putAppeal() {
		setupCall()
			.withServicePath(PATH + "/" + APPEAL_ID)
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(AD_USER_HEADER_KEY, AD_USER)
			.withHttpMethod(PUT)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		final var replacedAppeal = appealRepository.findById(APPEAL_ID).orElseThrow();
		assertThat(replacedAppeal).satisfies(appeal -> {
			assertThat(appeal.getDescription()).isEqualTo("This is a put description");
			assertThat(appeal.getStatus()).isEqualTo(AppealStatus.SENT_TO_COURT);
			assertThat(appeal.getTimelinessReview()).isEqualTo(TimelinessReview.NOT_RELEVANT);
			assertThat(appeal.getAppealConcernCommunicatedAt()).isEqualTo(OffsetDateTime.parse("2024-04-02T12:00:00.000+00:00"));
			assertThat(appeal.getDecision().getId()).isEqualTo(2L);
		});
		final var updatedErrand = errandRepository.findById(ERRAND_ID).orElseThrow();
		assertThat(updatedErrand.getUpdatedByClient()).isEqualTo("WSO2_MS_caseManagement");
	}
}