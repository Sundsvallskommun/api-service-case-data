package se.sundsvall.casedata.service.scheduler.emailreader;

import generated.se.sundsvall.emailreader.Email;
import java.util.Optional;

import static java.util.Collections.emptyList;

final class EmailReaderUtilities {

	private EmailReaderUtilities() {
		// Utility class
	}

	static boolean isAutoReply(final Email email) {
		return Optional.ofNullable(email.getHeaders())
			.map(headers -> headers.getOrDefault("AUTO_SUBMITTED", emptyList()))
			.orElse(emptyList())
			.stream()
			.anyMatch(value -> !"No".equalsIgnoreCase(value));
	}
}
