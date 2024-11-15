package se.sundsvall.casedata.integration.db.listeners;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import org.springframework.stereotype.Component;
import se.sundsvall.casedata.integration.db.model.AppealEntity;

@Component
public class AppealListener {

	private final ErrandListener errandListener;

	public AppealListener(final ErrandListener errandListener) {
		this.errandListener = errandListener;
	}

	@PrePersist
	private void postPersist(final AppealEntity appealEntity) {
		errandListener.updateErrandFields(appealEntity.getErrand());
	}

	@PreUpdate
	@PreRemove
	private void preUpdate(final AppealEntity appealEntity) {
		errandListener.updateErrandFields(appealEntity.getErrand());
	}
}
