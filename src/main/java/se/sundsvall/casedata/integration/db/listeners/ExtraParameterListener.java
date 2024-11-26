package se.sundsvall.casedata.integration.db.listeners;

import org.springframework.stereotype.Component;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import se.sundsvall.casedata.integration.db.model.ExtraParameterEntity;

@Component
public class ExtraParameterListener {

	private final ErrandListener errandListener;

	public ExtraParameterListener(final ErrandListener errandListener) {
		this.errandListener = errandListener;
	}

	@PrePersist
	private void postPersist(final ExtraParameterEntity extraParameterEntity) {
		errandListener.updateErrandFields(extraParameterEntity.getErrand());
	}

	@PreUpdate
	@PreRemove
	private void preUpdate(final ExtraParameterEntity extraParameterEntity) {
		errandListener.updateErrandFields(extraParameterEntity.getErrand());
	}
}
