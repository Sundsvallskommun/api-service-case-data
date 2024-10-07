package se.sundsvall.casedata.service;

import static java.text.MessageFormat.format;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.ERRAND_WAS_NOT_FOUND;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toStatusEntity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import se.sundsvall.casedata.api.model.Status;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

import io.github.resilience4j.retry.annotation.Retry;

@Service
public class StatusService {

	private final ErrandRepository errandRepository;

	private final ProcessService processService;

	public StatusService(final ErrandRepository errandRepository, final ProcessService processService) {
		this.errandRepository = errandRepository;
		this.processService = processService;
	}

	@Retry(name = "OptimisticLocking")
	public void addStatusToErrand(final Long errandId, final String municipalityId, final String namespace, final Status status) {
		final var oldErrand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		final var statusEntity = toStatusEntity(status);
		oldErrand.getStatuses().add(statusEntity);
		final var updatedErrand = errandRepository.save(oldErrand);
		processService.updateProcess(updatedErrand);
	}

	@Retry(name = "OptimisticLocking")
	public void replaceStatusesOnErrand(final Long errandId, final String municipalityId, final String namespace, final List<Status> statuses) {
		final var oldErrand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		oldErrand.setStatuses(new ArrayList<>(statuses.stream().map(EntityMapper::toStatusEntity).toList()));
		final var updatedErrand = errandRepository.save(oldErrand);
		processService.updateProcess(updatedErrand);
	}

	private ErrandEntity getErrandByIdAndMunicipalityIdAndNamespace(final Long errandId, final String municipalityId, final String namespace) {
		return errandRepository.findByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND,
				format(ERRAND_WAS_NOT_FOUND, errandId)));
	}

}
