package se.sundsvall.casedata.service.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConstantsTest {

	@Test
	void testConstantValues() {
		assertThat(Constants.DEPARTMENT_NAME_CONVERSATION).isEqualTo("CONVERSATION");
		assertThat(Constants.DEPARTMENT_NAME_PARATRANSIT).isEqualTo("PARATRANSIT");
	}
}
