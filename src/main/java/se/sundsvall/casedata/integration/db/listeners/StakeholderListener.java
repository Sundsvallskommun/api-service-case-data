package se.sundsvall.casedata.integration.db.listeners;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;

import org.springframework.stereotype.Component;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import se.sundsvall.casedata.integration.db.model.StakeholderEntity;

@Component
public class StakeholderListener {

	private final ErrandListener errandListener;

	public StakeholderListener(final ErrandListener errandListener) {
		this.errandListener = errandListener;
	}

	@PostPersist
	private void postPersist(final StakeholderEntity stakeholder) {
		errandListener.updateErrandFields(stakeholder.getErrand());
	}

	@PreUpdate
	@PreRemove
	private void preUpdate(final StakeholderEntity stakeholder) {
		if (stakeholder.getCreated() == null) {
			stakeholder.setCreated(now(systemDefault()));
		}
		errandListener.updateErrandFields(stakeholder.getErrand());
	}
}
