package se.sundsvall.casedata.service.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ConstantsTest {

	@Test
	void testConstantValues() {
		assertThat(Constants.DEPARTMENT_NAME_CONVERSATION).isEqualTo("CONVERSATION");
		assertThat(Constants.DEPARTMENT_NAME_PARATRANSIT).isEqualTo("PARATRANSIT");
	}
}
