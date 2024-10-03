package se.sundsvall.casedata.integration.db.listeners;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;

import org.springframework.stereotype.Component;

import se.sundsvall.casedata.integration.db.model.Appeal;

@Component
public class AppealListener {

	private final ErrandListener errandListener;

	public AppealListener(final ErrandListener errandListener) {
		this.errandListener = errandListener;
	}

	@PostPersist
	private void postPersist(final Appeal appeal) {
		errandListener.updateErrandFields(appeal.getErrand());
	}

	@PreUpdate
	@PreRemove
	private void preUpdate(final Appeal appeal) {
		errandListener.updateErrandFields(appeal.getErrand());
	}

}
