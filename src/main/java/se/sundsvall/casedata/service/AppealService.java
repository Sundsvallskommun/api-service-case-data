package se.sundsvall.casedata.service;

import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAppealDto;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchAppeal;
import static se.sundsvall.casedata.service.util.mappers.PutMapper.putAppeal;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import se.sundsvall.casedata.api.model.AppealDTO;
import se.sundsvall.casedata.api.model.PatchAppealDTO;
import se.sundsvall.casedata.integration.db.AppealRepository;

import io.github.resilience4j.retry.annotation.Retry;

@Service
public class AppealService {

	private static final String APPEAL_NOT_FOUND = "Appeal not found";

	private final AppealRepository appealRepository;

	public AppealService(final AppealRepository appealRepository) {
		this.appealRepository = appealRepository;
	}

	public AppealDTO findByIdAndMunicipalityIdAndNamespace(final Long id, final String municipalityId, final String namespace) {
		return toAppealDto(appealRepository.findByIdAndMunicipalityIdAndNamespace(id, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, APPEAL_NOT_FOUND)));
	}

	@Retry(name = "OptimisticLocking")
	public void replaceAppeal(final Long id, final String municipalityId, final String namespace, final AppealDTO dto) {
		final var entity = appealRepository.findByIdAndMunicipalityIdAndNamespace(id, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, APPEAL_NOT_FOUND));

		if ((entity.getDecision() != null) && !entity.getDecision().getId().equals(dto.getDecisionId())) {
			final var changedDecision = entity.getErrand().getDecisions().stream()
				.filter(decision -> decision.getId().equals(dto.getDecisionId()))
				.findFirst()
				.orElse(null);
			entity.setDecision(changedDecision);
		}

		appealRepository.save(putAppeal(entity, dto));
	}

	@Retry(name = "OptimisticLocking")
	public void updateAppeal(final Long id, final String municipalityId, final String namespace, final PatchAppealDTO dto) {
		final var entity = appealRepository.findByIdAndMunicipalityIdAndNamespace(id, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, APPEAL_NOT_FOUND));
		appealRepository.save(patchAppeal(entity, dto));
	}

}
