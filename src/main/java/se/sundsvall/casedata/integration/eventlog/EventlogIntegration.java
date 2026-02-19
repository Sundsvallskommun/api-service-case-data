package se.sundsvall.casedata.integration.eventlog;

import generated.se.sundsvall.eventlog.Event;
import generated.se.sundsvall.eventlog.EventType;
import generated.se.sundsvall.eventlog.Metadata;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.casedata.api.model.Status;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;

@Component
public class EventlogIntegration {

	private static final Logger LOG = LoggerFactory.getLogger(EventlogIntegration.class);

	private final EventlogClient eventlogClient;

	public EventlogIntegration(final EventlogClient eventlogClient) {
		this.eventlogClient = eventlogClient;
	}

	public void sendEventlogEvent(final String municipalityId, final ErrandEntity errand, final Status status) {
		try {
			final var event = new Event()
				.type(EventType.UPDATE)
				.owner("CaseData")
				.message("Status updated to " + status.getStatusType())
				.sourceType("Errand")
				.metadata(List.of(new Metadata().key("Status").value(status.getStatusType())));

			eventlogClient.createEvent(municipalityId, String.valueOf(errand.getId()), event);
		} catch (final Exception e) {
			LOG.error("Failed to send event to eventlog for errand with id: {}", errand.getId(), e);
		}
	}
}
