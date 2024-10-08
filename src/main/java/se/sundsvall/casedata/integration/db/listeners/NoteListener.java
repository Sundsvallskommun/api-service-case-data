package se.sundsvall.casedata.integration.db.listeners;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;

import org.springframework.stereotype.Component;

import se.sundsvall.casedata.api.filter.IncomingRequestFilter;
import se.sundsvall.casedata.integration.db.model.NoteEntity;

@Component
public class NoteListener {

	private final IncomingRequestFilter incomingRequestFilter;

	private final ErrandListener errandListener;

	public NoteListener(final IncomingRequestFilter incomingRequestFilter, final ErrandListener errandListener) {
		this.incomingRequestFilter = incomingRequestFilter;
		this.errandListener = errandListener;
	}

	@PostPersist
	private void postPersist(final NoteEntity noteEntity) {
		noteEntity.setCreatedBy(incomingRequestFilter.getAdUser());
		errandListener.updateErrandFields(noteEntity.getErrand());
	}

	@PreUpdate
	@PreRemove
	private void preUpdate(final NoteEntity noteEntity) {
		noteEntity.setUpdatedBy(incomingRequestFilter.getAdUser());
		errandListener.updateErrandFields(noteEntity.getErrand());
	}

}
