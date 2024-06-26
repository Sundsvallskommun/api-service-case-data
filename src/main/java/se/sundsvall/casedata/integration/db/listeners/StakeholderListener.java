package se.sundsvall.casedata.integration.db.listeners;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import org.springframework.stereotype.Component;
import se.sundsvall.casedata.integration.db.model.Stakeholder;

import java.time.OffsetDateTime;

@Component
public class StakeholderListener {

	private final ErrandListener errandListener;

	public StakeholderListener(final ErrandListener errandListener) {
		this.errandListener = errandListener;
	}

	@PostPersist
	private void postPersist(final Stakeholder stakeholder) {
		errandListener.updateErrandFields(stakeholder.getErrand());
	}

	@PreUpdate
	@PreRemove
	private void preUpdate(final Stakeholder stakeholder) {
		if (stakeholder.getCreated() == null) {
			stakeholder.setCreated(OffsetDateTime.now());
		}
		errandListener.updateErrandFields(stakeholder.getErrand());
	}
}
