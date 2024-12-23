package se.sundsvall.casedata.integration.db.listeners;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.MILLIS;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.stereotype.Component;
import se.sundsvall.casedata.integration.db.model.NotificationEntity;

@Component
public class NotificationListener {

	private final ErrandListener errandListener;

	public NotificationListener(final ErrandListener errandListener) {
		this.errandListener = errandListener;
	}

	@PrePersist
	void prePersist(final NotificationEntity notification) {
		notification.setCreated(now(systemDefault()).truncatedTo(MILLIS));
		errandListener.updateErrandFields(notification.getErrand());
	}

	@PreUpdate
	void preUpdate(final NotificationEntity notification) {
		notification.setModified(now(systemDefault()).truncatedTo(MILLIS));
		errandListener.updateErrandFields(notification.getErrand());
	}
}
