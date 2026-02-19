package se.sundsvall.casedata.service.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.support.Identifier;
import se.sundsvall.dept44.support.Identifier.Type;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceUtilTest {

	@AfterEach
	void afterEach() {
		Identifier.remove();
	}

	@Test
	void getAdUser() {

		// Arrange
		final var adAccount = "user123";

		Identifier.set(Identifier.create().withType(Type.AD_ACCOUNT).withValue(adAccount));

		// Act
		final var result = ServiceUtil.getAdUser();

		// Assert
		assertThat(result).isEqualTo(adAccount);
	}
}
