package se.sundsvall.casedata.service.scheduler.emailreader;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.MessageFormat;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ErrandNumberParserTest {

	@ParameterizedTest
	@ValueSource(strings = {
		"SGP-2022-01", "SGP-2022-000001", "PH-2022-000001", "SGP-2022-011111111111", "##SGP-2022-011111111111", "asd"
	})
	void parseSubject(final String errandNumber) {

		final var subject = MessageFormat.format("Ärende #{0} Ansökan om bygglov för fastighet KATARINA 4", errandNumber);

		final var result = ErrandNumberParser.parseSubject(subject);

		assertThat(result).isEqualTo(errandNumber);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"SGP-2022-01", "SGP-2022-000001", "PH-2022-000001", "SGP-2022-011111111111", "##SGP-2022-011111111111", "asd"
	})
	void parseSubjectWithNoSpaceAfterErrandNUmber(final String errandNumber) {

		final var result = ErrandNumberParser.parseSubject(MessageFormat.format("Ärende #{0}", errandNumber));

		assertThat(result).isEqualTo(errandNumber);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"", "SGP-2022-01", "Ärende SGP-2022-000001 Ansökan"
	})
	void parseSubjectFaultyValues(final String subject) {

		final var result = ErrandNumberParser.parseSubject(subject);

		assertThat(result).isNull();
	}

}
