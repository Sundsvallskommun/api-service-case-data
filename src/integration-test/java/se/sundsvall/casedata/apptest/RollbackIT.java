package se.sundsvall.casedata.apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static se.sundsvall.casedata.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casedata.TestUtil.createErrandDTO;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARKING_PERMIT;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;

import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/RollbackIT", classes = Application.class)
class RollbackIT extends CustomAbstractAppTest {

	@Autowired
	private ErrandRepository errandRepository;

	// Simulate HTTP 500 response from POST /start-process to ProcessEngine. No errand should be persisted.
	@Test
	void test01_500rollback() throws JsonProcessingException {

		final List<Errand> listBefore = errandRepository.findAll();
		final var errandDTO = createErrandDTO();
		errandDTO.setCaseType(PARKING_PERMIT.name());

		setupCall()
			.withHttpMethod(POST)
			.withServicePath("/errands")
			.withRequest(OBJECT_MAPPER.writeValueAsString(errandDTO))
			.withExpectedResponseStatus(INTERNAL_SERVER_ERROR)
			.sendRequestAndVerifyResponse();

		final List<Errand> listAfter = errandRepository.findAll();

		// Verify that no errand was persisted
		assertThat(listBefore).hasSameSizeAs(listAfter);
	}
}
