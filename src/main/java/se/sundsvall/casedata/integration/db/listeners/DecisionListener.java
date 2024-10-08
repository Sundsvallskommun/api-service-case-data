package se.sundsvall.casedata.integration.db.listeners;

import org.springframework.stereotype.Component;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import se.sundsvall.casedata.integration.db.model.Decision;

@Component
public class DecisionListener {

	private final ErrandListener errandListener;

	public DecisionListener(final ErrandListener errandListener) {
		this.errandListener = errandListener;
	}

	@PostPersist
	private void postPersist(final Decision decision) {
		errandListener.updateErrandFields(decision.getErrand());
	}

	@PreUpdate
	@PreRemove
	private void preUpdate(final Decision decision) {
		errandListener.updateErrandFields(decision.getErrand());
	}
}
