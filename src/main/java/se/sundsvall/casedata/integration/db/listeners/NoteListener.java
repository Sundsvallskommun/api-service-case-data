package se.sundsvall.casedata.integration.db.listeners;

import static se.sundsvall.casedata.service.util.ServiceUtil.getAdUser;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import org.springframework.stereotype.Component;
import se.sundsvall.casedata.integration.db.model.NoteEntity;

@Component
public class NoteListener {

	private final ErrandListener errandListener;

	public NoteListener(final ErrandListener errandListener) {
		this.errandListener = errandListener;
	}

	@PrePersist
	private void postPersist(final NoteEntity noteEntity) {
		noteEntity.setCreatedBy(getAdUser());
		errandListener.updateErrandFields(noteEntity.getErrand());
	}

	@PreUpdate
	@PreRemove
	private void preUpdate(final NoteEntity noteEntity) {
		noteEntity.setUpdatedBy(getAdUser());
		errandListener.updateErrandFields(noteEntity.getErrand());
	}
}
