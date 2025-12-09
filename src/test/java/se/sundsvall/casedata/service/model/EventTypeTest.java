package se.sundsvall.casedata.service.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EventTypeTest {

	@Test
	void testEnumValues() {
		assertThat(EventType.values()).containsExactlyInAnyOrder(
			EventType.ACCESS,
			EventType.CANCEL,
			EventType.CREATE,
			EventType.DELETE,
			EventType.DROP,
			EventType.EXECUTE,
			EventType.READ,
			EventType.UPDATE);
	}
}
