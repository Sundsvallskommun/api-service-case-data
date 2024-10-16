package se.sundsvall.casedata.integration.db.listeners;

import org.springframework.stereotype.Component;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import se.sundsvall.casedata.integration.db.model.AppealEntity;

@Component
public class AppealListener {

	private final ErrandListener errandListener;

	public AppealListener(final ErrandListener errandListener) {
		this.errandListener = errandListener;
	}

	@PostPersist
	private void postPersist(final AppealEntity appealEntity) {
		errandListener.updateErrandFields(appealEntity.getErrand());
	}

	@PreUpdate
	@PreRemove
	private void preUpdate(final AppealEntity appealEntity) {
		errandListener.updateErrandFields(appealEntity.getErrand());
	}
}
