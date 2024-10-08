package se.sundsvall.casedata.integration.db.listeners;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;

import org.springframework.stereotype.Component;

import se.sundsvall.casedata.integration.db.model.FacilityEntity;

@Component
public class FacilityListener {

	private final ErrandListener errandListener;

	public FacilityListener(final ErrandListener errandListener) {
		this.errandListener = errandListener;
	}

	@PostPersist
	private void postPersist(final FacilityEntity facility) {
		errandListener.updateErrandFields(facility.getErrand());
	}

	@PreUpdate
	@PreRemove
	private void preUpdate(final FacilityEntity facility) {
		errandListener.updateErrandFields(facility.getErrand());
	}

}
