package se.sundsvall.casedata.integration.db.listeners;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;

import org.springframework.stereotype.Component;

import se.sundsvall.casedata.integration.db.model.DecisionEntity;

@Component
public class DecisionListener {

	private final ErrandListener errandListener;

	public DecisionListener(final ErrandListener errandListener) {
		this.errandListener = errandListener;
	}

	@PostPersist
	private void postPersist(final DecisionEntity decision) {
		errandListener.updateErrandFields(decision.getErrand());
	}

	@PreUpdate
	@PreRemove
	private void preUpdate(final DecisionEntity decision) {
		errandListener.updateErrandFields(decision.getErrand());
	}

}
