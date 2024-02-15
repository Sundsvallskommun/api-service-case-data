package se.sundsvall.casedata.integration.db.listeners;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;

import org.springframework.stereotype.Component;

import se.sundsvall.casedata.api.filter.IncomingRequestFilter;
import se.sundsvall.casedata.integration.db.model.Note;

@Component
public class NoteListener {

	private final IncomingRequestFilter incomingRequestFilter;

	private final ErrandListener errandListener;

	public NoteListener(final IncomingRequestFilter incomingRequestFilter, final ErrandListener errandListener) {
		this.incomingRequestFilter = incomingRequestFilter;
		this.errandListener = errandListener;
	}

	@PostPersist
	private void postPersist(final Note note) {
		note.setCreatedBy(incomingRequestFilter.getAdUser());
		errandListener.updateErrandFields(note.getErrand());
	}

	@PreUpdate
	@PreRemove
	private void preUpdate(final Note note) {
		note.setUpdatedBy(incomingRequestFilter.getAdUser());
		errandListener.updateErrandFields(note.getErrand());
	}

}
