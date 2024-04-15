package se.sundsvall.casedata.apptest;

import static se.sundsvall.casedata.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casedata.TestUtil.createErrandDTO;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARKING_PERMIT;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

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
	void test1_500rollback() throws JsonProcessingException {
		final List<Errand> listBefore = errandRepository.findAll();
		final var errandDTO = createErrandDTO();
		errandDTO.setCaseType(PARKING_PERMIT.name());

		setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath("/errands")
			.withRequest(OBJECT_MAPPER.writeValueAsString(errandDTO))
			.withExpectedResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
			.sendRequestAndVerifyResponse();

		final List<Errand> listAfter = errandRepository.findAll();

		// Verify that no errand was persisted
		Assertions.assertEquals(listBefore.size(), listAfter.size());
	}
}
