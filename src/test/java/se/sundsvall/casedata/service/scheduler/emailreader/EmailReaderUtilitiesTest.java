package se.sundsvall.casedata.service.scheduler.emailreader;

import generated.se.sundsvall.emailreader.Email;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.casedata.service.scheduler.emailreader.EmailReaderUtilities.isAutoReply;

class EmailReaderUtilitiesTest {

	@Test
	void isAutoReplyWithAutoSubmittedHeader() {
		final var email = new Email();
		email.setHeaders(Map.of("AUTO_SUBMITTED", List.of("auto-replied")));

		assertThat(isAutoReply(email)).isTrue();
	}

	@Test
	void isAutoReplyWithNoHeader() {
		final var email = new Email();
		email.setHeaders(Map.of("AUTO_SUBMITTED", List.of("No")));

		assertThat(isAutoReply(email)).isFalse();
	}

	@Test
	void isAutoReplyWithNullHeaders() {
		final var email = new Email();
		email.setHeaders(null);

		assertThat(isAutoReply(email)).isFalse();
	}

	@Test
	void isNotAutoReplyForRegularEmail() {
		final var email = new Email();
		email.setSender("user@domain.com");

		assertThat(isAutoReply(email)).isFalse();
	}
}
