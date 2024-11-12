package se.sundsvall.casedata.integration.db.listeners;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import org.springframework.stereotype.Component;
import se.sundsvall.casedata.api.filter.IncomingRequestFilter;
import se.sundsvall.casedata.integration.db.model.ExtraParameterEntity;

@Component
public class ExtraParameterListener {

	private final IncomingRequestFilter incomingRequestFilter;

	private final ErrandListener errandListener;

	public ExtraParameterListener(final IncomingRequestFilter incomingRequestFilter, final ErrandListener errandListener) {
		this.incomingRequestFilter = incomingRequestFilter;
		this.errandListener = errandListener;
	}

	@PostPersist
	private void postPersist(final ExtraParameterEntity extraParameterEntity) {
		errandListener.updateErrandFields(extraParameterEntity.getErrand());
	}

	@PreUpdate
	@PreRemove
	private void preUpdate(final ExtraParameterEntity extraParameterEntity) {
		errandListener.updateErrandFields(extraParameterEntity.getErrand());
	}

}
