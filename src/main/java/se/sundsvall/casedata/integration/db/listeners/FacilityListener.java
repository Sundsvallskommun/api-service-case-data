package se.sundsvall.casedata.integration.db.listeners;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;

import org.springframework.stereotype.Component;

import se.sundsvall.casedata.integration.db.model.Facility;

@Component
public class FacilityListener {

	private final ErrandListener errandListener;

	public FacilityListener(final ErrandListener errandListener) {
		this.errandListener = errandListener;
	}

	@PostPersist
	private void postPersist(final Facility facility) {
		errandListener.updateErrandFields(facility.getErrand());
	}

	@PreUpdate
	@PreRemove
	private void preUpdate(final Facility facility) {
		errandListener.updateErrandFields(facility.getErrand());
	}

}
