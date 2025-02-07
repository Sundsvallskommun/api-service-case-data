package se.sundsvall.casedata.service;

import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.ERRAND_ENTITY_NOT_FOUND;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toStatusEntity;

import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.casedata.api.model.Status;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;

@Service
public class StatusService {

	private final ErrandRepository errandRepository;
	private final ProcessService processService;

	public StatusService(final ErrandRepository errandRepository, final ProcessService processService) {
		this.errandRepository = errandRepository;
		this.processService = processService;
	}

	@Retry(name = "OptimisticLocking")
	public void addToErrand(final Long errandId, final String municipalityId, final String namespace, final Status status) {
		final var oldErrand = findErrandEntity(errandId, municipalityId, namespace);
		final var statusEntity = toStatusEntity(status);
		oldErrand.setStatus(status.getStatusType());
		oldErrand.getStatuses().add(statusEntity);
		final var updatedErrand = errandRepository.save(oldErrand);
		processService.updateProcess(updatedErrand);
	}

	private ErrandEntity findErrandEntity(final Long errandId, final String municipalityId, final String namespace) {
		return errandRepository.findByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERRAND_ENTITY_NOT_FOUND.formatted(errandId, namespace, municipalityId)));
	}
}
