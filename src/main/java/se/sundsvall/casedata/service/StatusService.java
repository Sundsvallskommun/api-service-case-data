package se.sundsvall.casedata.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;
import se.sundsvall.casedata.api.model.Status;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.eventlog.EventlogIntegration;

import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.ERRAND_ENTITY_NOT_FOUND;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toStatusEntity;

@Service
@Transactional
public class StatusService {

	private final ErrandRepository errandRepository;
	private final ApplicationEventPublisher applicationEventPublisher;
	private final EventlogIntegration eventlogIntegration;

	public StatusService(final ErrandRepository errandRepository, final ApplicationEventPublisher applicationEventPublisher, final EventlogIntegration eventlogIntegration) {
		this.errandRepository = errandRepository;
		this.applicationEventPublisher = applicationEventPublisher;
		this.eventlogIntegration = eventlogIntegration;
	}

	public void addToErrand(final Long errandId, final String municipalityId, final String namespace, final Status status) {
		final var oldErrand = findErrandEntity(errandId, municipalityId, namespace);
		final var statusEntity = toStatusEntity(status);
		oldErrand.setStatus(statusEntity);
		oldErrand.getStatuses().add(statusEntity);
		final var updatedErrand = errandRepository.saveAndFlush(oldErrand);
		applicationEventPublisher.publishEvent(updatedErrand);
		eventlogIntegration.sendEventlogEvent(municipalityId, updatedErrand, status);
	}

	private ErrandEntity findErrandEntity(final Long errandId, final String municipalityId, final String namespace) {
		return errandRepository.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERRAND_ENTITY_NOT_FOUND.formatted(errandId, namespace, municipalityId)));
	}
}
